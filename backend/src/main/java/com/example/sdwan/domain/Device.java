package com.example.sdwan.domain;

import java.time.Instant;
import java.util.Objects;

public final class Device {

    private final String       id;
    private final String       siteId;
    private final String       name;
    private final String       model;
    private final DeviceStatus status;
    private final DeviceRole   role;
    private final String       uptime;
    private final String       ipAddress;
    private final String       firmwareVersion;
    private final Instant      lastSeen;

    public Device(String id, String siteId, String name, String model,
                  DeviceStatus status, DeviceRole role, String uptime,
                  String ipAddress, String firmwareVersion, Instant lastSeen) {
        this.id              = Objects.requireNonNull(id,              "id");
        this.siteId          = Objects.requireNonNull(siteId,          "siteId");
        this.name            = Objects.requireNonNull(name,            "name");
        this.model           = Objects.requireNonNull(model,           "model");
        this.status          = Objects.requireNonNull(status,          "status");
        this.role            = Objects.requireNonNull(role,            "role");
        this.uptime          = Objects.requireNonNull(uptime,          "uptime");
        this.ipAddress       = Objects.requireNonNull(ipAddress,       "ipAddress");
        this.firmwareVersion = Objects.requireNonNull(firmwareVersion, "firmwareVersion");
        this.lastSeen        = Objects.requireNonNull(lastSeen,        "lastSeen");
    }

    public String       id()              { return id; }
    public String       siteId()          { return siteId; }
    public String       name()            { return name; }
    public String       model()           { return model; }
    public DeviceStatus status()          { return status; }
    public DeviceRole   role()            { return role; }
    public String       uptime()          { return uptime; }
    public String       ipAddress()       { return ipAddress; }
    public String       firmwareVersion() { return firmwareVersion; }
    public Instant      lastSeen()        { return lastSeen; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Device other)) return false;
        return Objects.equals(id, other.id)
            && Objects.equals(siteId, other.siteId)
            && Objects.equals(name, other.name)
            && Objects.equals(model, other.model)
            && status == other.status
            && role   == other.role
            && Objects.equals(uptime, other.uptime)
            && Objects.equals(ipAddress, other.ipAddress)
            && Objects.equals(firmwareVersion, other.firmwareVersion)
            && Objects.equals(lastSeen, other.lastSeen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, siteId, name, model, status, role, uptime, ipAddress, firmwareVersion, lastSeen);
    }

    @Override
    public String toString() {
        return "Device[id=" + id + ", siteId=" + siteId + ", name=" + name
            + ", status=" + status + ", role=" + role + ", uptime=" + uptime
            + ", ipAddress=" + ipAddress + "]";
    }
}
