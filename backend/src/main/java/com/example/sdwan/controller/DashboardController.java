package com.example.sdwan.controller;

import com.example.sdwan.dto.DashboardOverviewDto;
import com.example.sdwan.dto.SiteHealthSnapshotDto;
import com.example.sdwan.response.SuccessResponse;
import com.example.sdwan.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Aggregated endpoints for the Overview dashboard page")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    @Operation(
        summary = "Dashboard overview",
        description = "Returns total sites, total edge devices, site status breakdown, " +
                      "and device status breakdown. Drives the four summary cards and two donut charts."
    )
    public ResponseEntity<SuccessResponse<DashboardOverviewDto>> getOverview() {
        return ResponseEntity.ok(SuccessResponse.of(dashboardService.getOverview()));
    }

    @GetMapping("/site-health")
    @Operation(
        summary = "Site health snapshot",
        description = "Returns one row per site with name, health status, and device counts. " +
                      "Drives the Site Health Snapshot table."
    )
    public ResponseEntity<SuccessResponse<List<SiteHealthSnapshotDto>>> getSiteHealthSnapshot() {
        return ResponseEntity.ok(SuccessResponse.of(dashboardService.getSiteHealthSnapshot()));
    }
}
