package com.example.sdwan.service;

import com.example.sdwan.dto.DashboardOverviewDto;
import com.example.sdwan.dto.SiteHealthSnapshotDto;

import java.util.List;

public interface DashboardService {

    /**
     * Returns all numbers needed to render the four overview cards
     * and the two donut charts on the Overview dashboard page.
     */
    DashboardOverviewDto getOverview();

    /**
     * Returns one row per site for the Site Health Snapshot table.
     */
    List<SiteHealthSnapshotDto> getSiteHealthSnapshot();
}
