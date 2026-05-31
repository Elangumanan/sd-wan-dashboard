package com.example.sdwan.controller;

import com.example.sdwan.domain.SiteHealth;
import com.example.sdwan.dto.*;
import com.example.sdwan.exception.GlobalExceptionHandler;
import com.example.sdwan.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@Import(GlobalExceptionHandler.class)
class DashboardControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean  DashboardService dashboardService;

    // ── GET /api/dashboard/overview ───────────────────────────────────────────

    @Test
    void getOverview_returns200WithAllFields() throws Exception {
        var overview = new DashboardOverviewDto(
                4, 12,
                new SiteStatusSummaryDto(2, 1, 1),
                new DeviceStatusSummaryDto(10, 2)
        );
        when(dashboardService.getOverview()).thenReturn(overview);

        mockMvc.perform(get("/api/dashboard/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value.totalSites").value(4))
                .andExpect(jsonPath("$.value.totalEdgeDevices").value(12))
                .andExpect(jsonPath("$.value.siteStatusSummary.healthy").value(2))
                .andExpect(jsonPath("$.value.siteStatusSummary.degraded").value(1))
                .andExpect(jsonPath("$.value.siteStatusSummary.down").value(1))
                .andExpect(jsonPath("$.value.siteStatusSummary.total").value(4))
                .andExpect(jsonPath("$.value.deviceStatusSummary.online").value(10))
                .andExpect(jsonPath("$.value.deviceStatusSummary.offline").value(2))
                .andExpect(jsonPath("$.value.deviceStatusSummary.total").value(12));
    }

    // ── GET /api/dashboard/site-health ────────────────────────────────────────

    @Test
    void getSiteHealthSnapshot_returns200WithTableRows() throws Exception {
        var rows = List.of(
                new SiteHealthSnapshotDto("site-001", "New York HQ",        SiteHealth.HEALTHY,  3, 3, 0),
                new SiteHealthSnapshotDto("site-002", "Los Angeles Branch", SiteHealth.DEGRADED, 3, 2, 1),
                new SiteHealthSnapshotDto("site-003", "Chicago Office",     SiteHealth.DOWN,     2, 0, 2)
        );
        when(dashboardService.getSiteHealthSnapshot()).thenReturn(rows);

        mockMvc.perform(get("/api/dashboard/site-health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").isArray())
                .andExpect(jsonPath("$.value.length()").value(3))
                .andExpect(jsonPath("$.value[0].siteId").value("site-001"))
                .andExpect(jsonPath("$.value[0].siteName").value("New York HQ"))
                .andExpect(jsonPath("$.value[0].healthStatus").value("HEALTHY"))
                .andExpect(jsonPath("$.value[0].totalDevices").value(3))
                .andExpect(jsonPath("$.value[0].onlineDevices").value(3))
                .andExpect(jsonPath("$.value[0].offlineDevices").value(0))
                .andExpect(jsonPath("$.value[1].healthStatus").value("DEGRADED"))
                .andExpect(jsonPath("$.value[2].healthStatus").value("DOWN"));
    }

    @Test
    void getSiteHealthSnapshot_returnsEmptyList_whenNoSites() throws Exception {
        when(dashboardService.getSiteHealthSnapshot()).thenReturn(List.of());

        mockMvc.perform(get("/api/dashboard/site-health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").isArray())
                .andExpect(jsonPath("$.value.length()").value(0));
    }
}
