import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { Store } from '@ngrx/store';
import { DonutChartComponent } from '../../shared/components/donut-chart/donut-chart.component';
import { TileComponent } from '../../shared/components/tile/tile.component';
import { SiteHealthSnapshot } from '../../core/models/dashboard.model';
import { DashboardActions } from './store/dashboard.actions';
import {
  selectDeviceDonutSegments,
  selectDashboardLoading,
  selectOverview,
  selectSiteDonutSegments,
  selectSiteHealthSnapshot,
} from './store/dashboard.selectors';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, DonutChartComponent, TileComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  private readonly store = inject(Store);

  protected readonly overview       = toSignal(this.store.select(selectOverview));
  protected readonly snapshots      = toSignal(
    this.store.select(selectSiteHealthSnapshot),
    { initialValue: [] as SiteHealthSnapshot[] },
  );
  protected readonly isLoading      = toSignal(
    this.store.select(selectDashboardLoading),
    { initialValue: false },
  );
  protected readonly siteSegments   = toSignal(
    this.store.select(selectSiteDonutSegments),
    { initialValue: [] },
  );
  protected readonly deviceSegments = toSignal(
    this.store.select(selectDeviceDonutSegments),
    { initialValue: [] },
  );

  ngOnInit(): void {
    this.store.dispatch(DashboardActions.loadOverview());
    this.store.dispatch(DashboardActions.loadSiteHealth());
  }

  protected pct(value: number, total: number): string {
    if (total === 0) return '0';
    const result = (value / total) * 100;
    return Number.isInteger(result) ? result.toString() : result.toFixed(1);
  }
}
