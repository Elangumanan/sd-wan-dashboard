package com.example.sdwan.dto;

import com.example.sdwan.domain.SiteHealth;

import java.util.Objects;

public final class SiteListItemDto {

    private final String     id;
    private final String     orgId;
    private final String     name;
    private final String     location;
    private final SiteHealth health;
    private final int        deviceCount;
    private final int        onlineDeviceCount;
    private final int        offlineDeviceCount;

    public SiteListItemDto(String id, String orgId, String name, String location,
                            SiteHealth health, int deviceCount,
                            int onlineDeviceCount, int offlineDeviceCount) {
        this.id                 = Objects.requireNonNull(id,       "id");
        this.orgId              = Objects.requireNonNull(orgId,    "orgId");
        this.name               = Objects.requireNonNull(name,     "name");
        this.location           = Objects.requireNonNull(location, "location");
        this.health             = Objects.requireNonNull(health,   "health");
        this.deviceCount        = deviceCount;
        this.onlineDeviceCount  = onlineDeviceCount;
        this.offlineDeviceCount = offlineDeviceCount;
    }

    public String     id()                 { return id; }
    public String     orgId()              { return orgId; }
    public String     name()               { return name; }
    public String     location()           { return location; }
    public SiteHealth health()             { return health; }
    public int        deviceCount()        { return deviceCount; }
    public int        onlineDeviceCount()  { return onlineDeviceCount; }
    public int        offlineDeviceCount() { return offlineDeviceCount; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SiteListItemDto other)) return false;
        return deviceCount == other.deviceCount
            && onlineDeviceCount == other.onlineDeviceCount
            && offlineDeviceCount == other.offlineDeviceCount
            && Objects.equals(id, other.id)
            && Objects.equals(orgId, other.orgId)
            && Objects.equals(name, other.name)
            && Objects.equals(location, other.location)
            && health == other.health;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orgId, name, location, health, deviceCount, onlineDeviceCount, offlineDeviceCount);
    }

    @Override
    public String toString() {
        return "SiteListItemDto[id=" + id + ", name=" + name + ", health=" + health
            + ", deviceCount=" + deviceCount + "]";
    }
}
