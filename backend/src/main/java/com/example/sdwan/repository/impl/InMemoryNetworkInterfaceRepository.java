package com.example.sdwan.repository.impl;

import com.example.sdwan.data.MockDataStore;
import com.example.sdwan.domain.NetworkInterface;
import com.example.sdwan.repository.NetworkInterfaceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryNetworkInterfaceRepository implements NetworkInterfaceRepository {

    private final MockDataStore store;

    public InMemoryNetworkInterfaceRepository(MockDataStore store) {
        this.store = store;
    }

    @Override
    public List<NetworkInterface> findByDeviceId(String deviceId) {
        return store.getInterfaces().values().stream()
                .filter(i -> i.deviceId().equals(deviceId))
                .toList();
    }

    @Override
    public Optional<NetworkInterface> findById(String id) {
        return Optional.ofNullable(store.getInterfaces().get(id));
    }
}
