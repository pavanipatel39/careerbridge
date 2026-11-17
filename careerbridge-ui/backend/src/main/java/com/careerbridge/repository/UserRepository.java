package com.careerbridge.repository;

import com.careerbridge.exception.DuplicateUserException;
import java.io.IOException;
import java.util.Optional;

public interface UserRepository {
    record UserRecord(String name, String email, String passwordHash, String role, String provider) { }

    Optional<UserRecord> findByEmail(String email) throws IOException;

    void save(UserRecord user) throws DuplicateUserException, IOException;
}
