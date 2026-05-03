import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { ActivatedRoute, Router } from '@angular/router';
import { Categoria, Fornecedor, HistoricoPreco, ProdutoService } from '../../services/produto.service';
import { CurrencyPipe, DatePipe } from '@angular/common';

@Component({
  selector: 'app-produto-create',
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatDividerModule,
    CurrencyPipe,
    DatePipe,
  ],
  templateUrl: './produto-create.component.html',
  styleUrls: ['./produto-create.component.css'],
})
export class ProdutoCreateComponent {
  private fb = inject(FormBuilder);
  private produtoService = inject(ProdutoService);
  private snackBar = inject(MatSnackBar);
  public router = inject(Router);
  private route = inject(ActivatedRoute);

  produtoForm: FormGroup = this.fb.group({
    codigoInterno: ['', Validators.required],
    codigoBarras: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(13)]],
    nome: ['', Validators.required],
    descricao: [''],
    unidadeMedida: ['UN', Validators.required],
    categoriaId: ['', Validators.required],
    fornecedorId: ['', Validators.required],
    precoCusto: [0, [Validators.required, Validators.min(0)]],
    precoVenda: [0, [Validators.required, Validators.min(0.01)]],
    ncm: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(8)]],
    cest: [''],
    cfop: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(4)]],
    situacaoTributaria: [''],
    aliquotaIcms: [0],
    aliquotaPis: [0],
    aliquotaCofins: [0],
  });

  categorias = signal<Categoria[]>([]);
  fornecedores = signal<Fornecedor[]>([]);
  isLoading = signal<boolean>(false);
  editMode = signal<boolean>(false);
  produtoId = signal<string | null>(null);
  selectedFile = signal<File | null>(null);
  imagePreview = signal<string | null>(null);
  historicoPrecos = signal<HistoricoPreco[]>([]);

  ngOnInit() {
    this.produtoService.getCategorias().subscribe((res) => this.categorias.set(res));
    this.produtoService.getFornecedores().subscribe((res) => this.fornecedores.set(res));

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.editMode.set(true);
      this.produtoId.set(id);
      this.loadProduto(id);

      this.produtoForm.get('codigoInterno')?.disable();
      this.produtoForm.get('codigoBarras')?.disable();
    }
  }

  loadProduto(id: string) {
    this.isLoading.set(true);
    this.produtoService.getProdutoById(id).subscribe({
      next: (produto) => {
        this.produtoForm.patchValue(produto);
        if (produto.imagemUrl) {
          // Point to our backend serve endpoint
          this.imagePreview.set(`${this.produtoService.baseUrl}${produto.imagemUrl}`);
        }
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.snackBar.open('Erro ao carregar produto.', 'OK', { duration: 3000 });
        this.router.navigate(['/produtos']);
      },
    });

    this.produtoService.getHistoricoPrecos(id).subscribe((res) => {
      this.historicoPrecos.set(res);
    });
  }

  onSubmit() {
    if (this.produtoForm.valid) {
      this.isLoading.set(true);

      const formValue = this.produtoForm.getRawValue();

      const request$ = this.editMode()
        ? this.produtoService.update(this.produtoId()!, formValue)
        : this.produtoService.create(formValue);

      request$.subscribe({
        next: (response: any) => {
          const productId = this.editMode() ? this.produtoId()! : response.id;
          const file = this.selectedFile();

          if (file) {
            this.produtoService.uploadImagem(productId, file).subscribe({
              next: () => this.finalizeSubmit(),
              error: () => {
                this.snackBar.open('Produto salvo, mas erro ao subir imagem.', 'OK', {
                  duration: 5000,
                });
                this.finalizeSubmit();
              },
            });
          } else {
            this.finalizeSubmit();
          }
        },
        error: (err) => {
          this.isLoading.set(false);
          const errorMsg = err.error?.message || 'Erro ao salvar produto.';
          this.snackBar.open(errorMsg, 'OK', { duration: 5000, panelClass: ['error-snackbar'] });
        },
      });
    } else {
      this.produtoForm.markAllAsTouched();
    }
  }

  private finalizeSubmit() {
    this.isLoading.set(false);
    const msg = this.editMode()
      ? 'Produto atualizado com sucesso!'
      : 'Produto cadastrado com sucesso!';
    this.snackBar.open(msg, 'OK', { duration: 3000 });
    this.router.navigate(['/produtos']);
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile.set(file);
      const reader = new FileReader();
      reader.onload = () => {
        this.imagePreview.set(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  }
}
