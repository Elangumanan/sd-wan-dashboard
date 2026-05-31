export type DeviceStatus = 'ONLINE' | 'OFFLINE';
export type DeviceRole   = 'ACTIVE' | 'STANDBY';
export type InterfaceType  = 'WAN' | 'LAN';
export type WanHistoryRange = '1h' | '6h' | '24h';

export interface NetworkInterface {
  id: string;
  name: string;
  type: InterfaceType;
  status: string;
  ipAddress?: string;
  speedMbps: number;
  currentRxMbps: number;
  currentTxMbps: number;
}

export interface DeviceDetail {
  id: string;
  siteId: string;
  name: string;
  model: string;
  status: DeviceStatus;
  ipAddress: string;
  firmwareVersion: string;
  lastSeen: string;
  interfaces: NetworkInterface[];
  role?: DeviceRole;
  uptime?: string;
}

export interface WanDataPoint {
  timestamp: number;
  rxMbps: number;
  txMbps: number;
}

export interface WanInterfaceSeries {
  interfaceId: string;
  interfaceName: string;
  dataPoints: WanDataPoint[];
}

export interface WanHistory {
  deviceId: string;
  deviceName: string;
  range: string;
  interfaces: WanInterfaceSeries[];
}
