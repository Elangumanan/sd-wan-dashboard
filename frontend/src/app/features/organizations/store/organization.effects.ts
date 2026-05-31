import { inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, map, of, switchMap } from 'rxjs';
import { SdwanApiService } from '../../../core/sdwan-api.service';
import { OrganizationActions } from './organization.actions';

export const loadOrganization$ = createEffect(
  (actions$ = inject(Actions), api = inject(SdwanApiService)) =>
    actions$.pipe(
      ofType(OrganizationActions.loadOrganization),
      switchMap(({ orgId }) =>
        api.getOrganization(orgId).pipe(
          map(organization => OrganizationActions.loadOrganizationSuccess({ organization })),
          catchError((err: unknown) =>
            of(OrganizationActions.loadOrganizationFailure({
              error: err instanceof Error ? err.message : 'Failed to load organization',
            })),
          ),
        ),
      ),
    ),
  { functional: true },
);

export const loadSites$ = createEffect(
  (actions$ = inject(Actions), api = inject(SdwanApiService)) =>
    actions$.pipe(
      ofType(OrganizationActions.loadSites),
      switchMap(({ orgId }) =>
        api.getSites(orgId).pipe(
          map(sites => OrganizationActions.loadSitesSuccess({ sites })),
          catchError((err: unknown) =>
            of(OrganizationActions.loadSitesFailure({
              error: err instanceof Error ? err.message : 'Failed to load sites',
            })),
          ),
        ),
      ),
    ),
  { functional: true },
);
