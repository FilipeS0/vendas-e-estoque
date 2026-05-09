import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { PixService, PixResponse } from '../../../core/services/pix.service';
import { CurrencyPipe } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-pix-dialog',
  imports: [
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    CurrencyPipe,
  ],
  templateUrl: './pix-dialog.component.html',
  styleUrls: ['./pix-dialog.component.css'],
})
export class PixDialogComponent {
  private dialogRef = inject(MatDialogRef<PixDialogComponent>);
  public data = inject(MAT_DIALOG_DATA);
  private pixService = inject(PixService);
  private snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);

  pixData = signal<PixResponse | null>(null);
  isLoading = signal(true);

  ngOnInit() {
    this.pixService.generate(this.data.valor)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
      next: (res) => {
        this.pixData.set(res);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.snackBar.open(err.error?.message || 'Erro ao gerar PIX.', 'OK', { duration: 5000 });
        this.dialogRef.close(false);
      },
    });
  }

  copyPayload() {
    if (this.pixData()) {
      navigator.clipboard.writeText(this.pixData()!.payload);
      this.snackBar.open('Código PIX copiado!', 'OK', { duration: 2000 });
    }
  }

  confirm() {
    this.dialogRef.close(true);
  }

  cancel() {
    this.dialogRef.close(false);
  }
}
