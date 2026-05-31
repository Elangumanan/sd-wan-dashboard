package com.example.sdwan.dto;

import com.example.sdwan.domain.SiteHealth;

import java.util.Objects;

/**
 * One row in the Site Health Snapshot table on the Overview dashboard.
 */
public final class SiteHealthSnapshotDto {

    private final String     siteId;
    private final String     siteName;
    private final SiteHealth healthStatus;
    private final int        totalDevices;
    private final int        onlineDevices;
    private final int        offlineDevices;

    public SiteHealthSnapshotDto(String siteId, String siteName, SiteHealth healthStatus,
                                  int totalDevices, int onlineDevices, int offlineDevices) {
        this.siteId         = Objects.requireNonNull(siteId,    "siteId");
        this.siteName       = Objects.requireNonNull(siteName,  "siteName");
        this.healthStatus   = Objects.requireNonNull(healthStatus, "healthStatus");
        this.totalDevices   = totalDevices;
        this.onlineDevices  = onlineDevices;
        this.offlineDevices = offlineDevices;
    }

    public String     siteId()         { return siteId; }
    public String     siteName()       { return siteName; }
    public SiteHealth healthStatus()   { return healthStatus; }
    public int        totalDevices()   { return totalDevices; }
    public int        onlineDevices()  { return onlineDevices; }
    public int        offlineDevices() { return offlineDevices; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SiteHealthSnapshotDto other)) return false;
        return totalDevices == other.totalDevices
            && onlineDevices == other.onlineDevices
            && offlineDevices == other.offlineDevices
            && Objects.equals(siteId, other.siteId)
            && Objects.equals(siteName, other.siteName)
            && healthStatus == other.healthStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(siteId, siteName, healthStatus, totalDevices, onlineDevices, offlineDevices);
    }

    @Override
    public String toString() {
        return "SiteHealthSnapshotDto[siteId=" + siteId + ", siteName=" + siteName
            + ", healthStatus=" + healthStatus + ", totalDevices=" + totalDevices + "]";
    }
}
