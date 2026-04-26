import { Component, DestroyRef, inject, signal, ViewChild } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { ProdutoService, ProdutoResponse } from '../../../services/produto.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-produto-list',
  imports: [
    MatTableModule,
    MatPaginatorModule,
    MatInputModule,
    MatFormFieldModule,
    MatIconModule,
    MatButtonModule,
    MatCardModule,
    MatSnackBarModule,
    ReactiveFormsModule,
    CurrencyPipe,
  ],
  templateUrl: './produto-list.component.html',
  styleUrls: ['./produto-list.component.css'],
})
export class ProdutoListComponent {
  private produtoService = inject(ProdutoService);
  private snackBar = inject(MatSnackBar);
  public router = inject(Router);
  private destroyRef = inject(DestroyRef);

  displayedColumns: string[] = [
    'codigoInterno',
    'codigoBarras',
    'nome',
    'categoriaNome',
    'precoVenda',
    'actions',
  ];
  dataSource = signal<ProdutoResponse[]>([]);
  totalElements = signal<number>(0);
  pageSize = signal<number>(10);
  currentPage = signal<number>(0);

  searchControl = new FormControl('');

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  ngOnInit() {
    this.loadProdutos();

    this.searchControl.valueChanges
      .pipe(debounceTime(400), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.currentPage.set(0); // Reset page on new search
        if (this.paginator) {
          this.paginator.pageIndex = 0;
        }
        this.loadProdutos();
      });
  }

  ngAfterViewInit() {
    if (this.paginator) {
      this.paginator.page
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe((event: PageEvent) => {
          this.currentPage.set(event.pageIndex);
          this.pageSize.set(event.pageSize);
          this.loadProdutos();
        });
    }
  }

  loadProdutos() {
    const searchTerm = this.searchControl.value || undefined;
    this.produtoService
      .getProdutos(searchTerm, this.currentPage(), this.pageSize())
      .subscribe((res) => {
        this.dataSource.set(res.content);
        this.totalElements.set(res.totalElements);
      });
  }

  editProduto(id: string) {
    this.router.navigate(['/produtos/editar', id]);
  }

  inativarProduto(id: string) {
    if (
      confirm('Tem certeza que deseja inativar este produto? Ele não será mais exibido nas buscas.')
    ) {
      this.produtoService.delete(id).subscribe({
        next: () => {
          this.snackBar.open('Produto inativado com sucesso!', 'OK', { duration: 3000 });
          this.loadProdutos();
        },
        error: (err) => {
          this.snackBar.open('Erro ao inativar produto.', 'OK', {
            duration: 3000,
            panelClass: ['error-snackbar'],
          });
        },
      });
    }
  }
}
