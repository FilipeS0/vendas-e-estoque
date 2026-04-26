import { Routes } from '@angular/router';
import { LoginPageComponent } from './pages/login/login.component';
import { DashboardPageComponent } from './pages/dashboard/dashboard.component';
import { PosPageComponent } from './pages/pos/pos.component';
import { CaixaComponent } from './pages/caixa/caixa.component';
import { ClienteDetailsPageComponent } from './pages/cliente/cliente-details/cliente-details.component';
import { EstoqueListComponent } from './pages/estoque/estoque-list.component';
import { ProdutoCreateComponent } from './pages/produto/produto-create/produto-create.component';
import { ProdutoListComponent } from './pages/produto/produto-list/produto-list.component';
import { ClienteListComponent } from './pages/cliente/cliente-list/cliente-list.component';
import { RelatoriosComponent } from './pages/relatorios/relatorios.component';
import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginPageComponent },
  { path: 'dashboard', component: DashboardPageComponent, canActivate: [authGuard] },
  {
    path: 'pos',
    component: PosPageComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_ADMIN', 'ROLE_GERENTE', 'ROLE_OPERADOR'] },
  },
  {
    path: 'caixa',
    component: CaixaComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_ADMIN', 'ROLE_GERENTE', 'ROLE_OPERADOR'] },
  },
  {
    path: 'estoque',
    component: EstoqueListComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_ADMIN', 'ROLE_GERENTE'] },
  },
  { path: 'clientes', component: ClienteListComponent, canActivate: [authGuard] },
  {
    path: 'produtos',
    component: ProdutoListComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_ADMIN', 'ROLE_GERENTE'] },
  },
  { path: 'clientes/:id', component: ClienteDetailsPageComponent, canActivate: [authGuard] },
  {
    path: 'produtos/novo',
    component: ProdutoCreateComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_ADMIN', 'ROLE_GERENTE'] },
  },
  {
    path: 'produtos/editar/:id',
    component: ProdutoCreateComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ROLE_ADMIN', 'ROLE_GERENTE'] },
  },
  { path: 'relatorios', component: RelatoriosComponent, canActivate: [authGuard] },
];
