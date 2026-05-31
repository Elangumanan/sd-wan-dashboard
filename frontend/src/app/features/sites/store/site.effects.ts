import { inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, map, of, switchMap } from 'rxjs';
import { SdwanApiService } from '../../../core/sdwan-api.service';
import { SiteActions } from './site.actions';

export const loadSite$ = createEffect(
  (actions$ = inject(Actions), api = inject(SdwanApiService)) =>
    actions$.pipe(
      ofType(SiteActions.loadSite),
      switchMap(({ orgId, siteId }) =>
        api.getSite(orgId, siteId).pipe(
          map(site => SiteActions.loadSiteSuccess({ site })),
          catchError((err: unknown) =>
            of(SiteActions.loadSiteFailure({
              error: err instanceof Error ? err.message : 'Failed to load site',
            })),
          ),
        ),
      ),
    ),
  { functional: true },
);
