package com.careerbridge.server;

import static org.junit.jupiter.api.Assertions.*;

import com.careerbridge.db.Database;
import com.careerbridge.exception.InvalidCredentialsException;
import com.careerbridge.repository.*;
import com.careerbridge.service.*;
import java.net.URI;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;

/** Integration tests: a real HTTP server on a random port with an in-memory database. */
class ApiServerTest {
    static ApiServer server, noGoogleServer;
    static Database db;
    static String base, baseNoGoogle;
    static final HttpClient http = HttpClient.newHttpClient();

    static final GoogleTokenVerifier FAKE_GOOGLE = token -> {
        if (token.equals("good-token")) return new GoogleTokenVerifier.GoogleProfile("gina@gmail.com", "Gina G");
        throw new InvalidCredentialsException("Google sign-in could not be verified.");
    };

    record Res(int status, String body) { }

    @BeforeAll static void start() throws Exception {
        db = new Database("jdbc:h2:mem:api" + UUID.randomUUID().toString().replace("-", "") + ";DB_CLOSE_DELAY=-1", "sa", "");
        server = new ApiServer(0, db, new AuthService(new JdbcUserRepository(db)), new JdbcApplicationRepository(db), "test-client-id", FAKE_GOOGLE);
        server.start();
        base = "http://127.0.0.1:" + server.port();
        Database db2 = new Database("jdbc:h2:mem:api2" + UUID.randomUUID().toString().replace("-", "") + ";DB_CLOSE_DELAY=-1", "sa", "");
        noGoogleServer = new ApiServer(0, db2, new AuthService(new JdbcUserRepository(db2)), new JdbcApplicationRepository(db2), "", null);
        noGoogleServer.start();
        baseNoGoogle = "http://127.0.0.1:" + noGoogleServer.port();
    }

    @AfterAll static void stop() { server.stop(); noGoogleServer.stop(); }

    static Res call(String method, String url, String body) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url));
        b.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        HttpResponse<String> r = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
        return new Res(r.statusCode(), r.body());
    }

    static String user(String name, String email, String pass) {
        return "{\"name\":\"" + name + "\",\"email\":\"" + email + "\",\"password\":\"" + pass + "\"}";
    }

    @Test void healthReportsServerAndDatabaseUp() throws Exception {
        Res r = call("GET", base + "/api/health", null);
        assertEquals(200, r.status());
        assertTrue(r.body().contains("\"database\":\"UP\""));
    }

    @Test void configExposesGoogleClientId() throws Exception {
        assertTrue(call("GET", base + "/api/config", null).body().contains("test-client-id"));
    }

    @Test void registerCreatesUserAndReturns201() throws Exception {
        Res r = call("POST", base + "/api/auth/register", user("Riya", "reg1@test.com", "secret123"));
        assertEquals(201, r.status());
        assertTrue(r.body().contains("\"success\":true"));
        assertFalse(r.body().contains("secret123"));
    }

    @Test void duplicateRegisterReturns409() throws Exception {
        call("POST", base + "/api/auth/register", user("A", "dup1@test.com", "secret123"));
        assertEquals(409, call("POST", base + "/api/auth/register", user("A", "dup1@test.com", "secret123")).status());
    }

    @Test void invalidRegisterReturns400() throws Exception {
        assertEquals(400, call("POST", base + "/api/auth/register", user("", "bad", "1")).status());
    }

    @Test void loginSuccessReturns200() throws Exception {
        call("POST", base + "/api/auth/register", user("Lena", "login1@test.com", "secret123"));
        Res r = call("POST", base + "/api/auth/login", "{\"email\":\"login1@test.com\",\"password\":\"secret123\"}");
        assertEquals(200, r.status());
        assertTrue(r.body().contains("Lena"));
    }

    @Test void wrongPasswordReturns401() throws Exception {
        call("POST", base + "/api/auth/register", user("Lena", "login2@test.com", "secret123"));
        assertEquals(401, call("POST", base + "/api/auth/login", "{\"email\":\"login2@test.com\",\"password\":\"wrong\"}").status());
    }

    @Test void unknownUserReturns401() throws Exception {
        assertEquals(401, call("POST", base + "/api/auth/login", "{\"email\":\"ghost@test.com\",\"password\":\"secret123\"}").status());
    }

    @Test void googleLoginWithValidTokenCreatesAccount() throws Exception {
        Res r = call("POST", base + "/api/auth/google", "{\"credential\":\"good-token\"}");
        assertEquals(200, r.status());
        assertTrue(r.body().contains("gina@gmail.com"));
        assertTrue(new JdbcUserRepository(db).findByEmail("gina@gmail.com").isPresent());
    }

    @Test void googleLoginWithBadTokenReturns401() throws Exception {
        assertEquals(401, call("POST", base + "/api/auth/google", "{\"credential\":\"forged\"}").status());
    }

    @Test void googleLoginWithoutServerConfigReturns503() throws Exception {
        assertEquals(503, call("POST", baseNoGoogle + "/api/auth/google", "{\"credential\":\"good-token\"}").status());
    }

    @Test void applicationIsAcceptedThenStoredAndOnlyVisibleToOwner() throws Exception {
        assertEquals(202, call("POST", base + "/api/applications", "{\"email\":\"app1@test.com\",\"internshipId\":\"intern-1\"}").status());
        String body = "";
        for (int i = 0; i < 40 && !body.contains("intern-1"); i++) {
            Thread.sleep(50);
            body = call("GET", base + "/api/applications?email=app1@test.com", null).body();
        }
        assertTrue(body.contains("intern-1"));
        assertFalse(call("GET", base + "/api/applications?email=other@test.com", null).body().contains("intern-1"));
    }

    @Test void applicationWithoutEmailIsRejected() throws Exception {
        assertEquals(400, call("POST", base + "/api/applications", "{\"internshipId\":\"intern-1\"}").status());
    }

    @Test void listingApplicationsWithoutEmailIsRejected() throws Exception {
        assertEquals(400, call("GET", base + "/api/applications", null).status());
    }

    @Test void unknownApiEndpointReturns404() throws Exception {
        assertEquals(404, call("GET", base + "/api/nothing", null).status());
    }

    @Test void oversizeBodyIsRejected() throws Exception {
        assertEquals(400, call("POST", base + "/api/auth/login", "a".repeat(70_000)).status());
    }

    @Test void corsPreflightReturns204() throws Exception {
        assertEquals(204, call("OPTIONS", base + "/api/auth/login", null).status());
    }

    @Test void sensitiveFoldersAreNotDownloadable() throws Exception {
        for (String p : List.of("/data/users.tsv", "/data/careerbridge.mv.db", "/backend/pom.xml", "/lib/h2.jar", "/docs/DEFECT-LOG.md"))
            assertEquals(404, call("GET", base + p, null).status(), p);
    }

    @Test void pathTraversalIsBlocked() throws Exception {
        assertEquals(404, call("GET", base + "/..%2f..%2fetc/passwd", null).status());
    }

    @Test void thirtySimultaneousRegistrationsAllSucceed() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(15);
        List<Future<Integer>> results = new ArrayList<>();
        for (int i = 0; i < 30; i++) { int n = i; results.add(pool.submit(() -> call("POST", base + "/api/auth/register", user("U" + n, "par" + n + "@test.com", "secret123")).status())); }
        for (Future<Integer> f : results) assertEquals(201, f.get(30, TimeUnit.SECONDS));
        pool.shutdown();
    }
}
