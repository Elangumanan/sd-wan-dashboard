import { DashboardState } from '../features/dashboard/store/dashboard.reducer';
import { DeviceState } from '../features/devices/store/device.reducer';
import { OrganizationState } from '../features/organizations/store/organization.reducer';
import { SiteState } from '../features/sites/store/site.reducer';

/**
 * Root application state.
 * Feature states are registered lazily in their own route providers via provideState().
 */
export interface AppState {
  dashboard:    DashboardState;
  organization: OrganizationState;
  site:         SiteState;
  device:       DeviceState;
}
