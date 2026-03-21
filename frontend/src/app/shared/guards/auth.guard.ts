import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { ServicoAutenticacao } from '../../core/services/auth.service';

export const guardaAutenticacao: CanActivateFn = () => {
  const servico = inject(ServicoAutenticacao);
  const roteador = inject(Router);
  if (servico.estaAutenticado()) return true;
  roteador.navigate(['/login']);
  return false;
};
