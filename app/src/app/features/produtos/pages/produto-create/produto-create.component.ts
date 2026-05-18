import { Component, TemplateRef, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { Categoria, Fornecedor, Marca, HistoricoPreco, Ncm, ProdutoService } from '../../services/produto.service';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { catchError, debounceTime, distinctUntilChanged, filter, finalize, of, switchMap, tap } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

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
    MatDialogModule,
    MatAutocompleteModule,
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
  private dialog = inject(MatDialog);
  public router = inject(Router);
  private route = inject(ActivatedRoute);

  produtoForm: FormGroup = this.fb.group({
    codigoInterno: ['', Validators.required],
    codigoBarras: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(13)]],
    nome: ['', Validators.required],
    descricao: [''],
    unidadeMedida: ['UN', Validators.required],
    categoriaId: ['', Validators.required],
    marcaId: ['', Validators.required],
    fornecedorId: ['', Validators.required],
    precoCusto: [0, [Validators.required, Validators.min(0)]],
    precoVenda: [0, [Validators.required, Validators.min(0.01)]],
    margemLucro: [0],
    ncm: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(8)]],
    cest: [''],
    cfop: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(4)]],
    situacaoTributaria: [''],
    aliquotaIcms: [0],
    aliquotaPis: [0],
    aliquotaCofins: [0],
    origem: ['NACIONAL'],
    quantidadeInicial: [0],
  });

  categoriaForm = this.fb.group({
    nome: ['', Validators.required],
    descricao: [''],
  });

  marcaForm = this.fb.group({
    nome: ['', Validators.required],
  });

  categorias = signal<Categoria[]>([]);
  marcas = signal<Marca[]>([]);
  fornecedores = signal<Fornecedor[]>([]);
  isLoading = signal<boolean>(false);
  editMode = signal<boolean>(false);
  produtoId = signal<string | null>(null);
  selectedFile = signal<File | null>(null);
  imagePreview = signal<string | null>(null);
  historicoPrecos = signal<HistoricoPreco[]>([]);
  ncms = signal<Ncm[]>([]);
  ncmQuery = signal('');
  isSearchingNcm = signal(false);
  isLoadingCategorias = signal(false);
  categoriaLoadError = signal('');
  isLoadingMarcas = signal(false);
  marcaLoadError = signal('');
  isLoadingFornecedores = signal(false);
  fornecedorLoadError = signal('');

  constructor() {
    this.produtoForm
      .get('ncm')
      ?.valueChanges.pipe(
        takeUntilDestroyed(),
        debounceTime(300),
        distinctUntilChanged(),
        tap((value) => {
          const query = typeof value === 'string' ? value.trim() : '';
          this.ncmQuery.set(query);

          if (query.length < 3) {
            this.ncms.set([]);
          }
        }),
        filter((value) => typeof value === 'string' && value.trim().length >= 3),
        tap(() => this.isSearchingNcm.set(true)),
        switchMap((query) =>
          this.produtoService.searchNcms(query.trim()).pipe(
            catchError(() => of([])),
            finalize(() => this.isSearchingNcm.set(false))
          )
        )
      )
      .subscribe((results) => this.ncms.set(results));

    // Pricing calculator listeners
    const custoControl = this.produtoForm.get('precoCusto');
    const vendaControl = this.produtoForm.get('precoVenda');
    const margemControl = this.produtoForm.get('margemLucro');

    custoControl?.valueChanges.pipe(takeUntilDestroyed()).subscribe((custo) => {
      const m = Number(margemControl?.value || 0);
      const c = Number(custo || 0);
      if (c >= 0) {
        const venda = c * (1 + m / 100);
        vendaControl?.setValue(Number(venda.toFixed(2)), { emitEvent: false });
      }
    });

    margemControl?.valueChanges.pipe(takeUntilDestroyed()).subscribe((margem) => {
      const c = Number(custoControl?.value || 0);
      const m = Number(margem || 0);
      if (c >= 0) {
        const venda = c * (1 + m / 100);
        vendaControl?.setValue(Number(venda.toFixed(2)), { emitEvent: false });
      }
    });

    vendaControl?.valueChanges.pipe(takeUntilDestroyed()).subscribe((venda) => {
      const c = Number(custoControl?.value || 0);
      const v = Number(venda || 0);
      if (c > 0) {
        const margem = ((v - c) / c) * 100;
        margemControl?.setValue(Number(margem.toFixed(2)), { emitEvent: false });
      } else {
        margemControl?.setValue(0, { emitEvent: false });
      }
    });
  }

  ngOnInit() {
    this.loadCategorias();
    this.loadMarcas();
    this.loadFornecedores();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.editMode.set(true);
      this.produtoId.set(id);
      this.loadProduto(id);

      this.produtoForm.get('codigoInterno')?.disable();
      this.produtoForm.get('codigoBarras')?.disable();
    } else {
      this.loadProximoCodigoInterno();
    }
  }

  private loadFornecedores() {
    this.isLoadingFornecedores.set(true);
    this.fornecedorLoadError.set('');

    this.produtoService.getFornecedores().subscribe({
      next: (res) => {
        this.fornecedores.set(res);
        this.isLoadingFornecedores.set(false);
      },
      error: () => {
        this.fornecedores.set([]);
        this.isLoadingFornecedores.set(false);
        this.fornecedorLoadError.set('Erro ao carregar fornecedores');
        this.snackBar.open('Erro ao carregar fornecedores.', 'OK', { duration: 4000 });
      },
    });
  }

  private loadCategorias(selectedId?: string) {
    this.isLoadingCategorias.set(true);
    this.categoriaLoadError.set('');

    this.produtoService.getCategorias().subscribe({
      next: (res) => {
        this.categorias.set(res);
        this.isLoadingCategorias.set(false);

        if (selectedId) {
          this.produtoForm.get('categoriaId')?.setValue(selectedId);
        }
      },
      error: () => {
        this.categorias.set([]);
        this.isLoadingCategorias.set(false);
        this.categoriaLoadError.set('Erro ao carregar categorias');
        this.snackBar.open('Erro ao carregar categorias.', 'OK', { duration: 4000 });
      },
    });
  }

  private loadMarcas(selectedId?: string) {
    this.isLoadingMarcas.set(true);
    this.marcaLoadError.set('');

    this.produtoService.getMarcas().subscribe({
      next: (res) => {
        this.marcas.set(res);
        this.isLoadingMarcas.set(false);

        if (selectedId) {
          this.produtoForm.get('marcaId')?.setValue(selectedId);
        }
      },
      error: () => {
        this.marcas.set([]);
        this.isLoadingMarcas.set(false);
        this.marcaLoadError.set('Erro ao carregar marcas');
        this.snackBar.open('Erro ao carregar marcas.', 'OK', { duration: 4000 });
      },
    });
  }

  private loadProximoCodigoInterno() {
    this.produtoService.getProximoCodigoInterno().subscribe({
      next: ({ codigoInterno }) => {
        if (!this.produtoForm.get('codigoInterno')?.value) {
          this.produtoForm.get('codigoInterno')?.setValue(codigoInterno);
        }
      },
      error: () => {
        this.snackBar.open('Não foi possível sugerir o código interno.', 'OK', { duration: 4000 });
      },
    });
  }

  openCategoriaDialog(template: TemplateRef<unknown>) {
    this.categoriaForm.reset({ nome: '', descricao: '' });
    this.dialog.open(template, { width: '420px' });
  }

  salvarCategoria() {
    if (this.categoriaForm.invalid) {
      this.categoriaForm.markAllAsTouched();
      return;
    }

    const { nome, descricao } = this.categoriaForm.getRawValue();

    this.produtoService
      .createCategoria({
        nome: nome ?? '',
        descricao: descricao || undefined,
      })
      .subscribe({
        next: (categoria) => {
          this.dialog.closeAll();
          this.snackBar.open('Categoria criada!', 'OK', { duration: 3000 });
          this.loadCategorias(categoria.id);
        },
        error: (err) => {
          this.snackBar.open(err.error?.message || 'Erro ao criar categoria.', 'OK', {
            duration: 4000,
          });
        },
      });
  }

  openMarcaDialog(template: TemplateRef<unknown>) {
    this.marcaForm.reset({ nome: '' });
    this.dialog.open(template, { width: '420px' });
  }

  salvarMarca() {
    if (this.marcaForm.invalid) {
      this.marcaForm.markAllAsTouched();
      return;
    }

    const { nome } = this.marcaForm.getRawValue();

    this.produtoService
      .createMarca({
        nome: nome ?? '',
      })
      .subscribe({
        next: (marca) => {
          this.dialog.closeAll();
          this.snackBar.open('Marca criada!', 'OK', { duration: 3000 });
          this.loadMarcas(marca.id);
        },
        error: (err) => {
          this.snackBar.open(err.error?.message || 'Erro ao criar marca.', 'OK', {
            duration: 4000,
          });
        },
      });
  }

  loadProduto(id: string) {
    this.isLoading.set(true);
    this.produtoService.getProdutoById(id).subscribe({
      next: (produto) => {
        this.produtoForm.patchValue(produto);
        
        // Calculate and set margemLucro dynamically
        const custo = Number(produto.precoCusto || 0);
        const venda = Number(produto.precoVenda || 0);
        if (custo > 0) {
          const margem = ((venda - custo) / custo) * 100;
          this.produtoForm.get('margemLucro')?.setValue(Number(margem.toFixed(2)), { emitEvent: false });
        } else {
          this.produtoForm.get('margemLucro')?.setValue(0, { emitEvent: false });
        }

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

      const formValue = { ...this.produtoForm.getRawValue() };
      delete formValue.margemLucro;

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

  onNcmSelected(event: any) {
    const ncm = event.option.value as Ncm;
    this.produtoForm.get('ncm')?.setValue(ncm.codigo, { emitEvent: false });
    this.ncmQuery.set(ncm.codigo);
    this.ncms.set([]);
  }

  displayNcm(ncm: any): string {
    return ncm?.codigo || ncm || '';
  }
}
