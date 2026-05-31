package com.example.sdwan.domain;

import java.util.Objects;

public final class NetworkInterface {

    private final String          id;
    private final String          deviceId;
    private final String          name;
    private final InterfaceType   type;
    private final InterfaceStatus status;
    private final String          ipAddress;
    private final long            speedMbps;

    public NetworkInterface(String id, String deviceId, String name,
                            InterfaceType type, InterfaceStatus status,
                            String ipAddress, long speedMbps) {
        this.id        = Objects.requireNonNull(id,        "id");
        this.deviceId  = Objects.requireNonNull(deviceId,  "deviceId");
        this.name      = Objects.requireNonNull(name,      "name");
        this.type      = Objects.requireNonNull(type,      "type");
        this.status    = Objects.requireNonNull(status,    "status");
        this.ipAddress = Objects.requireNonNull(ipAddress, "ipAddress");
        this.speedMbps = speedMbps;
    }

    public String          id()        { return id; }
    public String          deviceId()  { return deviceId; }
    public String          name()      { return name; }
    public InterfaceType   type()      { return type; }
    public InterfaceStatus status()    { return status; }
    public String          ipAddress() { return ipAddress; }
    public long            speedMbps() { return speedMbps; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NetworkInterface other)) return false;
        return speedMbps == other.speedMbps
            && Objects.equals(id, other.id)
            && Objects.equals(deviceId, other.deviceId)
            && Objects.equals(name, other.name)
            && type   == other.type
            && status == other.status
            && Objects.equals(ipAddress, other.ipAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deviceId, name, type, status, ipAddress, speedMbps);
    }

    @Override
    public String toString() {
        return "NetworkInterface[id=" + id + ", deviceId=" + deviceId
            + ", name=" + name + ", type=" + type + ", status=" + status
            + ", ipAddress=" + ipAddress + "]";
    }
}
