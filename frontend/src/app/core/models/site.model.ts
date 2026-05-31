export type SiteHealth = 'HEALTHY' | 'DEGRADED' | 'DOWN';

export interface SiteListItem {
  id: string;
  orgId: string;
  name: string;
  location: string;
  health: SiteHealth;
  deviceCount: number;
  onlineDeviceCount: number;
  offlineDeviceCount: number;
}

export interface DeviceSummary {
  id: string;
  siteId: string;
  name: string;
  model: string;
  status: 'ONLINE' | 'OFFLINE';
  ipAddress: string;
  role?: 'ACTIVE' | 'STANDBY';
  uptime?: string;
}

export interface SiteDetail extends SiteListItem {
  devices: DeviceSummary[];
}
