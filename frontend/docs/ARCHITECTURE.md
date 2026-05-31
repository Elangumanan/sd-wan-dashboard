# Frontend Architecture

## Stack

| Concern | Technology |
|---|---|
| Framework | Angular 17.3 (standalone components) |
| Language | TypeScript 5.4 (strict mode) |
| State | Angular Signals — `signal()`, `computed()`, `effect()` |
| HTTP | `HttpClient` via `SdwanApiService` (single gateway) |
| Charts | Chart.js 4 (tree-shaken — only used controllers registered) |
| Styles | Component-scoped SCSS (BEM naming) |
| Build | Angular CLI / `@angular-devkit/build-angular` |

---

## Folder Structure

```
src/app/
├── app.component.ts/html/scss   Shell — sidebar + topbar layout
├── app.config.ts                ApplicationConfig (router, HttpClient, interceptor)
├── app.routes.ts                Root lazy routes
│
├── core/
│   ├── interceptors/
│   │   └── api.interceptor.ts   Attaches JSON headers; surfaces HTTP errors as toasts
│   ├── models/
│   │   ├── api-response.model.ts  SuccessResponse<T> / ApiErrorResponse
│   │   ├── dashboard.model.ts     DashboardOverview, SiteHealthSnapshot
│   │   ├── device.model.ts        DeviceDetail, NetworkInterface, WanHistory, DeviceRole
│   │   ├── organization.model.ts  OrganizationSummary
│   │   └── site.model.ts          SiteListItem, SiteDetail, DeviceSummary
│   ├── sdwan-api.service.ts       Single HTTP gateway for all backend calls
│   └── services/
│       └── toast.service.ts       Signal-based global notification service
│
├── features/
│   ├── dashboard/
│   │   ├── dashboard.component.ts/html/scss   Overview page
│   │   └── dashboard.routes.ts
│   ├── organizations/
│   │   ├── organization-list.component.ts/html/scss  Redirect to first org
│   │   ├── organization-detail.component.ts/html/scss
│   │   └── organizations.routes.ts
│   ├── sites/
│   │   └── site-detail.component.ts/html/scss
│   └── devices/
│       └── device-detail.component.ts/html/scss
│
└── shared/
    └── components/
        ├── card/         CardComponent — titled panel with optional header action slot
        ├── donut-chart/  DonutChartComponent — Chart.js doughnut, tree-shaken
        ├── not-found/    NotFoundComponent
        ├── tile/         TileComponent — stat/info card, 5 type accents, compact mode
        └── toast/        ToastComponent — fixed overlay, reads ToastService signal
```

---

## Application Shell

`AppComponent` owns the full-page layout: a fixed left sidebar and a scrollable right content area. All page content is rendered inside `<router-outlet>` — no page-level HTML lives in `AppComponent` beyond the shell.

```
┌─────────────────────────────────────────────────┐
│  Sidebar (148px)    │  Topbar (52px)             │
│  SD-WAN Dashboard   │  Operations Summary  N  U  │
│  ─────────────────  ├────────────────────────────│
│  Dashboard          │                            │
│  Organization       │   <router-outlet>          │
│  ─────────────────  │   (page-content area)      │
│  FLOW               │                            │
│  Org→Site→Device    │                            │
└─────────────────────────────────────────────────┘
```

---

## Routing

Routes are entirely lazy-loaded. The root router only declares two feature paths:

```
/                    → redirect to /dashboard
/dashboard           → DashboardComponent
/organizations       → OrganizationListComponent (redirects to first org)
/organizations/:orgId                          → OrganizationDetailComponent
/organizations/:orgId/sites/:siteId            → SiteDetailComponent
/organizations/:orgId/sites/:siteId/devices/:deviceId → DeviceDetailComponent
/**                  → NotFoundComponent
```

All four site/device routes live inside `ORGANIZATION_ROUTES` so the URL hierarchy mirrors the domain hierarchy (Org → Site → Device). Route parameters (`orgId`, `siteId`, `deviceId`) are read directly from `ActivatedRoute.snapshot.params` in each component.

---

## State Management

**No external state library.** State is managed entirely with Angular 17 Signals at the component level.

### Pattern per feature component

```typescript
// 1. Writable signals for raw API data
protected readonly device  = signal<DeviceDetail | null>(null);
protected readonly loading = signal(true);
protected readonly error   = signal<string | null>(null);

// 2. Derived/computed signals — run lazily, re-evaluate only when deps change
protected readonly wanInterfaces = computed(() =>
  this.device()?.interfaces.filter(i => i.type === 'WAN') ?? []
);

// 3. Fetch on init via direct service call
ngOnInit(): void {
  this.api.getDevice(orgId, siteId, deviceId).subscribe({
    next:  d   => { this.device.set(d);  this.loading.set(false); },
    error: err => { this.error.set(...); this.loading.set(false); },
  });
}
```

### Parallel requests (dashboard)

`forkJoin` fires both API calls simultaneously so the overview page resolves as fast as the slower of the two:

```typescript
forkJoin({
  overview:  this.api.getDashboardOverview(),
  snapshots: this.api.getSiteHealthSnapshot(),
}).subscribe({ next: ({ overview, snapshots }) => { ... } });
```

### Chart updates (device page)

`effect()` watches the `wanHistory` signal and rebuilds or updates the Chart.js instance reactively:

```typescript
constructor() {
  effect(() => {
    const h = this.wanHistory();
    if (!h) return;
    this.chart ? this.updateChart(h) : this.buildChart(h);
  });
}
```

---

## Core Layer

### `SdwanApiService`

