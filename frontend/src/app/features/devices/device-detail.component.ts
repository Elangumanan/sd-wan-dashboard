import {
  AfterViewInit,
  Component,
  computed,
  effect,
  ElementRef,
  inject,
  OnDestroy,
  OnInit,
  ViewChild,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import {
  CategoryScale,
  Chart,
  type ChartData,
  Filler,
  Legend,
  LinearScale,
  LineController,
  LineElement,
  PointElement,
  Tooltip,
} from 'chart.js';
import { NetworkInterface, WanHistory, WanHistoryRange } from '../../core/models/device.model';
import { SdwanApiService } from '../../core/sdwan-api.service';
import { CardComponent } from '../../shared/components/card/card.component';
import { TileComponent, TileType } from '../../shared/components/tile/tile.component';
import { DeviceActions } from './store/device.actions';
import {
  selectDevice,
  selectDeviceError,
  selectDeviceLoading,
  selectHistoryLoading,
  selectLanInterfaces,
  selectSelectedRange,
  selectWanHistory,
  selectWanInterfaces,
} from './store/device.selectors';

Chart.register(CategoryScale, LinearScale, LineController, LineElement, PointElement, Filler, Tooltip, Legend);

export interface WanStat {
  name: string;
  current: number;
  average: number;
  peak: number;
}

const LINE_COLORS = ['#2563eb', '#16a34a', '#d97706', '#dc2626'];

@Component({
  selector: 'app-device-detail',
  standalone: true,
  imports: [RouterLink, TileComponent, CardComponent],
  templateUrl: './device-detail.component.html',
  styleUrl: './device-detail.component.scss',
})
export class DeviceDetailComponent implements OnInit, AfterViewInit, OnDestroy {
  private readonly store  = inject(Store);
  private readonly route  = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api    = inject(SdwanApiService);

  protected readonly device         = toSignal(this.store.select(selectDevice));
  protected readonly wanHistory     = toSignal(this.store.select(selectWanHistory));
  protected readonly selectedRange  = toSignal(this.store.select(selectSelectedRange), { initialValue: '6h' as WanHistoryRange });
  protected readonly wanInterfaces  = toSignal(this.store.select(selectWanInterfaces),  { initialValue: [] as NetworkInterface[] });
  protected readonly lanInterfaces  = toSignal(this.store.select(selectLanInterfaces),  { initialValue: [] as NetworkInterface[] });
  protected readonly loading        = toSignal(this.store.select(selectDeviceLoading),  { initialValue: false });
  protected readonly historyLoading = toSignal(this.store.select(selectHistoryLoading), { initialValue: false });
  protected readonly error          = toSignal(this.store.select(selectDeviceError));

  protected readonly wanStats = computed<WanStat[]>(() => {
    const h = this.wanHistory();
    if (!h) return [];
    return h.interfaces.map(iface => {
      const tx   = iface.dataPoints.map(p => p.txMbps);
      const last = tx.at(-1) ?? 0;
      const avg  = tx.length ? tx.reduce((a, b) => a + b, 0) / tx.length : 0;
      const peak = tx.length ? Math.max(...tx) : 0;
      return { name: iface.interfaceName, current: Math.round(last), average: +avg.toFixed(1), peak: Math.round(peak) };
    });
  });

  protected orgId    = '';
  protected siteId   = '';
  protected deviceId = '';
  protected siteName = '';

  @ViewChild('wanChartCanvas')
  private canvasRef?: ElementRef<HTMLCanvasElement>;

  private chart: Chart<'line'> | null = null;

  constructor() {
    effect(() => {
      const h = this.wanHistory();
      if (!h) return;
      if (this.chart) {
        this.updateChart(h);
      } else if (this.canvasRef) {
        this.buildChart(h);
      }
    });
  }

  ngOnInit(): void {
    this.orgId    = this.route.snapshot.params['orgId']    as string;
    this.siteId   = this.route.snapshot.params['siteId']   as string;
    this.deviceId = this.route.snapshot.params['deviceId'] as string;

    this.store.dispatch(DeviceActions.loadDevice({ orgId: this.orgId, siteId: this.siteId, deviceId: this.deviceId }));
    this.store.dispatch(DeviceActions.loadWanHistory({ orgId: this.orgId, siteId: this.siteId, deviceId: this.deviceId, range: '6h' }));

    this.api.getSite(this.orgId, this.siteId).subscribe({
      next:  s     => { this.siteName = s.name; },
      error: ()    => { this.siteName = this.siteId; },
    });
  }

  ngAfterViewInit(): void {
    const h = this.wanHistory();
    if (h && this.canvasRef && !this.chart) {
      this.buildChart(h);
    }
  }

  ngOnDestroy(): void {
    this.chart?.destroy();
  }

  protected onRangeChange(event: Event): void {
    const range = (event.target as HTMLSelectElement).value as WanHistoryRange;
    this.store.dispatch(DeviceActions.loadWanHistory({
      orgId: this.orgId, siteId: this.siteId, deviceId: this.deviceId, range,
    }));
  }

  protected interfaceStatus(iface: NetworkInterface): string {
    const s = iface.status?.toUpperCase() ?? '';
    return (s === 'UP' || s === 'ONLINE') ? 'UP' : 'DOWN';
  }

  protected navigateToSite(): void {
    this.router.navigate(['/organizations', this.orgId, 'sites', this.siteId]);
  }

  protected wanTileType(index: number): TileType {
    const types: TileType[] = ['info', 'success', 'warning', 'danger'];
    return types[index] ?? 'default';
  }

  private buildChart(history: WanHistory): void {
    if (!this.canvasRef) return;
    const ctx = this.canvasRef.nativeElement.getContext('2d');
    if (!ctx) return;

    const { labels, datasets } = this.toChartData(history);
    this.chart = new Chart<'line'>(ctx, {
      type: 'line',
      data: { labels, datasets },
      options: this.chartOptions(),
    });
  }

  private updateChart(history: WanHistory): void {
    if (!this.chart) return;
    const { labels, datasets } = this.toChartData(history);
    this.chart.data.labels   = labels;
    this.chart.data.datasets = datasets;
    this.chart.update();
  }

  private toChartData(history: WanHistory): ChartData<'line', number[]> {
    if (!history.interfaces.length) return { labels: [], datasets: [] };
    const labels = history.interfaces[0].dataPoints.map(p => this.fmtTs(p.timestamp));
    const datasets = history.interfaces.map((iface, i) => ({
      label:           iface.interfaceName,
      data:            iface.dataPoints.map(p => p.txMbps),
      borderColor:     LINE_COLORS[i] ?? '#94a3b8',
      backgroundColor: 'transparent',
      borderWidth:     2,
      pointRadius:     0,
      tension:         0.4,
    }));
    return { labels, datasets };
  }

  private chartOptions(): object {
    return {
      responsive: true,
      maintainAspectRatio: false,
      interaction: { mode: 'index', intersect: false },
      scales: {
        x: {
          ticks: { maxTicksLimit: 8, font: { size: 10 }, color: '#94a3b8' },
          grid:  { display: false },
        },
        y: {
          beginAtZero: true,
          ticks: { font: { size: 10 }, color: '#94a3b8', callback: (v: number | string) => `${v}` },
          grid:  { color: 'rgba(0,0,0,0.05)' },
        },
      },
      plugins: {
        legend: {
          display: true,
          position: 'top' as const,
          align:    'end' as const,
          labels:   { boxWidth: 24, font: { size: 11 }, usePointStyle: true },
        },
        tooltip: { enabled: true },
      },
    };
  }

  private fmtTs(ts: number): string {
    const ms   = ts > 1e10 ? ts : ts * 1000;
    const d    = new Date(ms);
    const hh   = d.getHours().toString().padStart(2, '0');
    const mm   = d.getMinutes().toString().padStart(2, '0');
    return `${hh}:${mm}`;
  }
}
