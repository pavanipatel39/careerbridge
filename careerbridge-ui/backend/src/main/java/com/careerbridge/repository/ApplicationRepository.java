package com.careerbridge.repository;

import com.careerbridge.model.InternshipApplication;
import java.io.IOException;
import java.util.List;

public interface ApplicationRepository {
    void save(InternshipApplication application) throws IOException;

    List<InternshipApplication> findByEmail(String email) throws IOException;
}
