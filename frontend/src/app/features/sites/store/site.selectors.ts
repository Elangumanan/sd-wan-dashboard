import { createFeatureSelector, createSelector } from '@ngrx/store';
import { SiteState } from './site.reducer';

export const selectSiteState =
  createFeatureSelector<SiteState>('site');

export const selectSite =
  createSelector(selectSiteState, s => s.site);

export const selectSiteLoading =
  createSelector(selectSiteState, s => s.loading);

export const selectSiteError =
  createSelector(selectSiteState, s => s.error);

export const selectSiteDevices =
  createSelector(selectSite, site => site?.devices ?? []);

export const selectOnlineCount =
  createSelector(selectSiteDevices, devices =>
    devices.filter(d => d.status === 'ONLINE').length,
  );

export const selectOfflineCount =
  createSelector(selectSiteDevices, devices =>
    devices.filter(d => d.status === 'OFFLINE').length,
  );
