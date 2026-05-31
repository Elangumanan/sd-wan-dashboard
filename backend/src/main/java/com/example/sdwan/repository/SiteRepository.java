package com.example.sdwan.repository;

import com.example.sdwan.domain.Site;

import java.util.List;
import java.util.Optional;

public interface SiteRepository {
    List<Site> findAll();
    List<Site> findByOrgId(String orgId);
    Optional<Site> findById(String id);
    boolean existsById(String id);
}
