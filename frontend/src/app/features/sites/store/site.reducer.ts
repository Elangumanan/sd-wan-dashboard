import { createReducer, on } from '@ngrx/store';
import { SiteDetail } from '../../../core/models/site.model';
import { SiteActions } from './site.actions';

export interface SiteState {
  site:    SiteDetail | null;
  loading: boolean;
  error:   string | null;
}

export const initialSiteState: SiteState = {
  site:    null,
  loading: false,
  error:   null,
};

export const siteReducer = createReducer(
  initialSiteState,
  on(SiteActions.loadSite, state => ({
    ...state, loading: true, error: null,
  })),
  on(SiteActions.loadSiteSuccess, (state, { site }) => ({
    ...state, site, loading: false,
  })),
  on(SiteActions.loadSiteFailure, (state, { error }) => ({
    ...state, error, loading: false,
  })),
);
