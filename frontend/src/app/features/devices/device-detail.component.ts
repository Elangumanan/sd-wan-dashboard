import {
  AfterViewInit,
  Component,
  computed,
  effect,
  ElementRef,
  inject,
  OnDestroy,
  OnInit,
  signal,
  ViewChild,
} from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
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
import { DeviceDetail, NetworkInterface, WanHistory, WanHistoryRange } from '../../core/models/device.model';
import { SdwanApiService } from '../../core/sdwan-api.service';
import { CardComponent } from '../../shared/components/card/card.component';
import { TileComponent, TileType } from '../../shared/components/tile/tile.component';

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
  private readonly api    = inject(SdwanApiService);
  private readonly route  = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly device         = signal<DeviceDetail | null>(null);
  protected readonly wanHistory     = signal<WanHistory | null>(null);
  protected readonly selectedRange  = signal<WanHistoryRange>('6h');
  protected readonly loading        = signal(true);
  protected readonly historyLoading = signal(false);
  protected readonly error          = signal<string | null>(null);

  protected readonly wanInterfaces = computed(() =>
    this.device()?.interfaces.filter(i => i.type === 'WAN') ?? [] as NetworkInterface[],
  );
  protected readonly lanInterfaces = computed(() =>
    this.device()?.interfaces.filter(i => i.type === 'LAN') ?? [] as NetworkInterface[],
  );

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

    this.api.getDevice(this.orgId, this.siteId, this.deviceId).subscribe({
      next: device => {
        this.device.set(device);
        this.loading.set(false);
      },
      error: (err: unknown) => {
        this.error.set(err instanceof Error ? err.message : 'Failed to load device');
        this.loading.set(false);
      },
    });

    this.fetchWanHistory('6h');

    this.api.getSite(this.orgId, this.siteId).subscribe({
      next:  s  => { this.siteName = s.name; },
      error: () => { this.siteName = this.siteId; },
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
    this.selectedRange.set(range);
    this.fetchWanHistory(range);
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

  private fetchWanHistory(range: WanHistoryRange): void {
    this.historyLoading.set(true);
    this.api.getWanHistory(this.orgId, this.siteId, this.deviceId, range).subscribe({
      next: history => {
        this.wanHistory.set(history);
        this.historyLoading.set(false);
      },
      error: () => this.historyLoading.set(false),
    });
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
    const ms = ts > 1e10 ? ts : ts * 1000;
    const d  = new Date(ms);
    return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`;
  }
}
