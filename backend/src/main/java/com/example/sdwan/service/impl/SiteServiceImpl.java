package com.example.sdwan.service.impl;

import com.example.sdwan.domain.Device;
import com.example.sdwan.domain.DeviceStatus;
import com.example.sdwan.domain.Site;
import com.example.sdwan.domain.SiteHealth;
import com.example.sdwan.dto.DeviceSummaryDto;
import com.example.sdwan.dto.SiteDetailDto;
import com.example.sdwan.dto.SiteListItemDto;
import com.example.sdwan.exception.ExceptionFactory;
import com.example.sdwan.repository.DeviceRepository;
import com.example.sdwan.repository.OrganizationRepository;
import com.example.sdwan.repository.SiteRepository;
import com.example.sdwan.service.SiteService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SiteServiceImpl implements SiteService {

    private final OrganizationRepository organizationRepository;
    private final SiteRepository siteRepository;
    private final DeviceRepository deviceRepository;

    public SiteServiceImpl(OrganizationRepository organizationRepository,
                           SiteRepository siteRepository,
                           DeviceRepository deviceRepository) {
        this.organizationRepository = organizationRepository;
        this.siteRepository = siteRepository;
        this.deviceRepository = deviceRepository;
    }

    @Override
    public List<SiteListItemDto> getSitesByOrgId(String orgId) {
        verifyOrgExists(orgId);
        return siteRepository.findByOrgId(orgId).stream()
                .map(this::toListItem)
                .toList();
    }

    @Override
    public SiteDetailDto getSiteById(String orgId, String siteId) {
        verifyOrgExists(orgId);
        Site site = siteRepository.findById(siteId)
                .filter(s -> s.orgId().equals(orgId))
                .orElseThrow(() -> ExceptionFactory.resourceNotFound("Site", siteId));

        List<Device> devices = deviceRepository.findBySiteId(siteId);
        List<DeviceSummaryDto> deviceSummaries = devices.stream()
                .map(this::toDeviceSummary)
                .toList();

        int online  = (int) devices.stream().filter(d -> d.status() == DeviceStatus.ONLINE).count();
        int offline = devices.size() - online;

        return new SiteDetailDto(
                site.id(), site.orgId(), site.name(), site.location(),
                computeHealth(online, offline),
                devices.size(), online, offline,
                deviceSummaries
        );
    }

    private SiteListItemDto toListItem(Site site) {
        List<Device> devices = deviceRepository.findBySiteId(site.id());
        int online  = (int) devices.stream().filter(d -> d.status() == DeviceStatus.ONLINE).count();
        int offline = devices.size() - online;
        return new SiteListItemDto(
                site.id(), site.orgId(), site.name(), site.location(),
                computeHealth(online, offline),
                devices.size(), online, offline
        );
    }

    private DeviceSummaryDto toDeviceSummary(Device d) {
        return new DeviceSummaryDto(d.id(), d.siteId(), d.name(), d.model(), d.status(), d.role(), d.uptime(), d.ipAddress());
    }

    private void verifyOrgExists(String orgId) {
        organizationRepository.findById(orgId)
                .orElseThrow(() -> ExceptionFactory.resourceNotFound("Organization", orgId));
    }

    public static SiteHealth computeHealth(int online, int offline) {
        if (offline == 0)  return SiteHealth.HEALTHY;
        if (online  == 0)  return SiteHealth.DOWN;
        return SiteHealth.DEGRADED;
    }
}
