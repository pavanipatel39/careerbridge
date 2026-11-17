package com.careerbridge.server;

import com.careerbridge.db.Database;
import com.careerbridge.exception.*;
import com.careerbridge.model.*;
import com.careerbridge.repository.*;
import com.careerbridge.service.*;
import com.careerbridge.util.Json;
import com.sun.net.httpserver.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;

/** HTTP server: JSON API under /api/ and the static frontend (allow-listed file types only). */
public class ApiServer {
    private static final int MAX_BODY = 64 * 1024;
    private static final Set<String> BLOCKED_DIRS = Set.of("data", "backend", "docs", "target", "lib", ".git", ".github");
    private static final Map<String, String> TYPES = Map.ofEntries(
            Map.entry("html", "text/html; charset=utf-8"), Map.entry("css", "text/css; charset=utf-8"),
            Map.entry("js", "text/javascript; charset=utf-8"), Map.entry("svg", "image/svg+xml"),
            Map.entry("png", "image/png"), Map.entry("jpg", "image/jpeg"), Map.entry("jpeg", "image/jpeg"),
            Map.entry("ico", "image/x-icon"), Map.entry("webp", "image/webp"));

    private final HttpServer server;
    private final Database database;
    private final AuthService auth;
    private final ApplicationRepository applications;
    private final GoogleTokenVerifier googleVerifier;
    private final String googleClientId;
    private final BackgroundProcessor background = new BackgroundProcessor();
    private final StorageRepository activityLog = new FileRepository("activity.log");
    private final Path frontend = Paths.get(com.careerbridge.util.Config.get("STATIC_DIR", ".")).toAbsolutePath().normalize();

    public ApiServer(int port) throws IOException {
        this(port, new Database(), System.getenv().getOrDefault("GOOGLE_CLIENT_ID", "").trim());
    }

    private ApiServer(int port, Database db, String clientId) throws IOException {
        this(port, db, new AuthService(new JdbcUserRepository(db)), new JdbcApplicationRepository(db), clientId,
                clientId.isBlank() ? null : new TokenInfoVerifier(clientId));
    }

    /** Full constructor (also used by tests to plug in fakes). */
    public ApiServer(int port, Database db, AuthService auth, ApplicationRepository applications,
                     String googleClientId, GoogleTokenVerifier googleVerifier) throws IOException {
        this.database = db;
        this.auth = auth;
        this.applications = applications;
        this.googleClientId = googleClientId;
        this.googleVerifier = googleVerifier;
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.createContext("/api/", this::api);
        server.createContext("/", this::staticFiles);
    }

    public void start() { server.start(); }

    /** Actual listening port (useful when started with port 0 in tests). */
    public int port() { return server.getAddress().getPort(); }

    public void stop() { server.stop(0); background.close(); }

