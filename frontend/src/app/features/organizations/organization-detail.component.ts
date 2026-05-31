import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { SiteListItem, SiteHealth } from '../../core/models/site.model';
import { TileComponent } from '../../shared/components/tile/tile.component';
import { OrganizationActions } from './store/organization.actions';
import {
  selectOrganization,
  selectOrganizationError,
  selectOrganizationLoading,
  selectSites,
  selectTotalEdgeDevices,
} from './store/organization.selectors';

export type SortKey = 'name' | 'deviceCount' | 'health' | 'healthy' | 'degraded' | 'down';
export type SortDir = 'asc' | 'desc';

const HEALTH_ORDER: Record<SiteHealth, number> = { HEALTHY: 0, DEGRADED: 1, DOWN: 2 };

@Component({
  selector: 'app-organization-detail',
  standalone: true,
  imports: [RouterLink, TileComponent],
  templateUrl: './organization-detail.component.html',
  styleUrl: './organization-detail.component.scss',
})
export class OrganizationDetailComponent implements OnInit {
  private readonly store = inject(Store);
  private readonly route = inject(ActivatedRoute);

  protected readonly org          = toSignal(this.store.select(selectOrganization));
  protected readonly loading      = toSignal(this.store.select(selectOrganizationLoading), { initialValue: false });
  protected readonly error        = toSignal(this.store.select(selectOrganizationError));
  protected readonly totalDevices = toSignal(this.store.select(selectTotalEdgeDevices), { initialValue: 0 });
  private   readonly sites        = toSignal(this.store.select(selectSites), { initialValue: [] as SiteListItem[] });

  protected readonly sortKey = signal<SortKey>('name');
  protected readonly sortDir = signal<SortDir>('asc');

  protected readonly sortedSites = computed(() => {
    const dir = this.sortDir() === 'asc' ? 1 : -1;
    return [...this.sites()].sort((a, b) => {
      switch (this.sortKey()) {
        case 'name':        return dir * a.name.localeCompare(b.name);
        case 'deviceCount': return dir * (a.deviceCount - b.deviceCount);
        case 'health':      return dir * (HEALTH_ORDER[a.health] - HEALTH_ORDER[b.health]);
        case 'healthy':     return dir * (this.healthyCount(a)  - this.healthyCount(b));
        case 'degraded':    return dir * (this.degradedCount(a) - this.degradedCount(b));
        case 'down':        return dir * (this.downCount(a)     - this.downCount(b));
      }
    });
  });

  ngOnInit(): void {
    const orgId = this.route.snapshot.params['orgId'] as string;
    this.store.dispatch(OrganizationActions.loadOrganization({ orgId }));
    this.store.dispatch(OrganizationActions.loadSites({ orgId }));
  }

  protected sort(key: SortKey): void {
    if (this.sortKey() === key) {
      this.sortDir.update(d => d === 'asc' ? 'desc' : 'asc');
    } else {
      this.sortKey.set(key);
      this.sortDir.set('asc');
    }
  }

  protected sortIcon(key: SortKey): string {
    if (this.sortKey() !== key) return '';
    return this.sortDir() === 'asc' ? '▲' : '▼';
  }

  protected healthyCount(site: SiteListItem): number {
    return site.health === 'HEALTHY' ? site.onlineDeviceCount : 0;
  }

  protected degradedCount(site: SiteListItem): number {
    return site.health === 'DEGRADED' ? site.offlineDeviceCount : 0;
  }

  protected downCount(site: SiteListItem): number {
    return site.health === 'DOWN' ? site.offlineDeviceCount : 0;
  }
}
