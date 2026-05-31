package com.example.sdwan.service;

import com.example.sdwan.dto.OrganizationSummaryDto;

import java.util.List;

public interface OrganizationService {
    List<OrganizationSummaryDto> getAllOrganizations();
    OrganizationSummaryDto getOrganizationById(String id);
}
