package com.example.sdwan.dto;

import com.example.sdwan.domain.DeviceRole;
import com.example.sdwan.domain.DeviceStatus;

import java.util.List;
import java.util.Objects;

public final class DeviceDetailDto {

    private final String             id;
    private final String             siteId;
    private final String             name;
    private final String             model;
    private final DeviceStatus       status;
    private final DeviceRole         role;
    private final String             ipAddress;
    private final String             firmwareVersion;
    private final String             lastSeen;
    private final List<InterfaceDto> interfaces;

    public DeviceDetailDto(String id, String siteId, String name, String model,
                            DeviceStatus status, DeviceRole role, String ipAddress,
                            String firmwareVersion, String lastSeen,
                            List<InterfaceDto> interfaces) {
        this.id              = Objects.requireNonNull(id,              "id");
        this.siteId          = Objects.requireNonNull(siteId,          "siteId");
        this.name            = Objects.requireNonNull(name,            "name");
        this.model           = Objects.requireNonNull(model,           "model");
        this.status          = Objects.requireNonNull(status,          "status");
        this.role            = Objects.requireNonNull(role,            "role");
        this.ipAddress       = Objects.requireNonNull(ipAddress,       "ipAddress");
        this.firmwareVersion = Objects.requireNonNull(firmwareVersion, "firmwareVersion");
        this.lastSeen        = Objects.requireNonNull(lastSeen,        "lastSeen");
        this.interfaces      = List.copyOf(Objects.requireNonNull(interfaces, "interfaces"));
    }

    public String             id()              { return id; }
    public String             siteId()          { return siteId; }
    public String             name()            { return name; }
    public String             model()           { return model; }
    public DeviceStatus       status()          { return status; }
    public DeviceRole         role()            { return role; }
    public String             ipAddress()       { return ipAddress; }
    public String             firmwareVersion() { return firmwareVersion; }
    public String             lastSeen()        { return lastSeen; }
    public List<InterfaceDto> interfaces()      { return interfaces; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceDetailDto other)) return false;
        return Objects.equals(id, other.id)
            && Objects.equals(siteId, other.siteId)
            && Objects.equals(name, other.name)
            && Objects.equals(model, other.model)
            && status == other.status
            && role   == other.role
            && Objects.equals(ipAddress, other.ipAddress)
            && Objects.equals(firmwareVersion, other.firmwareVersion)
            && Objects.equals(lastSeen, other.lastSeen)
            && Objects.equals(interfaces, other.interfaces);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, siteId, name, model, status, role, ipAddress, firmwareVersion, lastSeen, interfaces);
    }

    @Override
    public String toString() {
        return "DeviceDetailDto[id=" + id + ", name=" + name + ", status=" + status
            + ", role=" + role + ", interfaces=" + interfaces.size() + "]";
    }
}
