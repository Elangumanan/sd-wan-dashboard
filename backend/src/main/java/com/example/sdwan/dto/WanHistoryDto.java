package com.example.sdwan.dto;

import java.util.List;
import java.util.Objects;

public final class WanHistoryDto {

    private final String                     deviceId;
    private final String                     deviceName;
    private final String                     range;
    private final List<WanInterfaceSeriesDto> interfaces;

    public WanHistoryDto(String deviceId, String deviceName, String range,
                          List<WanInterfaceSeriesDto> interfaces) {
        this.deviceId   = Objects.requireNonNull(deviceId,   "deviceId");
        this.deviceName = Objects.requireNonNull(deviceName, "deviceName");
        this.range      = Objects.requireNonNull(range,      "range");
        this.interfaces = List.copyOf(Objects.requireNonNull(interfaces, "interfaces"));
    }

    public String                     deviceId()   { return deviceId; }
    public String                     deviceName() { return deviceName; }
    public String                     range()      { return range; }
    public List<WanInterfaceSeriesDto> interfaces() { return interfaces; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WanHistoryDto other)) return false;
        return Objects.equals(deviceId, other.deviceId)
            && Objects.equals(deviceName, other.deviceName)
            && Objects.equals(range, other.range)
            && Objects.equals(interfaces, other.interfaces);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, deviceName, range, interfaces);
    }

    @Override
    public String toString() {
        return "WanHistoryDto[deviceId=" + deviceId + ", range=" + range
            + ", interfaces=" + interfaces.size() + "]";
    }
}
