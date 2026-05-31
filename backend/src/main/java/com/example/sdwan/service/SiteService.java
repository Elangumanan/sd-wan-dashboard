package com.example.sdwan.service;

import com.example.sdwan.dto.SiteDetailDto;
import com.example.sdwan.dto.SiteListItemDto;

import java.util.List;

public interface SiteService {
    List<SiteListItemDto> getSitesByOrgId(String orgId);
    SiteDetailDto getSiteById(String orgId, String siteId);
}
