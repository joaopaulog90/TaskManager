import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ServicoAutenticacao } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrls: ['./login.component.scss'],
  templateUrl: './login.component.html'
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
        error: (err: any) => {
          if (err.error?.erros?.length) {
            this.erro = err.error.erros.map((e: any) => e.mensagem).join('. ');
          } else {
            this.erro = err.error?.detail || 'Erro ao criar conta';
          }
          this.carregando = false;
        }
      });
    } else {
      this.servicoAuth.autenticar({ email: this.email, senha: this.senha }).subscribe({
        next: () => { this.carregando = false; this.roteador.navigate(['/projects']); },
        error: (err: any) => { this.erro = err.error?.detail || 'Email ou senha inválidos'; this.carregando = false; }
      });
    }
  }
}
