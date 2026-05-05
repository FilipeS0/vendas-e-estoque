import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';

import { UsuarioListComponent } from './usuario-list.component';
import { UsuarioService } from '../../services/usuario.service';
import { MatDialog } from '@angular/material/dialog';

describe('UsuarioListComponent', () => {
  let component: UsuarioListComponent;
  let fixture: ComponentFixture<UsuarioListComponent>;

  const usuarioServiceSpy = {
    getUsuarios: () =>
      of({
        content: [],
        totalElements: 0,
        totalPages: 0,
        size: 50,
        number: 0,
      }),
    inativarUsuario: () => of(undefined),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UsuarioListComponent, NoopAnimationsModule],
      providers: [
        { provide: UsuarioService, useValue: usuarioServiceSpy },
        { provide: MatDialog, useValue: { open: () => ({ afterClosed: () => of(false) }) } },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(UsuarioListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
