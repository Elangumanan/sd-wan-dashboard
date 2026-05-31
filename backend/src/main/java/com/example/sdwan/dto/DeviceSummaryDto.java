package com.example.sdwan.dto;

import com.example.sdwan.domain.DeviceStatus;

import java.util.Objects;

public final class DeviceSummaryDto {

    private final String       id;
    private final String       siteId;
    private final String       name;
    private final String       model;
    private final DeviceStatus status;
    private final String       ipAddress;

    public DeviceSummaryDto(String id, String siteId, String name, String model,
                             DeviceStatus status, String ipAddress) {
        this.id        = Objects.requireNonNull(id,        "id");
        this.siteId    = Objects.requireNonNull(siteId,    "siteId");
        this.name      = Objects.requireNonNull(name,      "name");
        this.model     = Objects.requireNonNull(model,     "model");
        this.status    = Objects.requireNonNull(status,    "status");
        this.ipAddress = Objects.requireNonNull(ipAddress, "ipAddress");
    }

    public String       id()        { return id; }
    public String       siteId()    { return siteId; }
    public String       name()      { return name; }
    public String       model()     { return model; }
    public DeviceStatus status()    { return status; }
    public String       ipAddress() { return ipAddress; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceSummaryDto other)) return false;
        return Objects.equals(id, other.id)
            && Objects.equals(siteId, other.siteId)
            && Objects.equals(name, other.name)
            && Objects.equals(model, other.model)
            && status == other.status
            && Objects.equals(ipAddress, other.ipAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, siteId, name, model, status, ipAddress);
    }

    @Override
    public String toString() {
        return "DeviceSummaryDto[id=" + id + ", name=" + name + ", status=" + status + "]";
    }
}
