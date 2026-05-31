package com.example.sdwan.service;

import com.example.sdwan.domain.*;
import com.example.sdwan.dto.DashboardOverviewDto;
import com.example.sdwan.dto.SiteHealthSnapshotDto;
import com.example.sdwan.repository.DeviceRepository;
import com.example.sdwan.repository.SiteRepository;
import com.example.sdwan.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock SiteRepository   siteRepository;
    @Mock DeviceRepository deviceRepository;

    DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardServiceImpl(siteRepository, deviceRepository);
    }

    // ── getSiteHealthSnapshot ────────────────────────────────────────────────

    @Test
    void getSiteHealthSnapshot_allOnline_isHealthy() {
        when(siteRepository.findAll()).thenReturn(List.of(site("s1", "HQ")));
        when(deviceRepository.findBySiteId("s1")).thenReturn(List.of(
                device("d1", "s1", DeviceStatus.ONLINE),
                device("d2", "s1", DeviceStatus.ONLINE)
        ));

        var result = dashboardService.getSiteHealthSnapshot();

        assertThat(result).hasSize(1);
        var row = result.get(0);
        assertThat(row.healthStatus()).isEqualTo(SiteHealth.HEALTHY);
        assertThat(row.totalDevices()).isEqualTo(2);
        assertThat(row.onlineDevices()).isEqualTo(2);
        assertThat(row.offlineDevices()).isEqualTo(0);
        assertThat(row.siteName()).isEqualTo("HQ");
    }

    @Test
    void getSiteHealthSnapshot_mixedStatus_isDegraded() {
        when(siteRepository.findAll()).thenReturn(List.of(site("s1", "Branch")));
        when(deviceRepository.findBySiteId("s1")).thenReturn(List.of(
                device("d1", "s1", DeviceStatus.ONLINE),
                device("d2", "s1", DeviceStatus.OFFLINE)
        ));

        var result = dashboardService.getSiteHealthSnapshot();

        assertThat(result.get(0).healthStatus()).isEqualTo(SiteHealth.DEGRADED);
        assertThat(result.get(0).onlineDevices()).isEqualTo(1);
        assertThat(result.get(0).offlineDevices()).isEqualTo(1);
    }

    @Test
    void getSiteHealthSnapshot_allOffline_isDown() {
        when(siteRepository.findAll()).thenReturn(List.of(site("s1", "Offline")));
        when(deviceRepository.findBySiteId("s1")).thenReturn(List.of(
                device("d1", "s1", DeviceStatus.OFFLINE),
                device("d2", "s1", DeviceStatus.OFFLINE)
        ));

        var result = dashboardService.getSiteHealthSnapshot();

        assertThat(result.get(0).healthStatus()).isEqualTo(SiteHealth.DOWN);
    }

    @Test
    void getSiteHealthSnapshot_noDevices_isHealthy() {
        when(siteRepository.findAll()).thenReturn(List.of(site("s1", "Empty")));
        when(deviceRepository.findBySiteId("s1")).thenReturn(List.of());

        var result = dashboardService.getSiteHealthSnapshot();

        assertThat(result.get(0).healthStatus()).isEqualTo(SiteHealth.HEALTHY);
        assertThat(result.get(0).totalDevices()).isEqualTo(0);
    }

    // ── getOverview ──────────────────────────────────────────────────────────

    @Test
    void getOverview_aggregatesCountsCorrectly() {
        when(siteRepository.findAll()).thenReturn(List.of(
                site("s1", "HQ"),
                site("s2", "Branch"),
                site("s3", "Office"),
                site("s4", "DC")
        ));
        // s1: 2 online → HEALTHY
        when(deviceRepository.findBySiteId("s1")).thenReturn(List.of(
                device("d1", "s1", DeviceStatus.ONLINE),
                device("d2", "s1", DeviceStatus.ONLINE)
        ));
        // s2: 1 online + 1 offline → DEGRADED
        when(deviceRepository.findBySiteId("s2")).thenReturn(List.of(
                device("d3", "s2", DeviceStatus.ONLINE),
                device("d4", "s2", DeviceStatus.OFFLINE)
        ));
        // s3: 2 offline → DOWN
        when(deviceRepository.findBySiteId("s3")).thenReturn(List.of(
                device("d5", "s3", DeviceStatus.OFFLINE),
                device("d6", "s3", DeviceStatus.OFFLINE)
        ));
        // s4: 3 online → HEALTHY
        when(deviceRepository.findBySiteId("s4")).thenReturn(List.of(
                device("d7", "s4", DeviceStatus.ONLINE),
                device("d8", "s4", DeviceStatus.ONLINE),
                device("d9", "s4", DeviceStatus.ONLINE)
        ));

        DashboardOverviewDto overview = dashboardService.getOverview();

        assertThat(overview.totalSites()).isEqualTo(4);
        assertThat(overview.totalEdgeDevices()).isEqualTo(9);

        assertThat(overview.siteStatusSummary().healthy()).isEqualTo(2);
        assertThat(overview.siteStatusSummary().degraded()).isEqualTo(1);
        assertThat(overview.siteStatusSummary().down()).isEqualTo(1);
        assertThat(overview.siteStatusSummary().total()).isEqualTo(4);

        assertThat(overview.deviceStatusSummary().online()).isEqualTo(6);
        assertThat(overview.deviceStatusSummary().offline()).isEqualTo(3);
        assertThat(overview.deviceStatusSummary().total()).isEqualTo(9);
    }

    @Test
    void getOverview_emptyCatalogue_returnsZeroes() {
        when(siteRepository.findAll()).thenReturn(List.of());

        DashboardOverviewDto overview = dashboardService.getOverview();

        assertThat(overview.totalSites()).isEqualTo(0);
        assertThat(overview.totalEdgeDevices()).isEqualTo(0);
        assertThat(overview.siteStatusSummary().total()).isEqualTo(0);
        assertThat(overview.deviceStatusSummary().total()).isEqualTo(0);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static Site site(String id, String name) {
        return new Site(id, "org-001", name, "Location");
    }

    private static Device device(String id, String siteId, DeviceStatus status) {
        return new Device(id, siteId, id, "Model", status, "10.0.0.1", "v1", Instant.now());
    }
}
