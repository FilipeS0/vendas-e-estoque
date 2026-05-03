import { Component, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { FornecedorService, FornecedorItem } from '../services/fornecedor.service';

@Component({
  selector: 'app-fornecedor-list',
  imports: [MatCardModule, MatTableModule, MatIconModule, MatProgressSpinnerModule, MatChipsModule],
  templateUrl: './fornecedor-list.component.html',
  styleUrls: ['./fornecedor-list.component.css'],
})
export class FornecedorListComponent {
  private fornecedorService = inject(FornecedorService);

  dataSource = signal<FornecedorItem[]>([]);
  isLoading = signal(true);
  displayedColumns = ['nome', 'cnpj', 'telefone', 'email', 'ativo'];

  ngOnInit() {
    this.fornecedorService.getAll().subscribe({
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
