package com.example.sdwan.domain;

import java.util.Objects;

public final class WanDataPoint {

    private final long   timestamp;
    private final String interfaceId;
    private final double rxMbps;
    private final double txMbps;

    public WanDataPoint(long timestamp, String interfaceId, double rxMbps, double txMbps) {
        this.timestamp   = timestamp;
        this.interfaceId = Objects.requireNonNull(interfaceId, "interfaceId");
        this.rxMbps      = rxMbps;
        this.txMbps      = txMbps;
    }

    public long   timestamp()   { return timestamp; }
    public String interfaceId() { return interfaceId; }
    public double rxMbps()      { return rxMbps; }
    public double txMbps()      { return txMbps; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WanDataPoint other)) return false;
        return timestamp == other.timestamp
            && Double.compare(rxMbps, other.rxMbps) == 0
            && Double.compare(txMbps, other.txMbps) == 0
            && Objects.equals(interfaceId, other.interfaceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, interfaceId, rxMbps, txMbps);
    }

    @Override
    public String toString() {
        return "WanDataPoint[timestamp=" + timestamp + ", interfaceId=" + interfaceId
            + ", rxMbps=" + rxMbps + ", txMbps=" + txMbps + "]";
    }
}
