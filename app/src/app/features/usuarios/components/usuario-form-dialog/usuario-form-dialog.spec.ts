import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UsuarioFormDialog } from './usuario-form-dialog';

describe('UsuarioFormDialog', () => {
  let component: UsuarioFormDialog;
  let fixture: ComponentFixture<UsuarioFormDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UsuarioFormDialog]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UsuarioFormDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
