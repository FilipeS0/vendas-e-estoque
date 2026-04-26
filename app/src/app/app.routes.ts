import { Routes } from '@angular/router';
import { LoginPageComponent } from './pages/login/login.component';
import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginPageComponent },
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./pages/dashboard/dashboard.component').then((m) => m.DashboardPageComponent),
    canActivate: [authGuard],
  },
  {
    path: 'pos',
    loadComponent: () => import('./pages/pos/pos.component').then((m) => m.PosPageComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_ADMIN', 'ROLE_GERENTE', 'ROLE_OPERADOR'] },
  },
  {
    path: 'caixa',
    loadComponent: () => import('./pages/caixa/caixa.component').then((m) => m.CaixaComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_ADMIN', 'ROLE_GERENTE', 'ROLE_OPERADOR'] },
  },
  {
    path: 'estoque',
    loadComponent: () =>
      import('./pages/estoque/estoque-list.component').then((m) => m.EstoqueListComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_ADMIN', 'ROLE_GERENTE'] },
  },
  {
    path: 'clientes',
    loadComponent: () =>
      import('./pages/cliente/cliente-list/cliente-list.component').then(
        (m) => m.ClienteListComponent,
      ),
    canActivate: [authGuard],
  },
  {
    path: 'produtos',
    loadComponent: () =>
      import('./pages/produto/produto-list/produto-list.component').then(
        (m) => m.ProdutoListComponent,
      ),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_ADMIN', 'ROLE_GERENTE'] },
  },
  {
    path: 'clientes/:id',
    loadComponent: () =>
      import('./pages/cliente/cliente-details/cliente-details.component').then(
        (m) => m.ClienteDetailsPageComponent,
      ),
    canActivate: [authGuard],
  },
  {
    path: 'produtos/novo',
    loadComponent: () =>
      import('./pages/produto/produto-create/produto-create.component').then(
        (m) => m.ProdutoCreateComponent,
      ),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_ADMIN', 'ROLE_GERENTE'] },
  },
  {
    path: 'produtos/editar/:id',
    loadComponent: () =>
      import('./pages/produto/produto-create/produto-create.component').then(
        (m) => m.ProdutoCreateComponent,
      ),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_ADMIN', 'ROLE_GERENTE'] },
  },
  {
    path: 'relatorios',
    loadComponent: () =>
      import('./pages/relatorios/relatorios.component').then((m) => m.RelatoriosComponent),
    canActivate: [authGuard],
  },
];
