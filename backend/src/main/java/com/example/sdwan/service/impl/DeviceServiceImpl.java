package com.example.sdwan.service.impl;

import com.example.sdwan.data.MockDataStore;
import com.example.sdwan.domain.Device;
import com.example.sdwan.domain.InterfaceType;
import com.example.sdwan.domain.NetworkInterface;
import com.example.sdwan.domain.WanDataPoint;
import com.example.sdwan.dto.*;
import com.example.sdwan.exception.ExceptionFactory;
import com.example.sdwan.repository.DeviceRepository;
import com.example.sdwan.repository.NetworkInterfaceRepository;
import com.example.sdwan.repository.OrganizationRepository;
import com.example.sdwan.repository.SiteRepository;
import com.example.sdwan.repository.WanHistoryRepository;
import com.example.sdwan.service.DeviceService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DeviceServiceImpl implements DeviceService {

    private static final Map<String, Integer> RANGE_POINTS = Map.of(
            "1h",  12,
            "6h",  72,
            "24h", 288
    );

    private final OrganizationRepository      orgRepository;
    private final SiteRepository              siteRepository;
    private final DeviceRepository            deviceRepository;
    private final NetworkInterfaceRepository  ifaceRepository;
    private final WanHistoryRepository        wanHistoryRepository;
    private final MockDataStore               store;

    public DeviceServiceImpl(OrganizationRepository orgRepository,
                             SiteRepository siteRepository,
                             DeviceRepository deviceRepository,
                             NetworkInterfaceRepository ifaceRepository,
                             WanHistoryRepository wanHistoryRepository,
                             MockDataStore store) {
        this.orgRepository       = orgRepository;
        this.siteRepository      = siteRepository;
        this.deviceRepository    = deviceRepository;
        this.ifaceRepository     = ifaceRepository;
        this.wanHistoryRepository = wanHistoryRepository;
        this.store               = store;
    }

    @Override
    public DeviceDetailDto getDeviceById(String orgId, String siteId, String deviceId) {
        Device device = resolveDevice(orgId, siteId, deviceId);
        List<InterfaceDto> interfaces = ifaceRepository.findByDeviceId(deviceId).stream()
                .map(this::toInterfaceDto)
                .toList();
        return new DeviceDetailDto(
                device.id(), device.siteId(), device.name(), device.model(),
                device.status(), device.role(), device.ipAddress(), device.firmwareVersion(),
                device.lastSeen().toString(),
                interfaces
        );
    }

    @Override
    public WanHistoryDto getWanHistory(String orgId, String siteId, String deviceId, String range) {
        Device device = resolveDevice(orgId, siteId, deviceId);
        int points = RANGE_POINTS.getOrDefault(range, 288);

        List<WanInterfaceSeriesDto> series = ifaceRepository.findByDeviceId(deviceId).stream()
                .filter(i -> i.type() == InterfaceType.WAN)
                .map(iface -> toSeries(iface, points))
                .toList();

        return new WanHistoryDto(device.id(), device.name(), range, series);
    }

    private WanInterfaceSeriesDto toSeries(NetworkInterface iface, int points) {
        List<WanDataPoint> all = wanHistoryRepository.findByInterfaceId(iface.id());
        int from = Math.max(0, all.size() - points);
        List<WanDataPointDto> slice = all.subList(from, all.size()).stream()
                .map(p -> new WanDataPointDto(p.timestamp(), p.rxMbps(), p.txMbps()))
                .toList();
        return new WanInterfaceSeriesDto(iface.id(), iface.name(), slice);
    }

    private InterfaceDto toInterfaceDto(NetworkInterface iface) {
        return new InterfaceDto(
                iface.id(), iface.name(), iface.type(),
                iface.status().name(),
                iface.speedMbps(),
                store.getCurrentRx(iface.id()),
                store.getCurrentTx(iface.id())
        );
    }

    private Device resolveDevice(String orgId, String siteId, String deviceId) {
        orgRepository.findById(orgId)
                .orElseThrow(() -> ExceptionFactory.resourceNotFound("Organization", orgId));
        siteRepository.findById(siteId)
                .filter(s -> s.orgId().equals(orgId))
                .orElseThrow(() -> ExceptionFactory.resourceNotFound("Site", siteId));
        return deviceRepository.findById(deviceId)
                .filter(d -> d.siteId().equals(siteId))
                .orElseThrow(() -> ExceptionFactory.resourceNotFound("Device", deviceId));
    }
}
