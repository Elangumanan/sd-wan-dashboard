package com.example.sdwan.service;

import com.example.sdwan.domain.Organization;
import com.example.sdwan.domain.SiteHealth;
import com.example.sdwan.dto.OrganizationSummaryDto;
import com.example.sdwan.dto.SiteListItemDto;
import com.example.sdwan.exception.ResourceNotFoundException;
import com.example.sdwan.repository.OrganizationRepository;
import com.example.sdwan.service.impl.OrganizationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock OrganizationRepository orgRepo;
    @Mock SiteService siteService;

    OrganizationService orgService;

    private static final Organization ORG = new Organization("org-001", "Acme", "desc", "NA");

    @BeforeEach
    void setUp() {
        orgService = new OrganizationServiceImpl(orgRepo, siteService);
    }

    @Test
    void getAllOrganizations_aggregatesSiteHealth() {
        when(orgRepo.findAll()).thenReturn(List.of(ORG));
        when(siteService.getSitesByOrgId("org-001")).thenReturn(List.of(
                siteItem("s1", SiteHealth.HEALTHY),
                siteItem("s2", SiteHealth.DEGRADED),
                siteItem("s3", SiteHealth.DOWN)
        ));

        List<OrganizationSummaryDto> result = orgService.getAllOrganizations();

        assertThat(result).hasSize(1);
        OrganizationSummaryDto dto = result.get(0);
        assertThat(dto.totalSites()).isEqualTo(3);
        assertThat(dto.healthySites()).isEqualTo(1);
        assertThat(dto.degradedSites()).isEqualTo(1);
        assertThat(dto.downSites()).isEqualTo(1);
    }

    @Test
    void getOrganizationById_throwsWhenNotFound() {
        when(orgRepo.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orgService.getOrganizationById("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getOrganizationById_returnsCorrectOrg() {
        when(orgRepo.findById("org-001")).thenReturn(Optional.of(ORG));
        when(siteService.getSitesByOrgId("org-001")).thenReturn(List.of());

        OrganizationSummaryDto dto = orgService.getOrganizationById("org-001");
        assertThat(dto.id()).isEqualTo("org-001");
        assertThat(dto.name()).isEqualTo("Acme");
        assertThat(dto.totalSites()).isEqualTo(0);
    }

    private SiteListItemDto siteItem(String id, SiteHealth health) {
        return new SiteListItemDto(id, "org-001", "Site", "Location", health, 2, 1, 1);
    }
}