    private void api(HttpExchange x) throws IOException {
        cors(x);
        String path = x.getRequestURI().getPath(), method = x.getRequestMethod();
        try {
            if (method.equals("OPTIONS")) { x.sendResponseHeaders(204, -1); return; }
            if (path.equals("/api/health")) {
                boolean db = database.ping();
                send(x, db ? 200 : 503, "{\"status\":\"" + (db ? "UP" : "DEGRADED") + "\",\"database\":\""
                        + (db ? "UP" : "DOWN") + "\",\"service\":\"CareerBridge Java Backend\"}");
                return;
            }
            if (path.equals("/api/config") && method.equals("GET")) {
                send(x, 200, "{\"googleClientId\":" + Json.quote(googleClientId) + "}");
                return;
            }
            byte[] raw = x.getRequestBody().readNBytes(MAX_BODY + 1);
            if (raw.length > MAX_BODY) throw new InvalidInputException("Request body too large.");
            String body = new String(raw, StandardCharsets.UTF_8);

            if (path.equals("/api/auth/register") && method.equals("POST")) {
                Student s = auth.register(Json.get(body, "name"), Json.get(body, "email"), Json.get(body, "password"));
                log("REGISTER " + s.getEmail());
                send(x, 201, userJson(s));
            } else if (path.equals("/api/auth/login") && method.equals("POST")) {
                Student s = auth.login(Json.get(body, "email"), Json.get(body, "password"));
                log("LOGIN " + s.getEmail());
                send(x, 200, userJson(s));
            } else if (path.equals("/api/auth/google") && method.equals("POST")) {
                if (googleVerifier == null) {
                    error(x, 503, "Google sign-in is not configured on the server (set GOOGLE_CLIENT_ID).");
                    return;
                }
                GoogleTokenVerifier.GoogleProfile g = googleVerifier.verify(Json.get(body, "credential"));
                Student s = auth.loginWithGoogle(g.email(), g.name());
                log("GOOGLE_LOGIN " + s.getEmail());
                send(x, 200, userJson(s));
            } else if (path.equals("/api/applications") && method.equals("POST")) {
                String email = Json.get(body, "email").trim().toLowerCase(), internship = Json.get(body, "internshipId").trim();
                if (email.isBlank() || internship.isBlank() || internship.length() > 60)
                    throw new InvalidInputException("Email and internshipId are required.");
                InternshipApplication a = new InternshipApplication(UUID.randomUUID().toString(), email, internship);
                background.submit(() -> {
                    try { applications.save(a); log("APPLICATION " + email + " " + internship); }
                    catch (IOException e) { System.err.println("Could not save application: " + e.getMessage()); }
                });
                send(x, 202, "{\"success\":true,\"message\":\"Application accepted for processing\"}");
            } else if (path.equals("/api/applications") && method.equals("GET")) {
                String email = queryParam(x, "email");
                if (email.isBlank()) throw new InvalidInputException("email query parameter is required.");
                StringJoiner items = new StringJoiner(",", "[", "]");
                for (InternshipApplication a : applications.findByEmail(email))
                    items.add("{\"id\":" + Json.quote(a.getId()) + ",\"email\":" + Json.quote(a.getEmail())
                            + ",\"internshipId\":" + Json.quote(a.getInternshipId()) + ",\"status\":" + Json.quote(a.getStatus())
                            + ",\"createdAt\":" + Json.quote(a.getCreatedAt()) + "}");
                send(x, 200, "{\"items\":" + items + "}");
            } else {
                send(x, 404, "{\"success\":false,\"message\":\"API endpoint not found\"}");
            }
        } catch (InvalidInputException e) { error(x, 400, e.getMessage());
        } catch (InvalidCredentialsException e) { error(x, 401, e.getMessage());
        } catch (DuplicateUserException e) { error(x, 409, e.getMessage());
        } catch (Exception e) { e.printStackTrace(); error(x, 500, "Server error"); }
    }

    /** Appends one line to data/activity.log (file management; runs on the background thread pool). */
    private void log(String event) {
        background.submit(() -> {
            try { activityLog.append(LocalDateTime.now() + "\t" + event); }
            catch (IOException e) { System.err.println("Could not write activity log: " + e.getMessage()); }
        });
    }

    private String queryParam(HttpExchange x, String key) {
        String q = x.getRequestURI().getQuery();
        if (q == null) return "";
        for (String part : q.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv[0].equals(key) && kv.length == 2)
                return java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8).trim();
        }
        return "";
    }

    private String userJson(User u) {
        return "{\"success\":true,\"user\":{\"name\":" + Json.quote(u.getName()) + ",\"email\":" + Json.quote(u.getEmail())
                + ",\"role\":" + Json.quote(u.getRole()) + "}}";
    }

    private void staticFiles(HttpExchange x) throws IOException {
        String p = x.getRequestURI().getPath();
        if (p.equals("/")) p = "/index.html";
        Path f = frontend.resolve("." + p).normalize();
        String first = f.startsWith(frontend) && f.getNameCount() > frontend.getNameCount()
                ? f.getName(frontend.getNameCount()).toString().toLowerCase() : "";
        String name = f.getFileName() == null ? "" : f.getFileName().toString().toLowerCase();
        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : "";
        if (!f.startsWith(frontend) || BLOCKED_DIRS.contains(first) || !TYPES.containsKey(ext)
                || !Files.isRegularFile(f)) {
            send(x, 404, "{\"success\":false,\"message\":\"Not found\"}");
            return;
        }
        byte[] bytes = Files.readAllBytes(f);
        x.getResponseHeaders().set("Content-Type", TYPES.get(ext));
        x.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        x.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = x.getResponseBody()) { out.write(bytes); }
    }

    private void cors(HttpExchange x) {
        x.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        x.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        x.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private void error(HttpExchange x, int status, String msg) throws IOException {
        send(x, status, "{\"success\":false,\"message\":" + Json.quote(msg) + "}");
    }

    private void send(HttpExchange x, int status, String body) throws IOException {
        byte[] b = body.getBytes(StandardCharsets.UTF_8);
        x.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        x.sendResponseHeaders(status, b.length);
        try (OutputStream o = x.getResponseBody()) { o.write(b); }
    }
}
