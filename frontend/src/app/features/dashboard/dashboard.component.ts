import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { DashboardOverview, SiteHealthSnapshot } from '../../core/models/dashboard.model';
import { SdwanApiService } from '../../core/sdwan-api.service';
import { DonutSegment } from '../../shared/components/donut-chart/donut-chart.component';
import { DonutChartComponent } from '../../shared/components/donut-chart/donut-chart.component';
import { TileComponent } from '../../shared/components/tile/tile.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, DonutChartComponent, TileComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  private readonly api = inject(SdwanApiService);

  protected readonly overview   = signal<DashboardOverview | null>(null);
  protected readonly snapshots  = signal<SiteHealthSnapshot[]>([]);
  protected readonly isLoading  = signal(true);
  protected readonly error      = signal<string | null>(null);

  protected readonly siteSegments = computed<DonutSegment[]>(() => {
    const ov = this.overview();
    if (!ov) return [];
    const { healthy, degraded, down } = ov.siteStatusSummary;
    return [
      { label: 'Healthy',  value: healthy,  color: '#16a34a' },
      { label: 'Degraded', value: degraded, color: '#d97706' },
      { label: 'Down',     value: down,     color: '#dc2626' },
    ];
  });

  protected readonly deviceSegments = computed<DonutSegment[]>(() => {
    const ov = this.overview();
    if (!ov) return [];
    const { online, offline } = ov.deviceStatusSummary;
    return [
      { label: 'Online',  value: online,  color: '#16a34a' },
      { label: 'Offline', value: offline, color: '#dc2626' },
    ];
  });

  ngOnInit(): void {
    forkJoin({
      overview:  this.api.getDashboardOverview(),
      snapshots: this.api.getSiteHealthSnapshot(),
    }).subscribe({
      next: ({ overview, snapshots }) => {
        this.overview.set(overview);
        this.snapshots.set(snapshots);
        this.isLoading.set(false);
      },
      error: (err: unknown) => {
        this.error.set(err instanceof Error ? err.message : 'Failed to load dashboard');
        this.isLoading.set(false);
      },
    });
  }

  protected pct(value: number, total: number): string {
    if (total === 0) return '0';
    const result = (value / total) * 100;
    return Number.isInteger(result) ? result.toString() : result.toFixed(1);
  }
}
