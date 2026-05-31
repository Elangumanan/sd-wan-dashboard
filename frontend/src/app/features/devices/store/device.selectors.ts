import { createFeatureSelector, createSelector } from '@ngrx/store';
import { DeviceState } from './device.reducer';

export const selectDeviceState =
  createFeatureSelector<DeviceState>('device');

export const selectDevice =
  createSelector(selectDeviceState, s => s.device);

export const selectWanHistory =
  createSelector(selectDeviceState, s => s.wanHistory);

export const selectSelectedRange =
  createSelector(selectDeviceState, s => s.selectedRange);

export const selectDeviceLoading =
  createSelector(selectDeviceState, s => s.loading);

export const selectHistoryLoading =
  createSelector(selectDeviceState, s => s.historyLoading);

export const selectDeviceError =
  createSelector(selectDeviceState, s => s.error);

export const selectHistoryError =
  createSelector(selectDeviceState, s => s.historyError);

export const selectAllInterfaces =
  createSelector(selectDevice, d => d?.interfaces ?? []);

export const selectWanInterfaces =
  createSelector(selectAllInterfaces, ifaces =>
    ifaces.filter(i => i.type === 'WAN'),
  );

export const selectLanInterfaces =
  createSelector(selectAllInterfaces, ifaces =>
    ifaces.filter(i => i.type === 'LAN'),
  );
