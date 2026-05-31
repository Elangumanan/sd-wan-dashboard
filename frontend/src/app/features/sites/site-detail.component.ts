import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { SiteDetail } from '../../core/models/site.model';
import { SdwanApiService } from '../../core/sdwan-api.service';
import { TileComponent } from '../../shared/components/tile/tile.component';

@Component({
  selector: 'app-site-detail',
  standalone: true,
  imports: [RouterLink, TileComponent],
  templateUrl: './site-detail.component.html',
  styleUrl: './site-detail.component.scss',
})
export class SiteDetailComponent implements OnInit {
  private readonly api   = inject(SdwanApiService);
  private readonly route = inject(ActivatedRoute);

  protected readonly site         = signal<SiteDetail | null>(null);
  protected readonly loading      = signal(true);
  protected readonly error        = signal<string | null>(null);

  protected readonly devices      = computed(() => this.site()?.devices ?? []);
  protected readonly onlineCount  = computed(() => this.devices().filter(d => d.status === 'ONLINE').length);
  protected readonly offlineCount = computed(() => this.devices().filter(d => d.status === 'OFFLINE').length);

  protected orgId  = '';
  protected siteId = '';

  ngOnInit(): void {
    this.orgId  = this.route.snapshot.params['orgId']  as string;
    this.siteId = this.route.snapshot.params['siteId'] as string;

    this.api.getSite(this.orgId, this.siteId).subscribe({
      next: site => {
        this.site.set(site);
        this.loading.set(false);
      },
      error: (err: unknown) => {
        this.error.set(err instanceof Error ? err.message : 'Failed to load site');
        this.loading.set(false);
      },
    });
  }
}
