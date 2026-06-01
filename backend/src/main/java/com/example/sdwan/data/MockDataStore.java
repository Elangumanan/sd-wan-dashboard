package com.example.sdwan.data;

import com.example.sdwan.domain.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

/**
 * Single source of truth for all in-memory mock data.
 * Initialized once at startup — deterministic, no random seeds.
 *
 * Site layout (matches the reference dashboard mockup):
 *   site-001 Mumbai-Branch    — 2 devices, both ONLINE   → HEALTHY
 *   site-002 Bangalore-Branch — 2 devices, both ONLINE   → HEALTHY
 *   site-003 Pune-Branch      — 2 devices, one OFFLINE   → DEGRADED
 *   site-004 Chennai-Branch   — 2 devices, both OFFLINE  → DOWN
 *   site-005 Hyderabad-DC     — 1 device,  ONLINE        → HEALTHY
 */
@Component
public class MockDataStore {

    // Fixed reference: 2026-05-30T00:00:00Z — all WAN history covers the 24h from this point.
    private static final Instant BASE_TIME = Instant.parse("2026-05-30T00:00:00Z");
    private static final int TOTAL_POINTS = 288;          // 24h at 5-min intervals
    private static final long INTERVAL_MS = 5 * 60 * 1000L;

    private final Map<String, Organization>     organizations    = new LinkedHashMap<>();
    private final Map<String, Site>             sites            = new LinkedHashMap<>();
    private final Map<String, Device>           devices          = new LinkedHashMap<>();
    private final Map<String, NetworkInterface> interfaces       = new LinkedHashMap<>();
    private final Map<String, List<WanDataPoint>> wanHistory     = new LinkedHashMap<>();

