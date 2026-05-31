package com.example.sdwan.data;

import com.example.sdwan.domain.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

/**
 * Single source of truth for all in-memory mock data.
 * Initialized once at startup — deterministic, no random seeds.
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
                "Acme Networks",
                "Enterprise SD-WAN deployment across North America",
                "North America"
        );
        organizations.put(org.id(), org);
    }

    private void seedSites() {
        for (Site s : List.of(
                site("site-001", "org-001", "New York HQ",         "New York, NY"),
                site("site-002", "org-001", "Los Angeles Branch",  "Los Angeles, CA"),
                site("site-003", "org-001", "Chicago Office",      "Chicago, IL"),
                site("site-004", "org-001", "Seattle Data Center", "Seattle, WA")
        )) {
            sites.put(s.id(), s);
        }
    }

    private void seedDevices() {
        Instant onlineAt   = Instant.parse("2026-05-31T00:00:00Z");
        Instant offline5h  = Instant.parse("2026-05-30T18:30:00Z");
        Instant offline40h = Instant.parse("2026-05-29T08:00:00Z");

        for (Device d : List.of(
                // site-001 — all ONLINE → HEALTHY
                device("dev-001", "site-001", "NYC-EDGE-01",   "Cisco ISR 4331", DeviceStatus.ONLINE,  DeviceRole.ACTIVE,  "10.1.1.1", "17.12.3", onlineAt),
                device("dev-002", "site-001", "NYC-EDGE-02",   "Cisco ISR 4331", DeviceStatus.ONLINE,  DeviceRole.STANDBY, "10.1.1.2", "17.12.3", onlineAt),
                device("dev-003", "site-001", "NYC-CORE-01",   "Cisco ISR 4451", DeviceStatus.ONLINE,  DeviceRole.ACTIVE,  "10.1.1.3", "17.12.3", onlineAt),
                // site-002 — dev-005 offline → DEGRADED
                device("dev-004", "site-002", "LAX-EDGE-01",   "Cisco ISR 4331", DeviceStatus.ONLINE,  DeviceRole.ACTIVE,  "10.2.1.1", "17.11.2", onlineAt),
                device("dev-005", "site-002", "LAX-EDGE-02",   "Cisco ISR 4331", DeviceStatus.OFFLINE, DeviceRole.STANDBY, "10.2.1.2", "17.11.2", offline5h),
                device("dev-006", "site-002", "LAX-CORE-01",   "Cisco ISR 4451", DeviceStatus.ONLINE,  DeviceRole.ACTIVE,  "10.2.1.3", "17.11.2", onlineAt),
                // site-003 — all offline → DOWN
                device("dev-007", "site-003", "CHI-EDGE-01",   "Cisco ISR 4331", DeviceStatus.OFFLINE, DeviceRole.ACTIVE,  "10.3.1.1", "17.10.1", offline40h),
                device("dev-008", "site-003", "CHI-CORE-01",   "Cisco ISR 4451", DeviceStatus.OFFLINE, DeviceRole.ACTIVE,  "10.3.1.2", "17.10.1", offline40h),
                // site-004 — all ONLINE → HEALTHY
                device("dev-009", "site-004", "SEA-EDGE-01",   "Cisco ISR 4331", DeviceStatus.ONLINE,  DeviceRole.ACTIVE,  "10.4.1.1", "17.12.3", onlineAt),
                device("dev-010", "site-004", "SEA-EDGE-02",   "Cisco ISR 4331", DeviceStatus.ONLINE,  DeviceRole.STANDBY, "10.4.1.2", "17.12.3", onlineAt),
                device("dev-011", "site-004", "SEA-CORE-01",   "Cisco ISR 4451", DeviceStatus.ONLINE,  DeviceRole.ACTIVE,  "10.4.1.3", "17.12.3", onlineAt),
                device("dev-012", "site-004", "SEA-BACKUP-01", "Cisco ISR 4331", DeviceStatus.ONLINE,  DeviceRole.STANDBY, "10.4.1.4", "17.12.3", onlineAt)
        )) {
            devices.put(d.id(), d);
        }
    }

    private void seedInterfaces() {
        // Naming convention: iface-<deviceId>-<label>
        // Edge routers: 1 WAN + 1 LAN
        // Core routers: 2 WAN + 1 LAN
        // Offline devices: all interfaces DOWN

        // site-001
        wanIface("iface-dev001-wan0", "dev-001", "GigabitEthernet0/0/0", InterfaceStatus.UP,   1_000);
        lanIface("iface-dev001-lan0", "dev-001", "GigabitEthernet0/1/0", InterfaceStatus.UP,   1_000,  85.0,  32.0);

        wanIface("iface-dev002-wan0", "dev-002", "GigabitEthernet0/0/0", InterfaceStatus.UP,   1_000);
        lanIface("iface-dev002-lan0", "dev-002", "GigabitEthernet0/1/0", InterfaceStatus.UP,   1_000,  76.0,  28.0);

        wanIface("iface-dev003-wan0", "dev-003", "GigabitEthernet0/0/0", InterfaceStatus.UP,  10_000);
        wanIface("iface-dev003-wan1", "dev-003", "GigabitEthernet0/0/1", InterfaceStatus.UP,  10_000);
        lanIface("iface-dev003-lan0", "dev-003", "GigabitEthernet0/1/0", InterfaceStatus.UP,  10_000, 320.0, 110.0);

        // site-002
        wanIface("iface-dev004-wan0", "dev-004", "GigabitEthernet0/0/0", InterfaceStatus.UP,   1_000);
        lanIface("iface-dev004-lan0", "dev-004", "GigabitEthernet0/1/0", InterfaceStatus.UP,   1_000,  92.0,  38.0);

        wanIface("iface-dev005-wan0", "dev-005", "GigabitEthernet0/0/0", InterfaceStatus.DOWN,  1_000);
        lanIface("iface-dev005-lan0", "dev-005", "GigabitEthernet0/1/0", InterfaceStatus.DOWN,  1_000,   0.0,   0.0);

        wanIface("iface-dev006-wan0", "dev-006", "GigabitEthernet0/0/0", InterfaceStatus.UP,  10_000);
        wanIface("iface-dev006-wan1", "dev-006", "GigabitEthernet0/0/1", InterfaceStatus.UP,  10_000);
        lanIface("iface-dev006-lan0", "dev-006", "GigabitEthernet0/1/0", InterfaceStatus.UP,  10_000, 280.0,  95.0);

        // site-003 (all DOWN)
        wanIface("iface-dev007-wan0", "dev-007", "GigabitEthernet0/0/0", InterfaceStatus.DOWN,  1_000);
        lanIface("iface-dev007-lan0", "dev-007", "GigabitEthernet0/1/0", InterfaceStatus.DOWN,  1_000,   0.0,   0.0);

        wanIface("iface-dev008-wan0", "dev-008", "GigabitEthernet0/0/0", InterfaceStatus.DOWN, 10_000);
        wanIface("iface-dev008-wan1", "dev-008", "GigabitEthernet0/0/1", InterfaceStatus.DOWN, 10_000);
        lanIface("iface-dev008-lan0", "dev-008", "GigabitEthernet0/1/0", InterfaceStatus.DOWN, 10_000,   0.0,   0.0);

        // site-004
        wanIface("iface-dev009-wan0", "dev-009", "GigabitEthernet0/0/0", InterfaceStatus.UP,   1_000);
        wanIface("iface-dev009-wan1", "dev-009", "GigabitEthernet0/0/1", InterfaceStatus.UP,   1_000);
        lanIface("iface-dev009-lan0", "dev-009", "GigabitEthernet0/1/0", InterfaceStatus.UP,   1_000, 105.0,  44.0);

        wanIface("iface-dev010-wan0", "dev-010", "GigabitEthernet0/0/0", InterfaceStatus.UP,   1_000);
        wanIface("iface-dev010-wan1", "dev-010", "GigabitEthernet0/0/1", InterfaceStatus.UP,   1_000);
        lanIface("iface-dev010-lan0", "dev-010", "GigabitEthernet0/1/0", InterfaceStatus.UP,   1_000,  98.0,  40.0);

        wanIface("iface-dev011-wan0", "dev-011", "GigabitEthernet0/0/0", InterfaceStatus.UP,  10_000);
        wanIface("iface-dev011-wan1", "dev-011", "GigabitEthernet0/0/1", InterfaceStatus.UP,  10_000);
        lanIface("iface-dev011-lan0", "dev-011", "GigabitEthernet0/1/0", InterfaceStatus.UP,  10_000, 410.0, 140.0);

        wanIface("iface-dev012-wan0", "dev-012", "GigabitEthernet0/0/0", InterfaceStatus.UP,   1_000);
        lanIface("iface-dev012-lan0", "dev-012", "GigabitEthernet0/1/0", InterfaceStatus.UP,   1_000,  55.0,  20.0);
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
        // site-001
        m.put("iface-dev001-wan0", new double[]{ 50,  80,  20,  40});
        m.put("iface-dev002-wan0", new double[]{ 45,  70,  18,  35});
        m.put("iface-dev003-wan0", new double[]{200, 300,  80, 150});
        m.put("iface-dev003-wan1", new double[]{180, 280,  70, 130});
        // site-002
        m.put("iface-dev004-wan0", new double[]{ 60,  90,  25,  45});
        m.put("iface-dev005-wan0", new double[]{  0,   0,   0,   0}); // OFFLINE
        m.put("iface-dev006-wan0", new double[]{ 55,  85,  22,  42});
        m.put("iface-dev006-wan1", new double[]{ 48,  75,  19,  38});
        // site-003 (all OFFLINE)
        m.put("iface-dev007-wan0", new double[]{  0,   0,   0,   0});
        m.put("iface-dev008-wan0", new double[]{  0,   0,   0,   0});
        m.put("iface-dev008-wan1", new double[]{  0,   0,   0,   0});
        // site-004
        m.put("iface-dev009-wan0", new double[]{ 75, 100,  30,  55});
        m.put("iface-dev009-wan1", new double[]{ 65,  90,  28,  48});
        m.put("iface-dev010-wan0", new double[]{ 70,  95,  28,  52});
        m.put("iface-dev010-wan1", new double[]{ 62,  85,  25,  45});
        m.put("iface-dev011-wan0", new double[]{250, 350, 100, 180});
        m.put("iface-dev011-wan1", new double[]{230, 320,  90, 160});
        m.put("iface-dev012-wan0", new double[]{ 40,  60,  15,  30});
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

    private void wanIface(String id, String deviceId, String name, InterfaceStatus status, long speedMbps) {
        interfaces.put(id, new NetworkInterface(id, deviceId, name, InterfaceType.WAN, status, speedMbps));
    }

    private void lanIface(String id, String deviceId, String name, InterfaceStatus status, long speedMbps,
                          double currentRx, double currentTx) {
        interfaces.put(id, new NetworkInterface(id, deviceId, name, InterfaceType.LAN, status, speedMbps));
        lanCurrentTraffic.put(id, new double[]{currentRx, currentTx});
    }
    private static Site site(String id, String orgId, String name, String location) {
        return new Site(id, orgId, name, location);
    }

    private static Device device(String id, String siteId, String name, String model,
                                  DeviceStatus status, DeviceRole role, String ip,
                                  String firmware, Instant lastSeen) {
        return new Device(id, siteId, name, model, status, role, ip, firmware, lastSeen);
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
