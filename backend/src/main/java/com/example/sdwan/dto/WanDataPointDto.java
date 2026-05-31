package com.example.sdwan.dto;

import java.util.Objects;

public final class WanDataPointDto {

    private final long   timestamp;
    private final double rxMbps;
    private final double txMbps;

    public WanDataPointDto(long timestamp, double rxMbps, double txMbps) {
        this.timestamp = timestamp;
        this.rxMbps    = rxMbps;
        this.txMbps    = txMbps;
    }

    public long   timestamp() { return timestamp; }
    public double rxMbps()    { return rxMbps; }
    public double txMbps()    { return txMbps; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WanDataPointDto other)) return false;
        return timestamp == other.timestamp
            && Double.compare(rxMbps, other.rxMbps) == 0
            && Double.compare(txMbps, other.txMbps) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, rxMbps, txMbps);
    }

    @Override
    public String toString() {
        return "WanDataPointDto[timestamp=" + timestamp + ", rxMbps=" + rxMbps + ", txMbps=" + txMbps + "]";
    }
}
