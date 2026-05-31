import { createReducer, on } from '@ngrx/store';
import { OrganizationSummary } from '../../../core/models/organization.model';
import { SiteListItem } from '../../../core/models/site.model';
import { OrganizationActions } from './organization.actions';

export interface OrganizationState {
  organization: OrganizationSummary | null;
  sites:        SiteListItem[];
  loading:      boolean;
  error:        string | null;
}

export const initialOrganizationState: OrganizationState = {
  organization: null,
  sites:        [],
  loading:      false,
  error:        null,
};

export const organizationReducer = createReducer(
  initialOrganizationState,

  on(OrganizationActions.loadOrganization, state => ({
    ...state, loading: true, error: null,
  })),
  on(OrganizationActions.loadOrganizationSuccess, (state, { organization }) => ({
    ...state, organization, loading: false,
  })),
  on(OrganizationActions.loadOrganizationFailure, (state, { error }) => ({
    ...state, error, loading: false,
  })),

  on(OrganizationActions.loadSites, state => ({
    ...state, loading: true, error: null,
  })),
  on(OrganizationActions.loadSitesSuccess, (state, { sites }) => ({
    ...state, sites, loading: false,
  })),
  on(OrganizationActions.loadSitesFailure, (state, { error }) => ({
    ...state, error, loading: false,
  })),
);
