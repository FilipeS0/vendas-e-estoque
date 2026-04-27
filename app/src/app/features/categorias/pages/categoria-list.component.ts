import { Component, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { CategoriaService, CategoriaItem } from '../services/categoria.service';

@Component({
  selector: 'app-categoria-list',
  imports: [MatCardModule, MatTableModule, MatIconModule, MatProgressSpinnerModule, MatChipsModule],
  templateUrl: './categoria-list.component.html',
  styleUrls: ['./categoria-list.component.css'],
})
export class CategoriaListComponent {
  private categoriaService = inject(CategoriaService);

  dataSource = signal<CategoriaItem[]>([]);
  isLoading = signal(true);
  displayedColumns = ['nome', 'descricao', 'ativo'];

  ngOnInit() {
    this.categoriaService.getAll().subscribe({
      next: (data) => {
        this.dataSource.set(data);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      },
    });
  }
}
