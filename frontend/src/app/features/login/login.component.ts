import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ServicoAutenticacao } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div style="display:flex;justify-content:center;align-items:center;min-height:100vh;background:#f5f5f5;">
      <div style="background:white;padding:2rem;border-radius:8px;box-shadow:0 2px 8px rgba(0,0,0,0.1);width:100%;max-width:400px;">
        <h2 style="margin-bottom:1.5rem;text-align:center;">{{ modoCadastro ? 'Criar conta' : 'TaskManager' }}</h2>

        <div *ngIf="erro" style="background:#fee;color:#c00;padding:0.75rem;border-radius:4px;margin-bottom:1rem;font-size:0.875rem;">
          {{ erro }}
        </div>

        <div *ngIf="modoCadastro" style="margin-bottom:1rem;">
          <label style="display:block;margin-bottom:0.25rem;font-size:0.875rem;font-weight:500;">Nome</label>
          <input [(ngModel)]="nome" type="text" placeholder="Seu nome"
            style="width:100%;padding:0.5rem;border:1px solid #ddd;border-radius:4px;box-sizing:border-box;" />
        </div>

        <div style="margin-bottom:1rem;">
          <label style="display:block;margin-bottom:0.25rem;font-size:0.875rem;font-weight:500;">Email</label>
          <input [(ngModel)]="email" type="email" placeholder="seu@email.com"
            style="width:100%;padding:0.5rem;border:1px solid #ddd;border-radius:4px;box-sizing:border-box;" />
        </div>

        <div style="margin-bottom:1.5rem;">
          <label style="display:block;margin-bottom:0.25rem;font-size:0.875rem;font-weight:500;">Senha</label>
          <input [(ngModel)]="senha" type="password" placeholder="••••••"
            style="width:100%;padding:0.5rem;border:1px solid #ddd;border-radius:4px;box-sizing:border-box;" />
        </div>

        <button (click)="enviar()" [disabled]="carregando"
          style="width:100%;padding:0.75rem;background:#1976d2;color:white;border:none;border-radius:4px;cursor:pointer;font-size:1rem;">
          {{ carregando ? 'Aguarde...' : (modoCadastro ? 'Criar conta' : 'Entrar') }}
        </button>

        <p style="text-align:center;margin-top:1rem;font-size:0.875rem;">
          {{ modoCadastro ? 'Já tem conta?' : 'Não tem conta?' }}
          <a href="#" (click)="alternarModo($event)" style="color:#1976d2;">
            {{ modoCadastro ? 'Entrar' : 'Criar conta' }}
          </a>
        </p>
      </div>
    </div>
  `
})
export class LoginComponent {
  email = '';
  senha = '';
  nome = '';
  erro = '';
  carregando = false;
  modoCadastro = false;

  constructor(private servicoAuth: ServicoAutenticacao, private roteador: Router) {}

  alternarModo(e: Event): void {
    e.preventDefault();
    this.modoCadastro = !this.modoCadastro;
    this.erro = '';
  }

  enviar(): void {
    this.erro = '';
    this.carregando = true;

    if (this.modoCadastro) {
      this.servicoAuth.cadastrar({ nome: this.nome, email: this.email, senha: this.senha }).subscribe({
        next: () => { this.modoCadastro = false; this.carregando = false; this.erro = ''; },
        error: (err: any) => { this.erro = err.error?.detail || 'Erro ao criar conta'; this.carregando = false; }
      });
    } else {
      this.servicoAuth.autenticar({ email: this.email, senha: this.senha }).subscribe({
        next: () => { this.carregando = false; this.roteador.navigate(['/projects']); },
        error: (err: any) => { this.erro = err.error?.detail || 'Email ou senha inválidos'; this.carregando = false; }
      });
    }
  }
}
