import { Component, ElementRef, ViewChild, input, effect, OnDestroy } from '@angular/core';
import Chart, { ChartConfiguration, ChartType } from 'chart.js/auto';

@Component({
  selector: 'app-chart',
  standalone: true,
  template: `<canvas #chartCanvas></canvas>`,
  styles: [`:host { display: block; width: 100%; height: 100%; }`]
})
export class ChartComponent implements OnDestroy {
  @ViewChild('chartCanvas') chartCanvas!: ElementRef<HTMLCanvasElement>;
  
  type = input.required<ChartType>();
  data = input.required<any>();
  options = input<any>({});
  
  private chart?: Chart;

  constructor() {
    effect(() => {
      const type = this.type();
      const data = this.data();
      const options = this.options();
      
      if (this.chartCanvas) {
        this.updateChart(type, data, options);
      }
    });
  }

  private updateChart(type: ChartType, data: any, options: any) {
    if (this.chart) {
      this.chart.destroy();
    }

    this.chart = new Chart(this.chartCanvas.nativeElement, {
      type,
      data,
      options: {
        responsive: true,
        maintainAspectRatio: false,
        ...options
      }
    });
  }

  ngOnDestroy() {
    if (this.chart) {
      this.chart.destroy();
    }
  }
}
