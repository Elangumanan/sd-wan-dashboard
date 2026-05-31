package com.example.sdwan.service;

import com.example.sdwan.dto.DeviceDetailDto;
import com.example.sdwan.dto.WanHistoryDto;

public interface DeviceService {
    DeviceDetailDto getDeviceById(String orgId, String siteId, String deviceId);
    WanHistoryDto getWanHistory(String orgId, String siteId, String deviceId, String range);
}
