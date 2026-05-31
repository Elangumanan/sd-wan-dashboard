import { Routes } from '@angular/router';

export const ORGANIZATION_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./organization-list.component').then(m => m.OrganizationListComponent),
  },
  {
    path: ':orgId',
    loadComponent: () =>
      import('./organization-detail.component').then(m => m.OrganizationDetailComponent),
  },
  {
    path: ':orgId/sites/:siteId',
    loadComponent: () =>
      import('../sites/site-detail.component').then(m => m.SiteDetailComponent),
  },
  {
    path: ':orgId/sites/:siteId/devices/:deviceId',
    loadComponent: () =>
      import('../devices/device-detail.component').then(m => m.DeviceDetailComponent),
  },
];
