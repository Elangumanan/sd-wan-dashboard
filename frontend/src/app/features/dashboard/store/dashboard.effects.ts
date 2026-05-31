import { inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, map, of, switchMap } from 'rxjs';
import { SdwanApiService } from '../../../core/sdwan-api.service';
import { DashboardActions } from './dashboard.actions';

/**
 * Functional effects — registered via provideEffects([...]) in the route providers.
 * The HTTP interceptor already shows a toast on error, so effects only need to
 * dispatch failure actions for store state tracking.
 */

export const loadOverview$ = createEffect(
  (actions$ = inject(Actions), api = inject(SdwanApiService)) =>
    actions$.pipe(
      ofType(DashboardActions.loadOverview),
      switchMap(() =>
        api.getDashboardOverview().pipe(
          map(overview => DashboardActions.loadOverviewSuccess({ overview })),
          catchError((err: unknown) =>
            of(DashboardActions.loadOverviewFailure({
              error: err instanceof Error ? err.message : 'Failed to load overview',
            })),
          ),
        ),
      ),
    ),
  { functional: true },
);

export const loadSiteHealth$ = createEffect(
  (actions$ = inject(Actions), api = inject(SdwanApiService)) =>
    actions$.pipe(
      ofType(DashboardActions.loadSiteHealth),
      switchMap(() =>
        api.getSiteHealthSnapshot().pipe(
          map(snapshots => DashboardActions.loadSiteHealthSuccess({ snapshots })),
          catchError((err: unknown) =>
            of(DashboardActions.loadSiteHealthFailure({
              error: err instanceof Error ? err.message : 'Failed to load site health',
            })),
          ),
        ),
      ),
    ),
  { functional: true },
);
