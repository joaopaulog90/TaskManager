import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { ServicoToast } from './core/services/toast.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule],
  template: `
    <router-outlet />
    <div style="position:fixed;top:1rem;right:1rem;z-index:9999;display:flex;flex-direction:column;gap:0.5rem;">
      <div *ngFor="let toast of servicoToast.toasts"
        (click)="servicoToast.remover(toast)"
        [style.background]="corToast(toast.tipo)"
        style="padding:0.75rem 1.25rem;border-radius:8px;color:white;font-size:0.875rem;box-shadow:0 4px 12px rgba(0,0,0,0.2);cursor:pointer;max-width:350px;animation:slideIn 0.3s ease;">
        {{ toast.mensagem }}
      </div>
    </div>
  `,
  styles: [`
    @keyframes slideIn {
      from { opacity: 0; transform: translateX(100%); }
      to { opacity: 1; transform: translateX(0); }
    }
  `]
})
export class AppComponent {
  constructor(public servicoToast: ServicoToast) {}

  corToast(tipo: string): string {
    return ({ info: '#1976d2', sucesso: '#388e3c', erro: '#e53935' } as Record<string, string>)[tipo] ?? '#333';
  }
}
