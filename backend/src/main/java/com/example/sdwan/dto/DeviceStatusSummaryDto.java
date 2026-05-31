package com.example.sdwan.dto;

import java.util.Objects;

/**
 * Breakdown of edge devices by status — drives the Devices donut chart.
 * {@code total} is derived from online + offline.
 */
public final class DeviceStatusSummaryDto {

    private final int online;
    private final int offline;
    private final int total;

    public DeviceStatusSummaryDto(int online, int offline) {
        this.online  = online;
        this.offline = offline;
        this.total   = online + offline;
    }

    public int online()  { return online; }
    public int offline() { return offline; }
    public int total()   { return total; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceStatusSummaryDto other)) return false;
        return online == other.online && offline == other.offline;
    }

    @Override
    public int hashCode() {
        return Objects.hash(online, offline);
    }

    @Override
    public String toString() {
        return "DeviceStatusSummaryDto[online=" + online + ", offline=" + offline
            + ", total=" + total + "]";
    }
}
