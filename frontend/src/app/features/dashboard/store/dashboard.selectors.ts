import { createFeatureSelector, createSelector } from '@ngrx/store';
import { DonutSegment } from '../../../shared/components/donut-chart/donut-chart.component';
import { DashboardOverview } from '../../../core/models/dashboard.model';
import { DashboardState } from './dashboard.reducer';

export const selectDashboardState =
  createFeatureSelector<DashboardState>('dashboard');

export const selectOverview =
  createSelector(selectDashboardState, s => s.overview);

export const selectSiteHealthSnapshot =
  createSelector(selectDashboardState, s => s.siteHealthSnapshot);

export const selectOverviewLoading =
  createSelector(selectDashboardState, s => s.overviewLoading);

export const selectSnapshotLoading =
  createSelector(selectDashboardState, s => s.snapshotLoading);

/** True while either the overview or the snapshot is still loading. */
export const selectDashboardLoading = createSelector(
  selectOverviewLoading,
  selectSnapshotLoading,
  (a: boolean, b: boolean) => a || b,
);

/** Pre-shaped segments for the Sites donut chart. */
export const selectSiteDonutSegments = createSelector(
  selectOverview,
  (overview: DashboardOverview | null): DonutSegment[] => {
    if (!overview) return [];
    const { healthy, degraded, down } = overview.siteStatusSummary;
    return [
      { label: 'Healthy',  value: healthy,  color: '#16a34a' },
      { label: 'Degraded', value: degraded, color: '#d97706' },
      { label: 'Down',     value: down,     color: '#dc2626' },
    ];
  },
);

/** Pre-shaped segments for the Devices donut chart. */
export const selectDeviceDonutSegments = createSelector(
  selectOverview,
  (overview: DashboardOverview | null): DonutSegment[] => {
    if (!overview) return [];
    const { online, offline } = overview.deviceStatusSummary;
    return [
      { label: 'Online',  value: online,  color: '#16a34a' },
      { label: 'Offline', value: offline, color: '#dc2626' },
    ];
  },
);
