import { Routes } from '@angular/router';
import { guardaAutenticacao } from './shared/guards/auth.guard';

export const rotas: Routes = [
  { path: 'login', loadComponent: () => import('./features/login/login.component').then(m => m.LoginComponent) },
  { path: 'projects', canActivate: [guardaAutenticacao], loadComponent: () => import('./features/projects/projects.component').then(m => m.ProjectsComponent) },
  { path: 'projects/:id', canActivate: [guardaAutenticacao], loadComponent: () => import('./features/project-detail/project-detail.component').then(m => m.ProjectDetailComponent) },
  { path: '', redirectTo: 'projects', pathMatch: 'full' },
  { path: '**', redirectTo: 'projects' }
];
