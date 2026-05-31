package com.example.sdwan.repository;

import com.example.sdwan.domain.Device;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository {
    List<Device> findBySiteId(String siteId);
    Optional<Device> findById(String id);
}
