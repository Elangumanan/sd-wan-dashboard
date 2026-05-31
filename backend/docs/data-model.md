# Data Model

## Domain vs DTO Separation

The backend maintains a strict two-layer data model:

| Layer | Classes | Purpose |
|---|---|---|
| **Domain** | `com.example.sdwan.domain.*` | Internal representation. Never serialized to JSON directly. Passed between service and repository layers. |
| **DTO** | `com.example.sdwan.dto.*` | API-facing shapes. Serialized to JSON. Built by service layer from domain objects. |

---

## Domain Model

All domain classes are **immutable final classes** using `private final` fields, an all-args constructor with `Objects.requireNonNull` guards, and record-style accessors (`id()` not `getId()`).

### Enums

```
DeviceStatus    ONLINE | OFFLINE
SiteHealth      HEALTHY | DEGRADED | DOWN
InterfaceType   WAN | LAN
InterfaceStatus UP | DOWN
```

### Entity Classes

#### `Organization`
| Field | Type | Description |
|---|---|---|
| `id` | `String` | Unique identifier (e.g. `org-001`) |
| `name` | `String` | Display name |
| `description` | `String` | Free-text description |
| `region` | `String` | Geographic region |

#### `Site`
| Field | Type | Description |
|---|---|---|
| `id` | `String` | Unique identifier (e.g. `site-001`) |
| `orgId` | `String` | FK → Organization |
| `name` | `String` | Display name |
| `location` | `String` | City/state label |

> **Note:** `SiteHealth` is NOT stored on `Site`. It is computed at query time from the device statuses for that site.

#### `Device`
| Field | Type | Description |
|---|---|---|
| `id` | `String` | Unique identifier (e.g. `dev-001`) |
| `siteId` | `String` | FK → Site |
| `name` | `String` | Hostname (e.g. `NYC-EDGE-01`) |
| `model` | `String` | Hardware model string |
| `status` | `DeviceStatus` | ONLINE or OFFLINE |
| `ipAddress` | `String` | Management IP |
| `firmwareVersion` | `String` | IOS version string |
| `lastSeen` | `Instant` | UTC timestamp of last heartbeat |

#### `NetworkInterface`
| Field | Type | Description |
|---|---|---|
| `id` | `String` | Unique identifier (e.g. `iface-dev001-wan0`) |
| `deviceId` | `String` | FK → Device |
| `name` | `String` | Interface label (e.g. `GigabitEthernet0/0/0`) |
| `type` | `InterfaceType` | WAN or LAN |
| `status` | `InterfaceStatus` | UP or DOWN |
| `speedMbps` | `long` | Port speed in Mbps |

#### `WanDataPoint`
| Field | Type | Description |
|---|---|---|
| `timestamp` | `long` | Unix epoch milliseconds |
| `interfaceId` | `String` | FK → NetworkInterface |
| `rxMbps` | `double` | Receive bandwidth (Mbps), 2 decimal places |
| `txMbps` | `double` | Transmit bandwidth (Mbps), 2 decimal places |

---

## Entity Relationship

```
Organization (1)
  └── Site (*) — 4 sites per org in mock data
        └── Device (*) — 2 to 4 devices per site
              └── NetworkInterface (*) — 1–2 WAN + 1 LAN per device
                    └── WanDataPoint (*) — 288 points per WAN interface (24h @ 5min)
```

---

## Site Health Aggregation Rule

Computed by `SiteServiceImpl.computeHealth()` and `DashboardServiceImpl.computeHealth()`:

```
online_count  = devices where status == ONLINE
offline_count = devices where status == OFFLINE

if offline_count == 0  → HEALTHY   (all devices up)
if online_count  == 0  → DOWN      (all devices down)
else                   → DEGRADED  (partial outage)
```

---

## Mock Data Catalogue

### Organization

| ID | Name | Region |
|---|---|---|
| `org-001` | Acme Networks | North America |

### Sites

| ID | Name | Location | Expected Health |
|---|---|---|---|
| `site-001` | New York HQ | New York, NY | **HEALTHY** |
| `site-002` | Los Angeles Branch | Los Angeles, CA | **DEGRADED** |
| `site-003` | Chicago Office | Chicago, IL | **DOWN** |
| `site-004` | Seattle Data Center | Seattle, WA | **HEALTHY** |

