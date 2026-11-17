package com.careerbridge.service;

import com.careerbridge.exception.InvalidCredentialsException;
import com.careerbridge.util.Json;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Verifies a Google ID token using Google's tokeninfo endpoint (checks signature, expiry, audience). */
public class TokenInfoVerifier implements GoogleTokenVerifier {
    private final String clientId;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public TokenInfoVerifier(String clientId) { this.clientId = clientId; }

    @Override
    public GoogleProfile verify(String idToken) throws InvalidCredentialsException, IOException {
        if (idToken == null || idToken.isBlank()) throw new InvalidCredentialsException("Google sign-in failed.");
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token="
                    + URLEncoder.encode(idToken, StandardCharsets.UTF_8))).timeout(Duration.ofSeconds(8)).GET().build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) throw new InvalidCredentialsException("Google sign-in could not be verified.");
            String body = res.body();
            String iss = Json.get(body, "iss");
            if (!clientId.equals(Json.get(body, "aud")) || !"true".equals(Json.get(body, "email_verified"))
                    || !iss.endsWith("accounts.google.com") || Json.get(body, "email").isBlank())
                throw new InvalidCredentialsException("Google sign-in could not be verified.");
            String email = Json.get(body, "email");
            String name = Json.get(body, "name");
            return new GoogleProfile(email, name.isBlank() ? email : name);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while contacting Google");
        }
    }
}
