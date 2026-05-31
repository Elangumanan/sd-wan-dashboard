import { createActionGroup, props } from '@ngrx/store';
import { DeviceDetail, WanHistory, WanHistoryRange } from '../../../core/models/device.model';

export const DeviceActions = createActionGroup({
  source: 'Device',
  events: {
    'Load Device':         props<{ orgId: string; siteId: string; deviceId: string }>(),
    'Load Device Success': props<{ device: DeviceDetail }>(),
    'Load Device Failure': props<{ error: string }>(),

    'Load Wan History':         props<{ orgId: string; siteId: string; deviceId: string; range: WanHistoryRange }>(),
    'Load Wan History Success': props<{ wanHistory: WanHistory }>(),
    'Load Wan History Failure': props<{ error: string }>(),
  },
});
