import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { OrganizationSummary } from '../../core/models/organization.model';
import { SiteListItem, SiteHealth } from '../../core/models/site.model';
import { SdwanApiService } from '../../core/sdwan-api.service';
import { TileComponent } from '../../shared/components/tile/tile.component';

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
  private readonly api   = inject(SdwanApiService);
  private readonly route = inject(ActivatedRoute);

  protected readonly org     = signal<OrganizationSummary | null>(null);
  protected readonly loading = signal(true);
  protected readonly error   = signal<string | null>(null);

  private readonly sites = signal<SiteListItem[]>([]);

  protected readonly totalDevices = computed(() =>
    this.sites().reduce((sum, s) => sum + s.deviceCount, 0),
  );

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
    forkJoin({
      org:   this.api.getOrganization(orgId),
      sites: this.api.getSites(orgId),
    }).subscribe({
      next: ({ org, sites }) => {
        this.org.set(org);
        this.sites.set(sites);
        this.loading.set(false);
      },
      error: (err: unknown) => {
        this.error.set(err instanceof Error ? err.message : 'Failed to load organization');
        this.loading.set(false);
      },
    });
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