### Devices

| ID | Name | Model | Site | Status |
|---|---|---|---|---|
| `dev-001` | NYC-EDGE-01 | Cisco ISR 4331 | site-001 | ONLINE |
| `dev-002` | NYC-EDGE-02 | Cisco ISR 4331 | site-001 | ONLINE |
| `dev-003` | NYC-CORE-01 | Cisco ISR 4451 | site-001 | ONLINE |
| `dev-004` | LAX-EDGE-01 | Cisco ISR 4331 | site-002 | ONLINE |
| `dev-005` | LAX-EDGE-02 | Cisco ISR 4331 | site-002 | **OFFLINE** |
| `dev-006` | LAX-CORE-01 | Cisco ISR 4451 | site-002 | ONLINE |
| `dev-007` | CHI-EDGE-01 | Cisco ISR 4331 | site-003 | **OFFLINE** |
| `dev-008` | CHI-CORE-01 | Cisco ISR 4451 | site-003 | **OFFLINE** |
| `dev-009` | SEA-EDGE-01 | Cisco ISR 4331 | site-004 | ONLINE |
| `dev-010` | SEA-EDGE-02 | Cisco ISR 4331 | site-004 | ONLINE |
| `dev-011` | SEA-CORE-01 | Cisco ISR 4451 | site-004 | ONLINE |
| `dev-012` | SEA-BACKUP-01 | Cisco ISR 4331 | site-004 | ONLINE |

**Interface convention:**
- Edge routers (ISR 4331): 1 WAN + 1 LAN
- Core routers (ISR 4451): 2 WAN + 1 LAN
- Offline devices: all interfaces DOWN, zero traffic

---

## WAN Traffic Data Generation

WAN history is pre-generated at startup by `MockDataStore.generateWanHistory()`. It is **fully deterministic** — no random seed, same values on every run.

### Formula

```
factor(i) = (1 + sin(2π · i / 288 − π/2)) / 2

rxMbps(i) = baseRx + ampRx · factor(i)
txMbps(i) = baseTx + ampTx · factor(i)
```

- `i` = data point index (0 to 287)
- `factor` ranges from 0.0 at midnight (i=0) to 1.0 at noon (i=144)
- Simulates a realistic business-hours traffic ramp

### Time Reference

All data is anchored to: **`2026-05-30T00:00:00Z`** (midnight UTC).  
Point `i` has timestamp: `base + i × 5 × 60 × 1000` ms.

### Traffic Parameters (Mbps)

| Interface | baseRx | ampRx | baseTx | ampTx |
|---|---|---|---|---|
| dev-001 WAN0 | 50 | 80 | 20 | 40 |
| dev-002 WAN0 | 45 | 70 | 18 | 35 |
| dev-003 WAN0 | 200 | 300 | 80 | 150 |
| dev-003 WAN1 | 180 | 280 | 70 | 130 |
| dev-004 WAN0 | 60 | 90 | 25 | 45 |
| dev-005 WAN0 | 0 | 0 | 0 | 0 *(OFFLINE)* |
| dev-006 WAN0 | 55 | 85 | 22 | 42 |
| dev-006 WAN1 | 48 | 75 | 19 | 38 |
| dev-007–008 | 0 | 0 | 0 | 0 *(OFFLINE)* |
| dev-009 WAN0 | 75 | 100 | 30 | 55 |
| dev-009 WAN1 | 65 | 90 | 28 | 48 |
| dev-010 WAN0 | 70 | 95 | 28 | 52 |
| dev-010 WAN1 | 62 | 85 | 25 | 45 |
| dev-011 WAN0 | 250 | 350 | 100 | 180 |
| dev-011 WAN1 | 230 | 320 | 90 | 160 |
| dev-012 WAN0 | 40 | 60 | 15 | 30 |

Peak (noon) = base + amplitude. Minimum (midnight) = base.

---

## API Response Envelope

### Success
```json
{
  "value": <T>,
  "message": "optional string — omitted when absent"
}
```

### Error
```json
{
  "code": "ERROR_CODE",
  "message": "Human-readable description",
  "details": ["field: constraint message"]
}
```

`details` is omitted from JSON when null (`@JsonInclude(NON_NULL)`).
