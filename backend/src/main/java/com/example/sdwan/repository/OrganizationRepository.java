package com.example.sdwan.repository;

import com.example.sdwan.domain.Organization;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepository {
    List<Organization> findAll();
    Optional<Organization> findById(String id);
}
