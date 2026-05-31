package com.example.sdwan.repository.impl;

import com.example.sdwan.data.MockDataStore;
import com.example.sdwan.domain.WanDataPoint;
import com.example.sdwan.repository.WanHistoryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class InMemoryWanHistoryRepository implements WanHistoryRepository {

    private final MockDataStore store;

    public InMemoryWanHistoryRepository(MockDataStore store) {
        this.store = store;
    }

    @Override
    public List<WanDataPoint> findByInterfaceId(String interfaceId) {
        return store.getWanHistory().getOrDefault(interfaceId, List.of());
    }
}
