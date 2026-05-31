import { inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, map, of, switchMap } from 'rxjs';
import { SdwanApiService } from '../../../core/sdwan-api.service';
import { DeviceActions } from './device.actions';

export const loadDevice$ = createEffect(
  (actions$ = inject(Actions), api = inject(SdwanApiService)) =>
    actions$.pipe(
      ofType(DeviceActions.loadDevice),
      switchMap(({ orgId, siteId, deviceId }) =>
        api.getDevice(orgId, siteId, deviceId).pipe(
          map(device => DeviceActions.loadDeviceSuccess({ device })),
          catchError((err: unknown) =>
            of(DeviceActions.loadDeviceFailure({
              error: err instanceof Error ? err.message : 'Failed to load device',
            })),
          ),
        ),
      ),
    ),
  { functional: true },
);

export const loadWanHistory$ = createEffect(
  (actions$ = inject(Actions), api = inject(SdwanApiService)) =>
    actions$.pipe(
      ofType(DeviceActions.loadWanHistory),
      switchMap(({ orgId, siteId, deviceId, range }) =>
        api.getWanHistory(orgId, siteId, deviceId, range).pipe(
          map(wanHistory => DeviceActions.loadWanHistorySuccess({ wanHistory })),
          catchError((err: unknown) =>
            of(DeviceActions.loadWanHistoryFailure({
              error: err instanceof Error ? err.message : 'Failed to load WAN history',
            })),
          ),
        ),
      ),
    ),
  { functional: true },
);
