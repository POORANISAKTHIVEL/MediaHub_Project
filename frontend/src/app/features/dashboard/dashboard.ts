import { Component, HostListener, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { StatCard } from '../../shared/components/stat-card';
import { ToastService } from '../../shared/services/toast.service';

interface TrendPoint {
  label: string;
  value: number;
}

const TREND_DATA: Record<string, TrendPoint[]> = {
  'Last 3 months': [
    { label: 'May', value: 360 },
    { label: 'Jun', value: 430 },
    { label: 'Jul', value: 480 }
  ],
  'Last 6 months': [
    { label: 'Feb', value: 210 },
    { label: 'Mar', value: 260 },
    { label: 'Apr', value: 240 },
    { label: 'May', value: 360 },
    { label: 'Jun', value: 430 },
    { label: 'Jul', value: 480 }
  ],
  'Last 12 months': [
    { label: 'Aug', value: 120 },
    { label: 'Sep', value: 150 },
    { label: 'Oct', value: 140 },
    { label: 'Nov', value: 175 },
    { label: 'Dec', value: 190 },
    { label: 'Jan', value: 180 },
    { label: 'Feb', value: 210 },
    { label: 'Mar', value: 260 },
    { label: 'Apr', value: 240 },
    { label: 'May', value: 360 },
    { label: 'Jun', value: 430 },
    { label: 'Jul', value: 480 }
  ]
};

const CHART_X_START = 40;
const CHART_X_END = 616;
const CHART_Y_TOP = 20;
const CHART_Y_BASE = 200;

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, StatCard],
  templateUrl: './dashboard.html'
})
export class Dashboard {
  auth = inject(AuthService);
  private toast = inject(ToastService);

  durationOptions = Object.keys(TREND_DATA);
  duration = signal('Last 6 months');
  menuOpen = signal(false);

  trend = computed(() => TREND_DATA[this.duration()]);

  private coords = computed(() => {
    const data = this.trend();
    const values = data.map(d => d.value);
    const max = Math.max(...values);
    const min = Math.min(...values, 0);
    const range = max - min || 1;
    const step = data.length > 1 ? (CHART_X_END - CHART_X_START) / (data.length - 1) : 0;
    return data.map((d, i) => {
      const x = CHART_X_START + step * i;
      const y = CHART_Y_BASE - ((d.value - min) / range) * (CHART_Y_BASE - CHART_Y_TOP);
      return { x, y, label: d.label };
    });
  });

  linePoints = computed(() => this.coords().map(c => `${c.x},${c.y}`).join(' '));

  areaPoints = computed(() => {
    const pts = this.coords();
    if (!pts.length) return '';
    const first = pts[0];
    const last = pts[pts.length - 1];
    return `${first.x},${CHART_Y_BASE} ${this.linePoints()} ${last.x},${CHART_Y_BASE}`;
  });

  labelPositions = computed(() => this.coords().map(c => ({ x: c.x - 12, label: c.label })));

  toggleMenu(ev: Event) {
    ev.stopPropagation();
    this.menuOpen.set(!this.menuOpen());
  }

  selectDuration(d: string) {
    this.duration.set(d);
    this.menuOpen.set(false);
  }

  @HostListener('document:click')
  closeMenu() {
    this.menuOpen.set(false);
  }

  export() {
    const canContent = this.auth.hasPermission('content:read');
    const rows: string[][] = [
      ['Metric', 'Value'],
      ...(canContent ? [['Total Content', '2,847']] : []),
      ...(this.auth.hasPermission('subscription:manage') ? [['Active Subscriptions', '18,204']] : []),
      ...(canContent ? [['Pending Reviews', '37']] : []),
      ...(this.auth.hasPermission('royalty:view') ? [['Royalty Payable', '$94.2k']] : []),
      ...(canContent ? [
        [],
        ['Content Publishing Trend', this.duration()],
        ['Month', 'Content Published'],
        ...this.trend().map(t => [t.label, String(t.value)]),
        [],
        ['Content by Status', 'Count'],
        ['Published', '1940'],
        ['In Review', '312'],
        ['Scheduled', '168'],
        ['Draft', '427'],
        ...(this.auth.hasPermission('license:manage') ? [['Licenses expiring < 30d', '14']] : [])
      ] : [])
    ].map(row => row ?? []);

    const csv = rows.map(row => row.map(cell => `"${(cell ?? '').replace(/"/g, '""')}"`).join(',')).join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `mediahub-dashboard-${new Date().toISOString().slice(0, 10)}.csv`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
    this.toast.ok('Dashboard exported');
  }
}
