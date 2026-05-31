package com.example.sdwan.repository.impl;

import com.example.sdwan.data.MockDataStore;
import com.example.sdwan.domain.Site;
import com.example.sdwan.repository.SiteRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class InMemorySiteRepository implements SiteRepository {

    private final MockDataStore store;

    public InMemorySiteRepository(MockDataStore store) {
        this.store = store;
    }

    @Override
    public List<Site> findAll() {
        return List.copyOf(store.getSites().values());
    }

    @Override
    public List<Site> findByOrgId(String orgId) {
        return store.getSites().values().stream()
                .filter(s -> s.orgId().equals(orgId))
                .toList();
    }

    @Override
    public Optional<Site> findById(String id) {
        return Optional.ofNullable(store.getSites().get(id));
    }

    @Override
    public boolean existsById(String id) {
        return store.getSites().containsKey(id);
    }
}
