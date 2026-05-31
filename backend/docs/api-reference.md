# API Reference

Base URL: `http://localhost:8080`  
All endpoints are prefixed with `/api`.  
All successful responses are wrapped in `SuccessResponse<T>`: `{ "value": <T> }`.  
All error responses use `ErrorResponse`: `{ "code": "...", "message": "...", "details": [...] }`.

Interactive docs: `http://localhost:8080/swagger-ui.html`  
OpenAPI JSON: `http://localhost:8080/api-docs`

---

## Health

### `GET /api/health`

Smoke-test endpoint — verifies the server is running.

**Response `200 OK`**
```json
{
  "status": "ok",
  "service": "sdwan-assignment-starter-api",
  "timestamp": "2026-05-31T00:00:00Z",
  "assignment": "Replace this starter endpoint with your SD-WAN dashboard API."
}
```

> This endpoint returns a raw `Map`, not a `SuccessResponse<T>` wrapper.

---

## Dashboard

### `GET /api/dashboard/overview`

Returns all numbers needed for the four summary cards and two donut charts on the Overview page. A dedicated aggregation endpoint — avoids multiple round-trips from the frontend.

**Response `200 OK`**
```json
{
  "value": {
    "totalSites": 4,
    "totalEdgeDevices": 12,
    "siteStatusSummary": {
      "healthy": 2,
      "degraded": 1,
      "down": 1,
      "total": 4
    },
    "deviceStatusSummary": {
      "online": 10,
      "offline": 2,
      "total": 12
    }
  }
}
```

---

### `GET /api/dashboard/site-health`

Returns one row per site for the Site Health Snapshot table.

**Response `200 OK`**
```json
{
  "value": [
    {
      "siteId": "site-001",
      "siteName": "New York HQ",
      "healthStatus": "HEALTHY",
      "totalDevices": 3,
      "onlineDevices": 3,
      "offlineDevices": 0
    },
    {
      "siteId": "site-002",
      "siteName": "Los Angeles Branch",
      "healthStatus": "DEGRADED",
      "totalDevices": 3,
      "onlineDevices": 2,
      "offlineDevices": 1
    },
    {
      "siteId": "site-003",
      "siteName": "Chicago Office",
      "healthStatus": "DOWN",
      "totalDevices": 2,
      "onlineDevices": 0,
      "offlineDevices": 2
    },
    {
      "siteId": "site-004",
      "siteName": "Seattle Data Center",
      "healthStatus": "HEALTHY",
      "totalDevices": 4,
      "onlineDevices": 4,
      "offlineDevices": 0
    }
  ]
}
```

---

## Organizations

### `GET /api/organizations`

Lists all organizations with site health aggregation.

**Response `200 OK`**
```json
{
  "value": [
    {
      "id": "org-001",
      "name": "Acme Networks",
      "description": "Enterprise SD-WAN deployment across North America",
      "region": "North America",
      "totalSites": 4,
      "healthySites": 2,
      "degradedSites": 1,
      "downSites": 1
    }
  ]
}
```

---

### `GET /api/organizations/{id}`

Returns a single organization by ID.

**Path parameters**
| Parameter | Type | Description |
|---|---|---|
| `id` | `string` | Organization identifier |

**Response `200 OK`** — same shape as a single element from the list above.

**Response `404 Not Found`**
```json
{
  "code": "RESOURCE_NOT_FOUND",
  "message": "Organization not found: org-999"
}
```

---

## Sites

### `GET /api/organizations/{orgId}/sites`

Lists all sites for an organization, each with its computed health status and device counts.

**Path parameters**
| Parameter | Type | Description |
|---|---|---|
| `orgId` | `string` | Parent organization identifier |

**Response `200 OK`**
```json
{
  "value": [
    {
      "id": "site-001",
      "orgId": "org-001",
      "name": "New York HQ",
      "location": "New York, NY",
      "health": "HEALTHY",
      "deviceCount": 3,
      "onlineDeviceCount": 3,
      "offlineDeviceCount": 0
    }
  ]
}
```

**Error `404`** — if `orgId` does not exist.

---

### `GET /api/organizations/{orgId}/sites/{siteId}`

Returns site detail including a summary of every device.

**Path parameters**
| Parameter | Type | Description |
|---|---|---|
| `orgId` | `string` | Parent organization identifier |
| `siteId` | `string` | Site identifier |

**Response `200 OK`**
```json
{
  "value": {
    "id": "site-002",
    "orgId": "org-001",
    "name": "Los Angeles Branch",
    "location": "Los Angeles, CA",
    "health": "DEGRADED",
    "deviceCount": 3,
    "onlineDeviceCount": 2,
    "offlineDeviceCount": 1,
    "devices": [
      {
        "id": "dev-004",
        "siteId": "site-002",
        "name": "LAX-EDGE-01",
        "model": "Cisco ISR 4331",
        "status": "ONLINE",
        "ipAddress": "10.2.1.1"
      },
      {
        "id": "dev-005",
        "siteId": "site-002",
        "name": "LAX-EDGE-02",
        "model": "Cisco ISR 4331",
        "status": "OFFLINE",
        "ipAddress": "10.2.1.2"
      }
    ]
  }
}
```

