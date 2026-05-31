package com.example.sdwan.dto;

import com.example.sdwan.domain.InterfaceType;

import java.util.Objects;

public final class InterfaceDto {

    private final String        id;
    private final String        name;
    private final InterfaceType type;
    private final String        status;
    private final long          speedMbps;
    private final double        currentRxMbps;
    private final double        currentTxMbps;

    public InterfaceDto(String id, String name, InterfaceType type, String status,
                         long speedMbps, double currentRxMbps, double currentTxMbps) {
        this.id            = Objects.requireNonNull(id,     "id");
        this.name          = Objects.requireNonNull(name,   "name");
        this.type          = Objects.requireNonNull(type,   "type");
        this.status        = Objects.requireNonNull(status, "status");
        this.speedMbps     = speedMbps;
        this.currentRxMbps = currentRxMbps;
        this.currentTxMbps = currentTxMbps;
    }

    public String        id()            { return id; }
    public String        name()          { return name; }
    public InterfaceType type()          { return type; }
    public String        status()        { return status; }
    public long          speedMbps()     { return speedMbps; }
    public double        currentRxMbps() { return currentRxMbps; }
    public double        currentTxMbps() { return currentTxMbps; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InterfaceDto other)) return false;
        return speedMbps == other.speedMbps
            && Double.compare(currentRxMbps, other.currentRxMbps) == 0
            && Double.compare(currentTxMbps, other.currentTxMbps) == 0
            && Objects.equals(id, other.id)
            && Objects.equals(name, other.name)
            && type == other.type
            && Objects.equals(status, other.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, status, speedMbps, currentRxMbps, currentTxMbps);
    }

    @Override
    public String toString() {
        return "InterfaceDto[id=" + id + ", name=" + name + ", type=" + type
            + ", status=" + status + ", speedMbps=" + speedMbps + "]";
    }
}
