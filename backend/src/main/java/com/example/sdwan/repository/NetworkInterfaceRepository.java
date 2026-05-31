package com.example.sdwan.repository;

import com.example.sdwan.domain.NetworkInterface;

import java.util.List;
import java.util.Optional;

public interface NetworkInterfaceRepository {
    List<NetworkInterface> findByDeviceId(String deviceId);
    Optional<NetworkInterface> findById(String id);
}
