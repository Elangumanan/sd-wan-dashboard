package com.example.sdwan.service.impl;

import com.example.sdwan.domain.Organization;
import com.example.sdwan.domain.SiteHealth;
import com.example.sdwan.dto.OrganizationSummaryDto;
import com.example.sdwan.dto.SiteListItemDto;
import com.example.sdwan.exception.ExceptionFactory;
import com.example.sdwan.repository.OrganizationRepository;
import com.example.sdwan.service.OrganizationService;
import com.example.sdwan.service.SiteService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final SiteService siteService;

    public OrganizationServiceImpl(OrganizationRepository organizationRepository, SiteService siteService) {
        this.organizationRepository = organizationRepository;
        this.siteService = siteService;
    }

    @Override
    public List<OrganizationSummaryDto> getAllOrganizations() {
        return organizationRepository.findAll().stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    public OrganizationSummaryDto getOrganizationById(String id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> ExceptionFactory.resourceNotFound("Organization", id));
        return toSummary(org);
    }

    private OrganizationSummaryDto toSummary(Organization org) {
        List<SiteListItemDto> sites = siteService.getSitesByOrgId(org.id());
        int healthy  = (int) sites.stream().filter(s -> s.health() == SiteHealth.HEALTHY).count();
        int degraded = (int) sites.stream().filter(s -> s.health() == SiteHealth.DEGRADED).count();
        int down     = (int) sites.stream().filter(s -> s.health() == SiteHealth.DOWN).count();
        return new OrganizationSummaryDto(
                org.id(), org.name(), org.description(), org.region(),
                sites.size(), healthy, degraded, down
        );
    }
}