    // interfaceId -> [currentRxMbps, currentTxMbps] for LAN interfaces (fixed values)
    private final Map<String, double[]> lanCurrentTraffic = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        seedOrganization();
        seedSites();
        seedDevices();
        seedInterfaces();
        generateWanHistory();
    }

    // ── Seed methods ────────────────────────────────────────────────────────────

    private void seedOrganization() {
        Organization org = new Organization(
                "org-001",
                "Acme Corporation",
                "Enterprise SD-WAN deployment across India",
                "India"
        );
        organizations.put(org.id(), org);
    }

    private void seedSites() {
        for (Site s : List.of(
                site("site-001", "org-001", "Mumbai-Branch",    "Mumbai, MH"),
                site("site-002", "org-001", "Bangalore-Branch", "Bangalore, KA"),
                site("site-003", "org-001", "Pune-Branch",      "Pune, MH"),
                site("site-004", "org-001", "Chennai-Branch",   "Chennai, TN"),
                site("site-005", "org-001", "Hyderabad-DC",     "Hyderabad, TS")
        )) {
            sites.put(s.id(), s);
        }
    }

    private void seedDevices() {
        Instant onlineAt   = Instant.parse("2026-05-31T00:00:00Z");
        Instant offline5h  = Instant.parse("2026-05-30T18:30:00Z");
        Instant offline40h = Instant.parse("2026-05-29T08:00:00Z");

        for (Device d : List.of(
                // site-001 Mumbai-Branch — both ONLINE → HEALTHY
                device("dev-001", "site-001", "MUM-EDGE-01", "vEdge Cloud", DeviceStatus.ONLINE,  DeviceRole.ACTIVE,  "5d 14h 32m", "10.1.1.1", "17.12.3", onlineAt),
                device("dev-002", "site-001", "MUM-EDGE-02", "vEdge Cloud", DeviceStatus.ONLINE,  DeviceRole.STANDBY, "5d 14h 28m", "10.1.1.2", "17.12.3", onlineAt),
                // site-002 Bangalore-Branch — both ONLINE → HEALTHY
                device("dev-003", "site-002", "BLR-EDGE-01", "vEdge Cloud", DeviceStatus.ONLINE,  DeviceRole.ACTIVE,  "3d 8h 10m",  "10.2.1.1", "17.12.3", onlineAt),
                device("dev-004", "site-002", "BLR-EDGE-02", "vEdge 100",   DeviceStatus.ONLINE,  DeviceRole.STANDBY, "3d 8h 5m",   "10.2.1.2", "17.11.2", onlineAt),
                // site-003 Pune-Branch — one OFFLINE → DEGRADED
                device("dev-005", "site-003", "PUN-EDGE-01", "vEdge 100",   DeviceStatus.ONLINE,  DeviceRole.ACTIVE,  "1d 2h 44m",  "10.3.1.1", "17.11.2", onlineAt),
                device("dev-006", "site-003", "PUN-EDGE-02", "vEdge 100",   DeviceStatus.OFFLINE, DeviceRole.STANDBY, "—",          "10.3.1.2", "17.11.2", offline5h),
                // site-004 Chennai-Branch — all OFFLINE → DOWN
                device("dev-007", "site-004", "CHN-EDGE-01", "vEdge Cloud", DeviceStatus.OFFLINE, DeviceRole.ACTIVE,  "—",          "10.4.1.1", "17.10.1", offline40h),
                device("dev-008", "site-004", "CHN-EDGE-02", "vEdge Cloud", DeviceStatus.OFFLINE, DeviceRole.STANDBY, "—",          "10.4.1.2", "17.10.1", offline40h),
                // site-005 Hyderabad-DC — single device ONLINE → HEALTHY
                device("dev-009", "site-005", "HYD-EDGE-01", "vEdge 5000",  DeviceStatus.ONLINE,  DeviceRole.ACTIVE,  "12d 6h 0m",  "10.5.1.1", "17.12.3", onlineAt)
        )) {
            devices.put(d.id(), d);
        }
    }

    private void seedInterfaces() {
        // Naming convention: iface-<deviceId>-<label>
        // Each edge device: 2 WAN (WAN1, WAN2) + LAN interfaces
        // Offline devices: all interfaces DOWN

        // site-001 Mumbai-Branch
        wanIface("iface-dev001-wan0", "dev-001", "WAN1", InterfaceStatus.UP, "100.64.1.1", 1_000);
        wanIface("iface-dev001-wan1", "dev-001", "WAN2", InterfaceStatus.UP, "100.64.2.1", 1_000);
        lanIface("iface-dev001-lan0", "dev-001", "LAN1", InterfaceStatus.UP, "192.168.1.1/24", 1_000, 85.0, 32.0);
        lanIface("iface-dev001-lan1", "dev-001", "LAN2", InterfaceStatus.UP, "192.168.2.1/24", 1_000, 40.0, 18.0);
        lanIface("iface-dev001-lan2", "dev-001", "LAN3", InterfaceStatus.UP, "192.168.3.1/24", 1_000, 22.0, 10.0);

        wanIface("iface-dev002-wan0", "dev-002", "WAN1", InterfaceStatus.UP, "100.64.1.2", 1_000);
        wanIface("iface-dev002-wan1", "dev-002", "WAN2", InterfaceStatus.UP, "100.64.2.2", 1_000);
        lanIface("iface-dev002-lan0", "dev-002", "LAN1", InterfaceStatus.UP, "192.168.1.2/24", 1_000, 76.0, 28.0);
        lanIface("iface-dev002-lan1", "dev-002", "LAN2", InterfaceStatus.UP, "192.168.2.2/24", 1_000, 35.0, 15.0);

        // site-002 Bangalore-Branch
        wanIface("iface-dev003-wan0", "dev-003", "WAN1", InterfaceStatus.UP, "100.65.1.1", 1_000);
        wanIface("iface-dev003-wan1", "dev-003", "WAN2", InterfaceStatus.UP, "100.65.2.1", 1_000);
        lanIface("iface-dev003-lan0", "dev-003", "LAN1", InterfaceStatus.UP, "192.168.10.1/24", 1_000, 92.0, 38.0);

        wanIface("iface-dev004-wan0", "dev-004", "WAN1", InterfaceStatus.UP, "100.65.1.2", 1_000);
        wanIface("iface-dev004-wan1", "dev-004", "WAN2", InterfaceStatus.UP, "100.65.2.2", 1_000);
        lanIface("iface-dev004-lan0", "dev-004", "LAN1", InterfaceStatus.UP, "192.168.10.2/24", 1_000, 60.0, 24.0);

        // site-003 Pune-Branch
        wanIface("iface-dev005-wan0", "dev-005", "WAN1", InterfaceStatus.UP, "100.66.1.1", 1_000);
        wanIface("iface-dev005-wan1", "dev-005", "WAN2", InterfaceStatus.UP, "100.66.2.1", 1_000);
        lanIface("iface-dev005-lan0", "dev-005", "LAN1", InterfaceStatus.UP, "192.168.20.1/24", 1_000, 70.0, 28.0);

        // dev-006 OFFLINE — all interfaces DOWN
        wanIface("iface-dev006-wan0", "dev-006", "WAN1", InterfaceStatus.DOWN, "100.66.1.2", 1_000);
        wanIface("iface-dev006-wan1", "dev-006", "WAN2", InterfaceStatus.DOWN, "100.66.2.2", 1_000);
        lanIface("iface-dev006-lan0", "dev-006", "LAN1", InterfaceStatus.DOWN, "192.168.20.2/24", 1_000, 0.0, 0.0);

        // site-004 Chennai-Branch (all DOWN)
        wanIface("iface-dev007-wan0", "dev-007", "WAN1", InterfaceStatus.DOWN, "100.67.1.1", 1_000);
        wanIface("iface-dev007-wan1", "dev-007", "WAN2", InterfaceStatus.DOWN, "100.67.2.1", 1_000);
        lanIface("iface-dev007-lan0", "dev-007", "LAN1", InterfaceStatus.DOWN, "192.168.30.1/24", 1_000, 0.0, 0.0);

        wanIface("iface-dev008-wan0", "dev-008", "WAN1", InterfaceStatus.DOWN, "100.67.1.2", 1_000);
        wanIface("iface-dev008-wan1", "dev-008", "WAN2", InterfaceStatus.DOWN, "100.67.2.2", 1_000);
        lanIface("iface-dev008-lan0", "dev-008", "LAN1", InterfaceStatus.DOWN, "192.168.30.2/24", 1_000, 0.0, 0.0);

        // site-005 Hyderabad-DC
        wanIface("iface-dev009-wan0", "dev-009", "WAN1", InterfaceStatus.UP, "100.68.1.1", 10_000);
        wanIface("iface-dev009-wan1", "dev-009", "WAN2", InterfaceStatus.UP, "100.68.2.1", 10_000);
        lanIface("iface-dev009-lan0", "dev-009", "LAN1", InterfaceStatus.UP, "192.168.40.1/24", 10_000, 320.0, 110.0);
        lanIface("iface-dev009-lan1", "dev-009", "LAN2", InterfaceStatus.UP, "192.168.40.2/24", 10_000, 180.0, 70.0);
    }

    /**
     * Generate deterministic sine-wave WAN traffic for every WAN interface.
     * Mimics business-hours traffic: low at midnight (i=0), peaks midday (i=144).
     * Formula: value = base + amplitude * ((1 + sin(2π·i/288 - π/2)) / 2)
     */
    private void generateWanHistory() {
        Map<String, double[]> params = wanTrafficParams();

        for (NetworkInterface iface : interfaces.values()) {
            if (iface.type() != InterfaceType.WAN) continue;

            double[] p = params.getOrDefault(iface.id(), new double[]{0, 0, 0, 0});
            double baseRx = p[0], ampRx = p[1], baseTx = p[2], ampTx = p[3];

            List<WanDataPoint> points = new ArrayList<>(TOTAL_POINTS);
            for (int i = 0; i < TOTAL_POINTS; i++) {
                long ts = BASE_TIME.toEpochMilli() + (long) i * INTERVAL_MS;
                double factor = (1.0 + Math.sin(2 * Math.PI * i / TOTAL_POINTS - Math.PI / 2)) / 2.0;
                double rx = round2(baseRx + ampRx * factor);
                double tx = round2(baseTx + ampTx * factor);
                points.add(new WanDataPoint(ts, iface.id(), rx, tx));
            }
            wanHistory.put(iface.id(), Collections.unmodifiableList(points));
        }
    }

    /** Per-interface traffic parameters: [baseRx, ampRx, baseTx, ampTx] in Mbps. */
    private Map<String, double[]> wanTrafficParams() {
        Map<String, double[]> m = new HashMap<>();
        // site-001 Mumbai
        m.put("iface-dev001-wan0", new double[]{ 50,  80,  20,  40});
        m.put("iface-dev001-wan1", new double[]{ 25,  45,  10,  22});
        m.put("iface-dev002-wan0", new double[]{ 45,  70,  18,  35});
        m.put("iface-dev002-wan1", new double[]{ 22,  40,   9,  20});
        // site-002 Bangalore
        m.put("iface-dev003-wan0", new double[]{ 60,  90,  25,  45});
        m.put("iface-dev003-wan1", new double[]{ 30,  50,  12,  25});
        m.put("iface-dev004-wan0", new double[]{ 48,  75,  19,  38});
        m.put("iface-dev004-wan1", new double[]{ 24,  42,  10,  21});
        // site-003 Pune (dev-005 online, dev-006 OFFLINE → zero)
        m.put("iface-dev005-wan0", new double[]{ 55,  85,  22,  42});
        m.put("iface-dev005-wan1", new double[]{ 28,  48,  11,  24});
        m.put("iface-dev006-wan0", new double[]{  0,   0,   0,   0});
        m.put("iface-dev006-wan1", new double[]{  0,   0,   0,   0});
        // site-004 Chennai (all OFFLINE → zero)
        m.put("iface-dev007-wan0", new double[]{  0,   0,   0,   0});
        m.put("iface-dev007-wan1", new double[]{  0,   0,   0,   0});
        m.put("iface-dev008-wan0", new double[]{  0,   0,   0,   0});
        m.put("iface-dev008-wan1", new double[]{  0,   0,   0,   0});
        // site-005 Hyderabad (high-capacity DC)
        m.put("iface-dev009-wan0", new double[]{250, 350, 100, 180});
        m.put("iface-dev009-wan1", new double[]{180, 280,  70, 130});
        return m;
    }

    // ── Public accessors (used by repositories) ──────────────────────────────

    public Map<String, Organization>       getOrganizations() { return Collections.unmodifiableMap(organizations); }
    public Map<String, Site>               getSites()         { return Collections.unmodifiableMap(sites); }
    public Map<String, Device>             getDevices()       { return Collections.unmodifiableMap(devices); }
    public Map<String, NetworkInterface>   getInterfaces()    { return Collections.unmodifiableMap(interfaces); }
    public Map<String, List<WanDataPoint>> getWanHistory()    { return Collections.unmodifiableMap(wanHistory); }

    public double getCurrentRx(String interfaceId) {
        NetworkInterface iface = interfaces.get(interfaceId);
        if (iface == null) return 0.0;
        if (iface.type() == InterfaceType.LAN) {
            return lanCurrentTraffic.getOrDefault(interfaceId, new double[]{0.0, 0.0})[0];
        }
        List<WanDataPoint> pts = wanHistory.get(interfaceId);
        return (pts != null && !pts.isEmpty()) ? pts.get(pts.size() - 1).rxMbps() : 0.0;
    }

    public double getCurrentTx(String interfaceId) {
        NetworkInterface iface = interfaces.get(interfaceId);
        if (iface == null) return 0.0;
        if (iface.type() == InterfaceType.LAN) {
            return lanCurrentTraffic.getOrDefault(interfaceId, new double[]{0.0, 0.0})[1];
        }
        List<WanDataPoint> pts = wanHistory.get(interfaceId);
        return (pts != null && !pts.isEmpty()) ? pts.get(pts.size() - 1).txMbps() : 0.0;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void wanIface(String id, String deviceId, String name, InterfaceStatus status,
                          String ipAddress, long speedMbps) {
        interfaces.put(id, new NetworkInterface(id, deviceId, name, InterfaceType.WAN, status, ipAddress, speedMbps));
    }

    private void lanIface(String id, String deviceId, String name, InterfaceStatus status,
                          String ipAddress, long speedMbps, double currentRx, double currentTx) {
        interfaces.put(id, new NetworkInterface(id, deviceId, name, InterfaceType.LAN, status, ipAddress, speedMbps));
        lanCurrentTraffic.put(id, new double[]{currentRx, currentTx});
    }

    private static Site site(String id, String orgId, String name, String location) {
        return new Site(id, orgId, name, location);
    }

    private static Device device(String id, String siteId, String name, String model,
                                  DeviceStatus status, DeviceRole role, String uptime,
                                  String ip, String firmware, Instant lastSeen) {
        return new Device(id, siteId, name, model, status, role, uptime, ip, firmware, lastSeen);
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
