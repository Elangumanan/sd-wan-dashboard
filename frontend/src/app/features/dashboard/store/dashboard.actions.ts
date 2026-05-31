import { createActionGroup, emptyProps, props } from '@ngrx/store';
import { DashboardOverview, SiteHealthSnapshot } from '../../../core/models/dashboard.model';

export const DashboardActions = createActionGroup({
  source: 'Dashboard',
  events: {
    // Overview card + donut charts
    'Load Overview':         emptyProps(),
    'Load Overview Success': props<{ overview: DashboardOverview }>(),
    'Load Overview Failure': props<{ error: string }>(),

    // Site health snapshot table
    'Load Site Health':         emptyProps(),
    'Load Site Health Success': props<{ snapshots: SiteHealthSnapshot[] }>(),
    'Load Site Health Failure': props<{ error: string }>(),
  },
});
