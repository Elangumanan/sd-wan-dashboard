import { SiteHealth } from './site.model';

export interface SiteStatusSummary {
  healthy: number;
  degraded: number;
  down: number;
  total: number;
}

export interface DeviceStatusSummary {
  online: number;
  offline: number;
  total: number;
}

export interface DashboardOverview {
  totalSites: number;
  totalEdgeDevices: number;
  siteStatusSummary: SiteStatusSummary;
  deviceStatusSummary: DeviceStatusSummary;
}

export interface SiteHealthSnapshot {
  siteId: string;
  siteName: string;
  healthStatus: SiteHealth;
  totalDevices: number;
  onlineDevices: number;
  offlineDevices: number;
}
