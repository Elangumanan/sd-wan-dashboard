import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { map, Observable } from 'rxjs';
import { SuccessResponse } from './models/api-response.model';
import { DashboardOverview, SiteHealthSnapshot } from './models/dashboard.model';
import { DeviceDetail, WanHistory, WanHistoryRange } from './models/device.model';
import { OrganizationSummary } from './models/organization.model';
import { SiteDetail, SiteListItem } from './models/site.model';

/** Legacy shape — kept for the existing health-check view. */
export interface HealthResponse {
  status: string;
  service: string;
  timestamp: string;
  assignment: string;
}

/**
 * Single gateway for all backend API calls.
 * Every method unwraps the SuccessResponse<T> envelope so callers receive
 * the domain type directly.
 */
@Injectable({ providedIn: 'root' })
export class SdwanApiService {
  private readonly http = inject(HttpClient);
  private readonly base = 'http://localhost:8080/api';

  // ── Health ──────────────────────────────────────────────────────────────────

  getHealth(): Observable<HealthResponse> {
    return this.http.get<HealthResponse>(`${this.base}/health`);
  }

  // ── Dashboard ───────────────────────────────────────────────────────────────

  getDashboardOverview(): Observable<DashboardOverview> {
    return this.http
      .get<SuccessResponse<DashboardOverview>>(`${this.base}/dashboard/overview`)
      .pipe(map(r => r.value));
  }

  getSiteHealthSnapshot(): Observable<SiteHealthSnapshot[]> {
    return this.http
      .get<SuccessResponse<SiteHealthSnapshot[]>>(`${this.base}/dashboard/site-health`)
      .pipe(map(r => r.value));
  }

  // ── Organizations ───────────────────────────────────────────────────────────

  getOrganizations(): Observable<OrganizationSummary[]> {
    return this.http
      .get<SuccessResponse<OrganizationSummary[]>>(`${this.base}/organizations`)
      .pipe(map(r => r.value));
  }

  getOrganization(orgId: string): Observable<OrganizationSummary> {
    return this.http
      .get<SuccessResponse<OrganizationSummary>>(`${this.base}/organizations/${orgId}`)
      .pipe(map(r => r.value));
  }

  // ── Sites ───────────────────────────────────────────────────────────────────

  getSites(orgId: string): Observable<SiteListItem[]> {
    return this.http
      .get<SuccessResponse<SiteListItem[]>>(`${this.base}/organizations/${orgId}/sites`)
      .pipe(map(r => r.value));
  }

  getSite(orgId: string, siteId: string): Observable<SiteDetail> {
    return this.http
      .get<SuccessResponse<SiteDetail>>(`${this.base}/organizations/${orgId}/sites/${siteId}`)
      .pipe(map(r => r.value));
  }

  // ── Devices ─────────────────────────────────────────────────────────────────

  getDevice(orgId: string, siteId: string, deviceId: string): Observable<DeviceDetail> {
    return this.http
      .get<SuccessResponse<DeviceDetail>>(
        `${this.base}/organizations/${orgId}/sites/${siteId}/devices/${deviceId}`,
      )
      .pipe(map(r => r.value));
  }

  getWanHistory(
    orgId: string,
    siteId: string,
    deviceId: string,
    range: WanHistoryRange = '24h',
  ): Observable<WanHistory> {
    return this.http
      .get<SuccessResponse<WanHistory>>(
        `${this.base}/organizations/${orgId}/sites/${siteId}/devices/${deviceId}/wan-history`,
        { params: { range } },
      )
      .pipe(map(r => r.value));
  }
}
