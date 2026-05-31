import { createActionGroup, props } from '@ngrx/store';
import { SiteDetail } from '../../../core/models/site.model';

export const SiteActions = createActionGroup({
  source: 'Site',
  events: {
    'Load Site':         props<{ orgId: string; siteId: string }>(),
    'Load Site Success': props<{ site: SiteDetail }>(),
    'Load Site Failure': props<{ error: string }>(),
  },
});
