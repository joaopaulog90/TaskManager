import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { rotas } from './app.routes';
import { interceptadorAutenticacao } from './core/interceptors/auth.interceptor';

export const configuracaoApp: ApplicationConfig = {
  providers: [
    provideRouter(rotas),
    provideHttpClient(withInterceptors([interceptadorAutenticacao]))
  ]
};
