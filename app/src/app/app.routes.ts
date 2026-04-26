import { Routes } from '@angular/router';
import { LoginPageComponent } from './pages/login/login.component';
import { DashboardPageComponent } from './pages/dashboard/dashboard.component';
import { PosPageComponent } from './pages/pos/pos.component';
import { CaixaComponent } from './pages/caixa/caixa.component';
import { ClienteDetailsPageComponent } from './pages/cliente/cliente-details.component';
import { EstoqueListComponent } from './pages/estoque/estoque-list.component';
import { ProdutoCreateComponent } from './pages/produto/produto-create/produto-create.component';
import { ProdutoListComponent } from './pages/produto/produto-list/produto-list.component';
import { ClienteDetailsPageComponent } from './pages/cliente/cliente-details.component';
import { ClienteListComponent } from './pages/cliente/cliente-list.component';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginPageComponent },
  { path: 'dashboard', component: DashboardPageComponent, canActivate: [authGuard] },
  { path: 'pos', component: PosPageComponent, canActivate: [authGuard] },
  { path: 'caixa', component: CaixaComponent, canActivate: [authGuard] },
  { path: 'estoque', component: EstoqueListComponent, canActivate: [authGuard] },
  { path: 'clientes', component: ClienteListComponent, canActivate: [authGuard] },
  { path: 'produtos', component: ProdutoListComponent, canActivate: [authGuard] },
  { path: 'clientes/:id', component: ClienteDetailsPageComponent, canActivate: [authGuard] },
  { path: 'produtos/novo', component: ProdutoCreateComponent, canActivate: [authGuard] },
  { path: 'produtos/editar/:id', component: ProdutoCreateComponent, canActivate: [authGuard] },
];
