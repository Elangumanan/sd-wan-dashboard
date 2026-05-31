package com.example.sdwan.service;

import com.example.sdwan.domain.*;
import com.example.sdwan.dto.SiteDetailDto;
import com.example.sdwan.dto.SiteListItemDto;
import com.example.sdwan.exception.ResourceNotFoundException;
import com.example.sdwan.repository.DeviceRepository;
import com.example.sdwan.repository.OrganizationRepository;
import com.example.sdwan.repository.SiteRepository;
import com.example.sdwan.service.impl.SiteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SiteServiceTest {

    @Mock OrganizationRepository orgRepo;
    @Mock SiteRepository         siteRepo;
    @Mock DeviceRepository       deviceRepo;

    SiteService siteService;

    private static final Organization ORG  = new Organization("org-001", "Acme", "desc", "NA");
    private static final Site         SITE = new Site("site-001", "org-001", "HQ", "NY");

    @BeforeEach
    void setUp() {
        siteService = new SiteServiceImpl(orgRepo, siteRepo, deviceRepo);
    }

    // ── getSitesByOrgId ──────────────────────────────────────────────────────

    @Test
    void getSitesByOrgId_returnsListWithCorrectHealth() {
        when(orgRepo.findById("org-001")).thenReturn(Optional.of(ORG));
        when(siteRepo.findByOrgId("org-001")).thenReturn(List.of(SITE));
        when(deviceRepo.findBySiteId("site-001")).thenReturn(List.of(
                device("d1", DeviceStatus.ONLINE),
                device("d2", DeviceStatus.ONLINE)
        ));

        List<SiteListItemDto> result = siteService.getSitesByOrgId("org-001");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).health()).isEqualTo(SiteHealth.HEALTHY);
        assertThat(result.get(0).onlineDeviceCount()).isEqualTo(2);
        assertThat(result.get(0).offlineDeviceCount()).isEqualTo(0);
    }

    @Test
    void getSitesByOrgId_throwsWhenOrgNotFound() {
        when(orgRepo.findById("bad")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> siteService.getSitesByOrgId("bad"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── health aggregation rules ─────────────────────────────────────────────

    @Test
    void computeHealth_allOnline_isHealthy() {
        assertThat(SiteServiceImpl.computeHealth(3, 0)).isEqualTo(SiteHealth.HEALTHY);
    }

    @Test
    void computeHealth_someOffline_isDegraded() {
        assertThat(SiteServiceImpl.computeHealth(2, 1)).isEqualTo(SiteHealth.DEGRADED);
    }

    @Test
    void computeHealth_allOffline_isDown() {
        assertThat(SiteServiceImpl.computeHealth(0, 2)).isEqualTo(SiteHealth.DOWN);
    }

    // ── getSiteById ──────────────────────────────────────────────────────────

    @Test
    void getSiteById_returnsSiteDetailWithDevices() {
        when(orgRepo.findById("org-001")).thenReturn(Optional.of(ORG));
        when(siteRepo.findById("site-001")).thenReturn(Optional.of(SITE));
        when(deviceRepo.findBySiteId("site-001")).thenReturn(List.of(
                device("d1", DeviceStatus.ONLINE),
                device("d2", DeviceStatus.OFFLINE)
        ));

        SiteDetailDto dto = siteService.getSiteById("org-001", "site-001");

        assertThat(dto.health()).isEqualTo(SiteHealth.DEGRADED);
        assertThat(dto.devices()).hasSize(2);
        assertThat(dto.onlineDeviceCount()).isEqualTo(1);
        assertThat(dto.offlineDeviceCount()).isEqualTo(1);
    }

    @Test
    void getSiteById_throwsWhenSiteBelongsToDifferentOrg() {
        when(orgRepo.findById("org-001")).thenReturn(Optional.of(ORG));
        Site otherOrgSite = new Site("site-001", "org-999", "Other", "LA");
        when(siteRepo.findById("site-001")).thenReturn(Optional.of(otherOrgSite));

        assertThatThrownBy(() -> siteService.getSiteById("org-001", "site-001"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── helper ───────────────────────────────────────────────────────────────

    private Device device(String id, DeviceStatus status) {
        return new Device(id, "site-001", id, "Model", status, "10.0.0.1", "v1", Instant.now());
    }
}
