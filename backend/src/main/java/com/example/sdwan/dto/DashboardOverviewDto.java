package com.example.sdwan.dto;

import java.util.Objects;

/**
 * Top-level response for {@code GET /api/dashboard/overview}.
 * Carries all numbers needed to render the four overview cards and two donut charts.
 */
public final class DashboardOverviewDto {

    private final int totalSites;
    private final int totalEdgeDevices;
    private final SiteStatusSummaryDto  siteStatusSummary;
    private final DeviceStatusSummaryDto deviceStatusSummary;

    public DashboardOverviewDto(int totalSites, int totalEdgeDevices,
                                 SiteStatusSummaryDto siteStatusSummary,
                                 DeviceStatusSummaryDto deviceStatusSummary) {
        this.totalSites          = totalSites;
        this.totalEdgeDevices    = totalEdgeDevices;
        this.siteStatusSummary   = Objects.requireNonNull(siteStatusSummary,   "siteStatusSummary");
        this.deviceStatusSummary = Objects.requireNonNull(deviceStatusSummary, "deviceStatusSummary");
    }

    public int                    totalSites()          { return totalSites; }
    public int                    totalEdgeDevices()    { return totalEdgeDevices; }
    public SiteStatusSummaryDto   siteStatusSummary()   { return siteStatusSummary; }
    public DeviceStatusSummaryDto deviceStatusSummary() { return deviceStatusSummary; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DashboardOverviewDto other)) return false;
        return totalSites == other.totalSites
            && totalEdgeDevices == other.totalEdgeDevices
            && Objects.equals(siteStatusSummary, other.siteStatusSummary)
            && Objects.equals(deviceStatusSummary, other.deviceStatusSummary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalSites, totalEdgeDevices, siteStatusSummary, deviceStatusSummary);
    }

    @Override
    public String toString() {
        return "DashboardOverviewDto[totalSites=" + totalSites
            + ", totalEdgeDevices=" + totalEdgeDevices
            + ", siteStatusSummary=" + siteStatusSummary
            + ", deviceStatusSummary=" + deviceStatusSummary + "]";
    }
}
