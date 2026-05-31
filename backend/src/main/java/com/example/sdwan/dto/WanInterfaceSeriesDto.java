package com.example.sdwan.dto;

import java.util.List;
import java.util.Objects;

public final class WanInterfaceSeriesDto {

    private final String               interfaceId;
    private final String               interfaceName;
    private final List<WanDataPointDto> dataPoints;

    public WanInterfaceSeriesDto(String interfaceId, String interfaceName,
                                  List<WanDataPointDto> dataPoints) {
        this.interfaceId   = Objects.requireNonNull(interfaceId,   "interfaceId");
        this.interfaceName = Objects.requireNonNull(interfaceName, "interfaceName");
        this.dataPoints    = List.copyOf(Objects.requireNonNull(dataPoints, "dataPoints"));
    }

    public String                interfaceId()   { return interfaceId; }
    public String                interfaceName() { return interfaceName; }
    public List<WanDataPointDto> dataPoints()    { return dataPoints; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WanInterfaceSeriesDto other)) return false;
        return Objects.equals(interfaceId, other.interfaceId)
            && Objects.equals(interfaceName, other.interfaceName)
            && Objects.equals(dataPoints, other.dataPoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interfaceId, interfaceName, dataPoints);
    }

    @Override
    public String toString() {
        return "WanInterfaceSeriesDto[interfaceId=" + interfaceId
            + ", interfaceName=" + interfaceName + ", points=" + dataPoints.size() + "]";
    }
}
