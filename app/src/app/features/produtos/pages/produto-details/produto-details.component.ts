import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DestroyRef } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { ProdutoService } from '../../services/produto.service';
import { Produto } from '../../../../shared/index';

@Component({
  selector: 'app-produto-details',
  standalone: true,
  imports: [MatCardModule, MatButtonModule, MatIconModule, MatDividerModule, RouterModule],
  templateUrl: './produto-details.component.html',
  styleUrls: ['./produto-details.component.css']
})
export class ProdutoDetailsComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private produtoService = inject(ProdutoService);
  private destroyRef = inject(DestroyRef);

  produto = signal<Produto | null>(null);
  isLoading = signal(true);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.produtoService.getProdutoById(id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
        next: (res) => {
          this.produto.set(res);
          this.isLoading.set(false);
        },
        error: () => {
          this.router.navigate(['/produtos']);
        }
      });
    } else {
      this.router.navigate(['/produtos']);
    }
  }

  voltar(): void {
    this.router.navigate(['/produtos']);
  }
}
