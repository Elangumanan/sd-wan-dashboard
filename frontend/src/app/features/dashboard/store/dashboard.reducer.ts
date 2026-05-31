import { createReducer, on } from '@ngrx/store';
import { DashboardOverview, SiteHealthSnapshot } from '../../../core/models/dashboard.model';
import { DashboardActions } from './dashboard.actions';

export interface DashboardState {
  overview:             DashboardOverview | null;
  siteHealthSnapshot:   SiteHealthSnapshot[];
  overviewLoading:      boolean;
  snapshotLoading:      boolean;
  overviewError:        string | null;
  snapshotError:        string | null;
}

export const initialDashboardState: DashboardState = {
  overview:           null,
  siteHealthSnapshot: [],
  overviewLoading:    false,
  snapshotLoading:    false,
  overviewError:      null,
  snapshotError:      null,
};

export const dashboardReducer = createReducer(
  initialDashboardState,

  on(DashboardActions.loadOverview, state => ({
    ...state, overviewLoading: true, overviewError: null,
  })),
  on(DashboardActions.loadOverviewSuccess, (state, { overview }) => ({
    ...state, overview, overviewLoading: false,
  })),
  on(DashboardActions.loadOverviewFailure, (state, { error }) => ({
    ...state, overviewError: error, overviewLoading: false,
  })),

  on(DashboardActions.loadSiteHealth, state => ({
    ...state, snapshotLoading: true, snapshotError: null,
  })),
  on(DashboardActions.loadSiteHealthSuccess, (state, { snapshots }) => ({
    ...state, siteHealthSnapshot: snapshots, snapshotLoading: false,
  })),
  on(DashboardActions.loadSiteHealthFailure, (state, { error }) => ({
    ...state, snapshotError: error, snapshotLoading: false,
  })),
);
