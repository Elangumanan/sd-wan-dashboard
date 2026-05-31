package com.example.sdwan.dto;

import java.util.Objects;

/**
 * Breakdown of sites by health status — drives the Sites donut chart.
 * {@code total} is derived from the three counts.
 */
public final class SiteStatusSummaryDto {

    private final int healthy;
    private final int degraded;
    private final int down;
    private final int total;

    public SiteStatusSummaryDto(int healthy, int degraded, int down) {
        this.healthy  = healthy;
        this.degraded = degraded;
        this.down     = down;
        this.total    = healthy + degraded + down;
    }

    public int healthy()  { return healthy; }
    public int degraded() { return degraded; }
    public int down()     { return down; }
    public int total()    { return total; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SiteStatusSummaryDto other)) return false;
        return healthy == other.healthy
            && degraded == other.degraded
            && down == other.down;
    }

    @Override
    public int hashCode() {
        return Objects.hash(healthy, degraded, down);
    }

    @Override
    public String toString() {
        return "SiteStatusSummaryDto[healthy=" + healthy + ", degraded=" + degraded
            + ", down=" + down + ", total=" + total + "]";
    }
}
