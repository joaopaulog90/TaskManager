import { Injectable } from '@angular/core';

export interface Toast {
  mensagem: string;
  tipo: 'info' | 'sucesso' | 'erro';
}

@Injectable({ providedIn: 'root' })
export class ServicoToast {
  toasts: Toast[] = [];

  mostrar(mensagem: string, tipo: Toast['tipo'] = 'info'): void {
    const toast: Toast = { mensagem, tipo };
    this.toasts.push(toast);
    setTimeout(() => this.remover(toast), 4000);
  }

  remover(toast: Toast): void {
    this.toasts = this.toasts.filter(t => t !== toast);
  }
}
