import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';

import { UsuarioFormDialogComponent } from './usuario-form-dialog.component';
import { UsuarioService } from '../../services/usuario.service';

describe('UsuarioFormDialogComponent', () => {
  let component: UsuarioFormDialogComponent;
  let fixture: ComponentFixture<UsuarioFormDialogComponent>;

  const usuarioServiceSpy = {
    getPerfis: () => of([]),
    createUsuario: () => of({}),
    updateUsuario: () => of({}),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UsuarioFormDialogComponent],
      providers: [
        { provide: UsuarioService, useValue: usuarioServiceSpy },
        { provide: MAT_DIALOG_DATA, useValue: undefined },
        { provide: MatDialogRef, useValue: { close: () => undefined } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(UsuarioFormDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
