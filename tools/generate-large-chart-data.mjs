import { mkdirSync, writeFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';

const outputPath = resolve('mock-data/chart-api-datasets.json');
mkdirSync(dirname(outputPath), { recursive: true });

function createRng(seed) {
  let state = seed >>> 0;
  return () => {
    state = (1664525 * state + 1013904223) >>> 0;
    return state / 0x100000000;
  };
}

function round(value, digits = 2) {
  const factor = 10 ** digits;
  return Math.round(value * factor) / factor;
}

function buildDataset(config) {
  const rng = createRng(config.seed);
  const startTs = Date.parse(config.start);
  const stepMs = config.stepMs;
  const points = new Array(config.count);

  for (let index = 0; index < config.count; index += 1) {
    const ts = startTs + index * stepMs;
    const wave = Math.sin(index / config.waveDivisor);
    const noise = (rng() - 0.5) * config.noiseScale;
    points[index] = config.builder({ index, ts, wave, noise, rng });
  }

  return {
    api: config.api,
    chartType: config.chartType,
    pointCount: config.count,
    stepMs: config.stepMs,
    points
  };
}

const datasets = {
  siteHealthTrend100k: buildDataset({
    api: '/api/charts/site-health-trend?range=30d&resolution=raw',
    chartType: 'stacked-area',
    count: 100000,
    seed: 101,
    start: '2026-01-01T00:00:00.000Z',
    stepMs: 60_000,
    waveDivisor: 220,
    noiseScale: 1.6,
    builder: ({ ts, wave, noise, rng }) => {
      const healthy = Math.max(0, Math.min(120, Math.round(82 + wave * 16 + noise * 5)));
      const degraded = Math.max(0, Math.min(35, Math.round(11 - wave * 5 + noise * 2)));
      const down = Math.max(0, Math.min(15, Math.round(4 + (rng() - 0.5) * 2 + Math.abs(noise))));
      return { t: ts, healthy, degraded, down };
    }
  }),
  edgeAvailabilityTrend100k: buildDataset({
    api: '/api/charts/edge-availability-trend?range=30d&resolution=raw',
    chartType: 'stacked-bar',
    count: 100000,
    seed: 202,
    start: '2026-01-01T00:00:00.000Z',
    stepMs: 60_000,
    waveDivisor: 180,
    noiseScale: 2.0,
    builder: ({ ts, wave, noise }) => {
      const online = Math.max(0, Math.min(800, Math.round(690 + wave * 60 + noise * 10)));
      const offline = Math.max(0, Math.min(110, Math.round(42 - wave * 9 + Math.abs(noise * 4))));
      return { t: ts, online, offline };
    }
  }),
  wanBandwidthTrend100k: buildDataset({
    api: '/api/charts/wan-bandwidth?deviceId=mum-edge-01&range=24h&resolution=raw',
    chartType: 'dual-line',
    count: 100000,
    seed: 303,
    start: '2026-01-01T00:00:00.000Z',
    stepMs: 30_000,
    waveDivisor: 75,
    noiseScale: 6.0,
    builder: ({ ts, index, wave, noise, rng }) => {
      const burst = index % 2500 === 0 ? 24 : 0;
      const wan1 = round(Math.max(0, 68 + wave * 14 + noise + burst));
      const wan2 = round(Math.max(0, 39 + Math.cos(index / 88) * 10 + noise * 0.7 + (rng() - 0.5) * 3));
      return { t: ts, wan1, wan2 };
    }
  }),
  latencyTrend100k: buildDataset({
    api: '/api/charts/latency-trend?deviceId=mum-edge-01&range=24h&resolution=raw',
    chartType: 'line',
    count: 100000,
    seed: 404,
    start: '2026-01-01T00:00:00.000Z',
    stepMs: 30_000,
    waveDivisor: 95,
    noiseScale: 4.2,
    builder: ({ ts, index, wave, noise }) => {
      const spike = index % 12000 === 0 ? 35 : 0;
      const latencyMs = round(Math.max(2, 24 + wave * 7 + noise + spike));
      return { t: ts, latencyMs };
    }
  }),
  packetLossTrend100k: buildDataset({
    api: '/api/charts/packet-loss-trend?deviceId=mum-edge-01&range=24h&resolution=raw',
    chartType: 'line',
    count: 100000,
    seed: 505,
    start: '2026-01-01T00:00:00.000Z',
    stepMs: 30_000,
    waveDivisor: 140,
    noiseScale: 1.8,
    builder: ({ ts, index, wave, noise, rng }) => {
      const spike = index % 15000 === 0 ? rng() * 2.8 : 0;
      const packetLossPct = round(Math.max(0, 0.45 + Math.abs(wave) * 0.9 + Math.abs(noise) * 0.18 + spike), 3);
      return { t: ts, packetLossPct };
    }
  }),
  wanBandwidthTrend200k: buildDataset({
    api: '/api/charts/wan-bandwidth?deviceId=mum-edge-01&range=7d&resolution=raw',
    chartType: 'dual-line',
    count: 200000,
    seed: 606,
    start: '2026-01-01T00:00:00.000Z',
    stepMs: 15_000,
    waveDivisor: 130,
    noiseScale: 7.5,
    builder: ({ ts, index, wave, noise, rng }) => {
      const surge = index % 5000 === 0 ? 38 : 0;
      const dip = index % 3300 === 0 ? -16 : 0;
      const wan1 = round(Math.max(0, 74 + wave * 17 + noise + surge + dip));
      const wan2 = round(Math.max(0, 43 + Math.cos(index / 145) * 11 + noise * 0.8 + (rng() - 0.5) * 4));
      return { t: ts, wan1, wan2 };
    }
  })
};

const document = {
  generatedAt: new Date().toISOString(),
  description: 'Large deterministic datasets for SD-WAN chart API performance testing.',
  notes: [
    '100k datasets are intended for heavy frontend chart tests.',
    '200k WAN dataset is intended for worst-case stress testing.',
    'All timestamps are unix epoch milliseconds to keep payloads compact.'
  ],
  datasets
};

writeFileSync(outputPath, JSON.stringify(document));

const summary = Object.entries(datasets).map(([key, value]) => ({
  key,
  api: value.api,
  pointCount: value.pointCount
}));

console.log(JSON.stringify({ outputPath, datasets: summary }, null, 2));
