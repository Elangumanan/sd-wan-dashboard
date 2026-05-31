package com.example.sdwan.service.impl;

import com.example.sdwan.domain.Device;
import com.example.sdwan.domain.DeviceStatus;
import com.example.sdwan.domain.Site;
import com.example.sdwan.domain.SiteHealth;
import com.example.sdwan.dto.*;
import com.example.sdwan.repository.DeviceRepository;
import com.example.sdwan.repository.SiteRepository;
import com.example.sdwan.service.DashboardService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Aggregation service for the Overview dashboard page.
 * Computes all required UI-ready data in a single service call,
 * avoiding multiple round-trips from the frontend to domain APIs.
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private final SiteRepository   siteRepository;
    private final DeviceRepository deviceRepository;

    public DashboardServiceImpl(SiteRepository siteRepository, DeviceRepository deviceRepository) {
        this.siteRepository   = siteRepository;
        this.deviceRepository = deviceRepository;
    }

    /**
     * Computes the full overview by building the site health snapshot first
     * and aggregating from it — avoids iterating all sites twice.
     */
    @Override
    public DashboardOverviewDto getOverview() {
        List<SiteHealthSnapshotDto> snapshots = getSiteHealthSnapshot();

        int healthy  = 0, degraded = 0, down = 0;
        int online   = 0, offline  = 0;

        for (SiteHealthSnapshotDto s : snapshots) {
            switch (s.healthStatus()) {
                case HEALTHY  -> healthy++;
                case DEGRADED -> degraded++;
                case DOWN     -> down++;
            }
            online  += s.onlineDevices();
            offline += s.offlineDevices();
        }

        return new DashboardOverviewDto(
                snapshots.size(),
                online + offline,
                new SiteStatusSummaryDto(healthy, degraded, down),
                new DeviceStatusSummaryDto(online, offline)
        );
    }

    @Override
    public List<SiteHealthSnapshotDto> getSiteHealthSnapshot() {
        return siteRepository.findAll().stream()
                .map(this::toSnapshot)
                .toList();
    }

    private SiteHealthSnapshotDto toSnapshot(Site site) {
        List<Device> devices = deviceRepository.findBySiteId(site.id());
        int online  = (int) devices.stream().filter(d -> d.status() == DeviceStatus.ONLINE).count();
        int offline = devices.size() - online;
        return new SiteHealthSnapshotDto(
                site.id(),
                site.name(),
                computeHealth(online, offline),
                devices.size(),
                online,
                offline
        );
    }

    private static SiteHealth computeHealth(int online, int offline) {
        if (offline == 0) return SiteHealth.HEALTHY;
        if (online  == 0) return SiteHealth.DOWN;
        return SiteHealth.DEGRADED;
    }
}
