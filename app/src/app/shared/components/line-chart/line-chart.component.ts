import { Component, input, effect } from '@angular/core';

export interface SeriesPoint {
  date: string;
  value: number;
}

@Component({
  selector: 'app-line-chart',
  imports: [],
  templateUrl: './line-chart.component.html',
  styleUrls: ['./line-chart.component.css'],
})
export class LineChartComponent {
  data = input<SeriesPoint[]>([]);
  public points = '';

  // recompute `points` whenever `data()` changes
  private _p = effect(() => {
    const d = this.data() || [];
    if (!d || d.length === 0) {
      this.points = '';
      return;
    }

    const width = 400;
    const height = 120;
    const values = d.map((p) => p.value);
    const min = Math.min(...values);
    const max = Math.max(...values);
    const range = max - min || 1;
    const stepX = width / Math.max(1, d.length - 1);

    const pts = d.map((p, i) => {
      const x = i * stepX;
      const normalized = (p.value - min) / range;
      const y = height - normalized * height;
      return `${x},${y}`;
    });

    this.points = pts.join(' ');
  });
}
