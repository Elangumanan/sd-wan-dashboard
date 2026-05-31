import { createReducer, on } from '@ngrx/store';
import { DeviceDetail, WanHistory, WanHistoryRange } from '../../../core/models/device.model';
import { DeviceActions } from './device.actions';

export interface DeviceState {
  device:         DeviceDetail | null;
  wanHistory:     WanHistory | null;
  selectedRange:  WanHistoryRange;
  loading:        boolean;
  historyLoading: boolean;
  error:          string | null;
  historyError:   string | null;
}

export const initialDeviceState: DeviceState = {
  device:         null,
  wanHistory:     null,
  selectedRange:  '6h',
  loading:        false,
  historyLoading: false,
  error:          null,
  historyError:   null,
};

export const deviceReducer = createReducer(
  initialDeviceState,

  on(DeviceActions.loadDevice, state => ({
    ...state, loading: true, error: null,
  })),
  on(DeviceActions.loadDeviceSuccess, (state, { device }) => ({
    ...state, device, loading: false,
  })),
  on(DeviceActions.loadDeviceFailure, (state, { error }) => ({
    ...state, error, loading: false,
  })),

  on(DeviceActions.loadWanHistory, (state, { range }) => ({
    ...state, historyLoading: true, selectedRange: range, historyError: null,
  })),
  on(DeviceActions.loadWanHistorySuccess, (state, { wanHistory }) => ({
    ...state, wanHistory, historyLoading: false,
  })),
  on(DeviceActions.loadWanHistoryFailure, (state, { error }) => ({
    ...state, historyError: error, historyLoading: false,
  })),
);