**Error `404`** — if `orgId` or `siteId` does not exist, or `siteId` does not belong to `orgId`.

---

## Devices

### `GET /api/organizations/{orgId}/sites/{siteId}/devices/{deviceId}`

Returns full device detail including all network interfaces with current bandwidth readings.

**Path parameters**
| Parameter | Type | Description |
|---|---|---|
| `orgId` | `string` | Parent organization identifier |
| `siteId` | `string` | Parent site identifier |
| `deviceId` | `string` | Device identifier |

**Response `200 OK`**
```json
{
  "value": {
    "id": "dev-003",
    "siteId": "site-001",
    "name": "NYC-CORE-01",
    "model": "Cisco ISR 4451",
    "status": "ONLINE",
    "ipAddress": "10.1.1.3",
    "firmwareVersion": "17.12.3",
    "lastSeen": "2026-05-31T00:00:00Z",
    "interfaces": [
      {
        "id": "iface-dev003-wan0",
        "name": "GigabitEthernet0/0/0",
        "type": "WAN",
        "status": "UP",
        "speedMbps": 10000,
        "currentRxMbps": 350.0,
        "currentTxMbps": 130.0
      },
      {
        "id": "iface-dev003-wan1",
        "name": "GigabitEthernet0/0/1",
        "type": "WAN",
        "status": "UP",
        "speedMbps": 10000,
        "currentRxMbps": 320.0,
        "currentTxMbps": 110.0
      },
      {
        "id": "iface-dev003-lan0",
        "name": "GigabitEthernet0/1/0",
        "type": "LAN",
        "status": "UP",
        "speedMbps": 10000,
        "currentRxMbps": 320.0,
        "currentTxMbps": 110.0
      }
    ]
  }
}
```

**Error `404`** — if any segment of the path (`orgId` / `siteId` / `deviceId`) does not exist or the ownership chain is violated.

---

### `GET /api/organizations/{orgId}/sites/{siteId}/devices/{deviceId}/wan-history`

Returns WAN bandwidth time-series data per WAN interface. LAN interfaces are excluded.

**Path parameters** — same as device detail above.

**Query parameters**
| Parameter | Type | Default | Values | Description |
|---|---|---|---|---|
| `range` | `string` | `24h` | `1h`, `6h`, `24h` | Time window of data to return |

**Range → data points mapping**
| `range` | Points returned | Time covered |
|---|---|---|
| `1h` | 12 | Last 60 minutes |
| `6h` | 72 | Last 6 hours |
| `24h` | 288 | Full 24-hour window |

Data interval: **5 minutes** per point. Timestamps are Unix epoch milliseconds.

**Response `200 OK`**
```json
{
  "value": {
    "deviceId": "dev-003",
    "deviceName": "NYC-CORE-01",
    "range": "24h",
    "interfaces": [
      {
        "interfaceId": "iface-dev003-wan0",
        "interfaceName": "GigabitEthernet0/0/0",
        "dataPoints": [
          { "timestamp": 1748563200000, "rxMbps": 50.0,  "txMbps": 20.0  },
          { "timestamp": 1748563500000, "rxMbps": 51.2,  "txMbps": 20.5  },
          "... 286 more points"
        ]
      },
      {
        "interfaceId": "iface-dev003-wan1",
        "interfaceName": "GigabitEthernet0/0/1",
        "dataPoints": [ "... 288 points" ]
      }
    ]
  }
}
```

**Error `400 Bad Request`** — if `range` is not one of the accepted values.
```json
{
  "code": "CONSTRAINT_VIOLATION",
  "message": "Constraint violation on request parameter",
  "details": ["getWanHistory.range: range must be one of: 1h, 6h, 24h"]
}
```

---

## Error Response Reference

All error responses share the same shape:

```json
{
  "code": "RESOURCE_NOT_FOUND",
  "message": "Human-readable description",
  "details": ["field1: message", "field2: message"]
}
```

`details` is omitted from the JSON when absent (not an empty array — the field itself is missing).

### Error Codes

| Code | HTTP | When |
|---|---|---|
| `RESOURCE_NOT_FOUND` | 404 | Requested entity does not exist |
| `ROUTE_NOT_FOUND` | 404 | No matching endpoint for the URL |
| `METHOD_NOT_ALLOWED` | 405 | Wrong HTTP verb for the endpoint |
| `VALIDATION_ERROR` | 400 | Business-level validation failure |
| `CONSTRAINT_VIOLATION` | 400 | `@Pattern`/`@NotBlank` on request param |
| `ILLEGAL_ARGUMENT` | 400 | Invalid argument (not domain-specific) |
| `TYPE_MISMATCH` | 400 | Path var or query param wrong type |
| `MISSING_PARAMETER` | 400 | Required query param absent |
| `MALFORMED_REQUEST` | 400 | Unparseable JSON body |
| `BUSINESS_ERROR` | 422 | Domain rule prevented the operation |
| `UNAUTHORIZED` | 401 | Missing or invalid credentials |
| `INTERNAL_ERROR` | 500 | Unexpected server fault |
