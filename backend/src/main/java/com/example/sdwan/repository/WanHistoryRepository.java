package com.example.sdwan.repository;

import com.example.sdwan.domain.WanDataPoint;

import java.util.List;

public interface WanHistoryRepository {
    List<WanDataPoint> findByInterfaceId(String interfaceId);
}
