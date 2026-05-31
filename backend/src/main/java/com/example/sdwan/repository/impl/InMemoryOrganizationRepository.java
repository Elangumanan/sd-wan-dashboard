package com.example.sdwan.repository.impl;

import com.example.sdwan.data.MockDataStore;
import com.example.sdwan.domain.Organization;
import com.example.sdwan.repository.OrganizationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryOrganizationRepository implements OrganizationRepository {

    private final MockDataStore store;

    public InMemoryOrganizationRepository(MockDataStore store) {
        this.store = store;
    }

    @Override
    public List<Organization> findAll() {
        return List.copyOf(store.getOrganizations().values());
    }

    @Override
    public Optional<Organization> findById(String id) {
        return Optional.ofNullable(store.getOrganizations().get(id));
    }
}
