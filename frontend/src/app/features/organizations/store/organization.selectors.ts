import { createFeatureSelector, createSelector } from '@ngrx/store';
import { OrganizationState } from './organization.reducer';

export const selectOrganizationState =
  createFeatureSelector<OrganizationState>('organization');

export const selectOrganization =
  createSelector(selectOrganizationState, s => s.organization);

export const selectSites =
  createSelector(selectOrganizationState, s => s.sites);

export const selectOrganizationLoading =
  createSelector(selectOrganizationState, s => s.loading);

export const selectOrganizationError =
  createSelector(selectOrganizationState, s => s.error);

export const selectTotalEdgeDevices = createSelector(
  selectSites,
  sites => sites.reduce((sum, s) => sum + s.deviceCount, 0),
);
