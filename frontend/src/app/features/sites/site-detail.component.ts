import { Component, inject, OnInit } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { DeviceSummary } from '../../core/models/site.model';
import { TileComponent } from '../../shared/components/tile/tile.component';
import { SiteActions } from './store/site.actions';
import {
  selectSite,
  selectSiteDevices,
  selectSiteError,
  selectSiteLoading,
  selectOnlineCount,
  selectOfflineCount,
} from './store/site.selectors';

@Component({
  selector: 'app-site-detail',
  standalone: true,
  imports: [RouterLink, TileComponent],
  templateUrl: './site-detail.component.html',
  styleUrl: './site-detail.component.scss',
})
export class SiteDetailComponent implements OnInit {
  private readonly store = inject(Store);
  private readonly route = inject(ActivatedRoute);

  protected readonly site         = toSignal(this.store.select(selectSite));
  protected readonly devices      = toSignal(this.store.select(selectSiteDevices), { initialValue: [] as DeviceSummary[] });
  protected readonly loading      = toSignal(this.store.select(selectSiteLoading), { initialValue: false });
  protected readonly error        = toSignal(this.store.select(selectSiteError));
  protected readonly onlineCount  = toSignal(this.store.select(selectOnlineCount),  { initialValue: 0 });
  protected readonly offlineCount = toSignal(this.store.select(selectOfflineCount), { initialValue: 0 });

  protected orgId  = '';
  protected siteId = '';

  ngOnInit(): void {
    this.orgId  = this.route.snapshot.params['orgId']  as string;
    this.siteId = this.route.snapshot.params['siteId'] as string;
    this.store.dispatch(SiteActions.loadSite({ orgId: this.orgId, siteId: this.siteId }));
  }
}
