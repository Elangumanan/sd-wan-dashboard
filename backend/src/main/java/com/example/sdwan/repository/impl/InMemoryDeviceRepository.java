package com.example.sdwan.repository.impl;

import com.example.sdwan.data.MockDataStore;
import com.example.sdwan.domain.Device;
import com.example.sdwan.repository.DeviceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryDeviceRepository implements DeviceRepository {

    private final MockDataStore store;

    public InMemoryDeviceRepository(MockDataStore store) {
        this.store = store;
    }

    @Override
    public List<Device> findBySiteId(String siteId) {
        return store.getDevices().values().stream()
                .filter(d -> d.siteId().equals(siteId))
                .toList();
    }

    @Override
    public Optional<Device> findById(String id) {
        return Optional.ofNullable(store.getDevices().get(id));
    }
}
