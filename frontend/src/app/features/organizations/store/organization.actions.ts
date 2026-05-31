import { createActionGroup, props } from '@ngrx/store';
import { OrganizationSummary } from '../../../core/models/organization.model';
import { SiteListItem } from '../../../core/models/site.model';

export const OrganizationActions = createActionGroup({
  source: 'Organization',
  events: {
    'Load Organization':         props<{ orgId: string }>(),
    'Load Organization Success': props<{ organization: OrganizationSummary }>(),
    'Load Organization Failure': props<{ error: string }>(),

    'Load Sites':         props<{ orgId: string }>(),
    'Load Sites Success': props<{ sites: SiteListItem[] }>(),
    'Load Sites Failure': props<{ error: string }>(),
  },
});
