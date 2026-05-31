import { Routes } from '@angular/router';

/** Root routes — every feature is lazy-loaded. */
export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'dashboard',
  },
  {
    path: 'dashboard',
    loadChildren: () =>
      import('./features/dashboard/dashboard.routes').then(
        m => m.DASHBOARD_ROUTES,
      ),
  },
  {
    path: 'organizations',
    loadChildren: () =>
      import('./features/organizations/organizations.routes').then(
        m => m.ORGANIZATION_ROUTES,
      ),
  },
  {
    path: '**',
    loadComponent: () =>
      import('./shared/components/not-found/not-found.component').then(
        m => m.NotFoundComponent,
      ),
  },
];