Single HTTP gateway — the only file allowed to call `HttpClient`. Every method:
1. Calls the backend at `http://localhost:8080/api`
2. Unwraps the `SuccessResponse<T>` envelope via `.pipe(map(r => r.value))`
3. Returns the domain type directly to callers

| Method | Endpoint |
|---|---|
| `getHealth()` | `GET /api/health` |
| `getDashboardOverview()` | `GET /api/dashboard/overview` |
| `getSiteHealthSnapshot()` | `GET /api/dashboard/site-health` |
| `getOrganizations()` | `GET /api/organizations` |
| `getOrganization(orgId)` | `GET /api/organizations/:orgId` |
| `getSites(orgId)` | `GET /api/organizations/:orgId/sites` |
| `getSite(orgId, siteId)` | `GET /api/organizations/:orgId/sites/:siteId` |
| `getDevice(orgId, siteId, deviceId)` | `GET /api/.../devices/:deviceId` |
| `getWanHistory(orgId, siteId, deviceId, range)` | `GET /api/.../wan-history?range=` |

### `apiInterceptor`

Functional HTTP interceptor registered globally via `provideHttpClient(withInterceptors([apiInterceptor]))`. Responsibilities:
- Attaches `Content-Type: application/json` and `Accept: application/json` to every outbound request.
- Catches `HttpErrorResponse`, resolves a human-readable message, and calls `ToastService.error()`.
- Re-throws the error so the calling component's `error()` subscription also fires.

### `ToastService`

Signal-based (`signal<Toast[]>([])`). Components call `toast.success/error/warning/info(message)`. `ToastComponent` reads the signal directly — no `AsyncPipe`, no subscriptions.

---

## Shared Components

### `TileComponent`

Stat / info card. Used on every page for KPI display.

| Input | Type | Purpose |
|---|---|---|
| `title` | `string` (required) | Label above the value |
| `count` | `number \| string` (required) | Large displayed value |
| `type` | `TileType` | Left-border accent: `default \| success \| warning \| danger \| info` |
| `compact` | `boolean` | Smaller count font (1rem vs 2rem) — used for text values like role/uptime |
| `clickable` | `boolean` | Adds `role="button"`, hover lift, `tileClick` output |
| `subtitle` | `string?` | Secondary line below the count |
| `labelColor` | `string?` | Overrides the count colour via inline style |

`tileClasses` is a `computed()` that builds the BEM modifier string from `type`, `clickable`, and `compact` inputs.

### `CardComponent`

Reusable titled panel. Used for Port Status and Uplink Bandwidth sections on the device page.

```html
<app-card title="Port Status (Live)">
  <!-- body content via default ng-content slot -->
</app-card>

<app-card title="Uplink Bandwidth" description="WAN-only history...">
  <select card-actions ...></select>   <!-- projected into header-right slot -->
  <!-- chart canvas in body -->
</app-card>
```

Two `ng-content` slots: default (body) and `[card-actions]` (header right area).

### `DonutChartComponent`

Wraps Chart.js `DoughnutController`. Registered controllers: `DoughnutController`, `ArcElement`, `Tooltip`, `Legend` — nothing else loaded.

- Built-in legend disabled; callers render a custom HTML legend to the right (matching the mockup layout).
- `segments = input<DonutSegment[]>([])` — an `effect()` in the constructor watches the signal and calls `chart.update()` on change, replacing `ngOnChanges`.
- `centerValue` and `centerLabel` inputs drive the two-line overlay (large number + small label) positioned absolutely in the donut hole.

### `ToastComponent`

Fixed overlay mounted once in `AppComponent`. Reads `toastService.toasts()` signal directly with `@for` — no `AsyncPipe`. Slide-in CSS animation, auto-dismiss via `setTimeout` in `ToastService`.

---

## Data Flow Diagram

```
Backend API
    │
    ▼
SdwanApiService          ← single gateway, unwraps SuccessResponse<T>
    │
    ├── forkJoin / subscribe (in ngOnInit)
    │
    ▼
signal<T>()              ← writable, set() on API response
    │
    ├── computed()        ← derived values (segments, filtered interfaces, sorted lists)
    │
    ▼
Template (signal calls)  ← @if / @for / property bindings read signals directly
    │
    ├── TileComponent     ← stat/info tiles
    ├── DonutChartComponent  ← doughnut charts (effect() watches segments signal)
    ├── CardComponent     ← titled panels
    └── Chart.js canvas   ← line chart (effect() watches wanHistory signal)
```

---

## Conventions

### TypeScript
- Strict mode enforced — no `any`, all null/undefined cases handled.
- `input()` / `input.required()` / `output()` from `@angular/core` replace `@Input()` / `@Output()`.
- `computed()` replaces derived getters; `effect()` replaces `ngOnChanges` for reactive side effects.
- `toSignal()` is not used — API calls use `subscribe()` so loading/error state is explicitly controlled.

### Styles
- BEM naming: `.block__element--modifier`.
- SCSS nesting with `&` for modifiers/elements.
- Component styles are scoped — no global CSS beyond `styles.css` (Angular default).
- Design tokens: consistent colour palette across components (`#16a34a` healthy, `#d97706` degraded, `#dc2626` down/danger, `#2563eb` info/link).

### File structure
- Every component uses three files: `.ts` (class), `.html` (template), `.scss` (styles).
- Feature routes live in `<feature>.routes.ts` alongside their components.
- Shared components live in `shared/components/<name>/`.

### API calls
- All HTTP calls go through `SdwanApiService` — no `HttpClient` calls in components.
- `forkJoin` for parallel independent calls; sequential `subscribe` chains for dependent calls.
- Errors are handled at the component level (set `error` signal) and globally (interceptor toast).
