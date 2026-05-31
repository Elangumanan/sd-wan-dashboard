import { Routes } from '@angular/router';
import { provideEffects } from '@ngrx/effects';
import { provideState } from '@ngrx/store';
import * as DeviceEffects from '../devices/store/device.effects';
import { deviceReducer } from '../devices/store/device.reducer';
import * as OrganizationEffects from './store/organization.effects';
import { organizationReducer } from './store/organization.reducer';
import * as SiteEffects from '../sites/store/site.effects';
import { siteReducer } from '../sites/store/site.reducer';

export const ORGANIZATION_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./organization-list.component').then(m => m.OrganizationListComponent),
  },
  {
    path: ':orgId',
    providers: [
      provideState('organization', organizationReducer),
      provideEffects(OrganizationEffects),
    ],
    loadComponent: () =>
      import('./organization-detail.component').then(m => m.OrganizationDetailComponent),
  },
  {
    path: ':orgId/sites/:siteId',
    providers: [
      provideState('site', siteReducer),
      provideEffects(SiteEffects),
    ],
    loadComponent: () =>
      import('../sites/site-detail.component').then(m => m.SiteDetailComponent),
  },
  {
    path: ':orgId/sites/:siteId/devices/:deviceId',
    providers: [
      provideState('device', deviceReducer),
      provideEffects(DeviceEffects),
    ],
    loadComponent: () =>
      import('../devices/device-detail.component').then(m => m.DeviceDetailComponent),
  },
];
