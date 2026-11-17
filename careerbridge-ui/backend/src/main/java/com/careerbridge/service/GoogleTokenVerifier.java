package com.careerbridge.service;

import com.careerbridge.exception.InvalidCredentialsException;
import java.io.IOException;

/** Checks a Google "ID token" and returns the verified profile. */
public interface GoogleTokenVerifier {
    record GoogleProfile(String email, String name) { }

    GoogleProfile verify(String idToken) throws InvalidCredentialsException, IOException;
}
