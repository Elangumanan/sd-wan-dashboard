import { Routes } from '@angular/router';
import { provideEffects } from '@ngrx/effects';
import { provideState } from '@ngrx/store';
import * as DashboardEffects from './store/dashboard.effects';
import { dashboardReducer } from './store/dashboard.reducer';

/**
 * Feature routes for the Dashboard.
 * State and effects are registered here so they are only loaded when
 * the user navigates to /dashboard — true lazy initialisation.
 */
export const DASHBOARD_ROUTES: Routes = [
  {
    path: '',
    providers: [
      provideState('dashboard', dashboardReducer),
      provideEffects(DashboardEffects),
    ],
    loadComponent: () =>
      import('./dashboard.component').then(m => m.DashboardComponent),
  },
];
