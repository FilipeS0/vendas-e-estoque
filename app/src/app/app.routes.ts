import { Routes } from '@angular/router';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { authGuard } from './core/auth/auth.guard';
import { roleGuard } from './core/auth/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/pages/login.component').then((m) => m.LoginPageComponent),
  },
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/pages/dashboard.component').then((m) => m.DashboardPageComponent),
      },
      {
        path: '403',
        loadComponent: () => import('./features/forbidden/pages/forbidden.component').then((m) => m.ForbiddenComponent),
      },
      {
        path: 'pos',
        loadComponent: () => import('./features/pdv/pages/pos.component').then((m) => m.PosPageComponent),
        canActivate: [roleGuard],
        data: { roles: ['ROLE_ADMIN', 'ROLE_GERENTE', 'ROLE_OPERADOR'] },
      },
      {
        path: 'caixa',
        loadComponent: () => import('./features/caixa/pages/caixa.component').then((m) => m.CaixaComponent),
        canActivate: [roleGuard],
        data: { roles: ['ROLE_ADMIN', 'ROLE_GERENTE', 'ROLE_OPERADOR'] },
      },
      {
        path: 'estoque',
        loadComponent: () => import('./features/estoque/pages/estoque-list.component').then((m) => m.EstoqueListComponent),
        canActivate: [roleGuard],
        data: { roles: ['ROLE_ADMIN', 'ROLE_GERENTE'] },
      },
      {
        path: 'clientes',
        loadComponent: () => import('./features/clientes/pages/cliente-list/cliente-list.component').then((m) => m.ClienteListComponent),
      },
      {
        path: 'clientes/:id',
        loadComponent: () => import('./features/clientes/pages/cliente-details/cliente-details.component').then((m) => m.ClienteDetailsPageComponent),
      },
      {
        path: 'produtos',
        loadComponent: () => import('./features/produtos/pages/produto-list/produto-list.component').then((m) => m.ProdutoListComponent),
        canActivate: [roleGuard],
        data: { roles: ['ROLE_ADMIN', 'ROLE_GERENTE'] },
      },
      {
        path: 'produtos/novo',
        loadComponent: () => import('./features/produtos/pages/produto-create/produto-create.component').then((m) => m.ProdutoCreateComponent),
        canActivate: [roleGuard],
        data: { roles: ['ROLE_ADMIN', 'ROLE_GERENTE'] },
      },
      {
        path: 'produtos/editar/:id',
        loadComponent: () => import('./features/produtos/pages/produto-create/produto-create.component').then((m) => m.ProdutoCreateComponent),
        canActivate: [roleGuard],
        data: { roles: ['ROLE_ADMIN', 'ROLE_GERENTE'] },
      },
      {
        path: 'relatorios',
        loadComponent: () => import('./features/relatorios/pages/relatorios.component').then((m) => m.RelatoriosComponent),
      },
      {
        path: 'configuracoes',
        loadComponent: () => import('./features/configuracoes/pages/configuracoes-page.component').then((m) => m.ConfiguracoesPageComponent),
        canActivate: [roleGuard],
        data: { roles: ['ROLE_ADMIN'] },
      },
      {
        path: 'crediario',
        loadComponent: () => import('./features/crediario/pages/crediario-page.component').then((m) => m.CrediarioPageComponent),
        canActivate: [roleGuard],
        data: { roles: ['ROLE_ADMIN', 'ROLE_GERENTE'] },
      },
    ],
  },
];
