import {
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  ViewChild,
  effect,
  input,
} from '@angular/core';
import {
  ArcElement,
  Chart,
  type ChartData,
  DoughnutController,
  Legend,
  Tooltip,
} from 'chart.js';

Chart.register(DoughnutController, ArcElement, Tooltip, Legend);

export interface DonutSegment {
  label: string;
  value: number;
  color: string;
}

@Component({
  selector: 'app-donut-chart',
  standalone: true,
  templateUrl: './donut-chart.component.html',
  styleUrl: './donut-chart.component.scss',
})
export class DonutChartComponent implements OnInit, OnDestroy {
  readonly segments    = input<DonutSegment[]>([]);
  readonly centerValue = input('');
  readonly centerLabel = input('');
  readonly width       = input(200);
  readonly height      = input(200);

  @ViewChild('chartCanvas', { static: true })
  private readonly canvasRef!: ElementRef<HTMLCanvasElement>;

  private chart: Chart<'doughnut'> | null = null;

  constructor() {
    // Re-render whenever segments change after initial build.
    effect(() => {
      this.segments(); // track
      if (this.chart) this.update();
    });
  }

  ngOnInit(): void {
    this.build();
  }

  ngOnDestroy(): void {
    this.chart?.destroy();
  }

  private build(): void {
    const canvas = this.canvasRef.nativeElement;
    canvas.width  = this.width();
    canvas.height = this.height();

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    this.chart = new Chart<'doughnut'>(ctx, {
      type: 'doughnut',
      data: this.toChartData(),
      options: {
        responsive: false,
        cutout: '65%',
        plugins: {
          legend:  { display: false },
          tooltip: { enabled: true },
        },
      },
    });
  }

  private update(): void {
    if (!this.chart) return;
    this.chart.data = this.toChartData();
    this.chart.update();
  }

  private toChartData(): ChartData<'doughnut'> {
    const segs = this.segments();
    return {
      labels: segs.map(s => s.label),
      datasets: [{
        data:            segs.map(s => s.value),
        backgroundColor: segs.map(s => s.color),
        borderWidth: 2,
        borderColor: '#fff',
      }],
    };
  }
}
