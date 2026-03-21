import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ServicoProjeto } from '../../core/services/project.service';
import { ServicoAutenticacao } from '../../core/services/auth.service';
import { ServicoUsuario } from '../../core/services/user.service';
import { RespostaProjeto } from '../../core/models/project.model';
import { RespostaUsuario } from '../../core/models/auth.model';

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div style="max-width:900px;margin:0 auto;padding:2rem;">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:2rem;">
        <h1 style="margin:0;">Meus Projetos</h1>
        <div style="display:flex;gap:0.75rem;align-items:center;">
          <button *ngIf="ehAdmin" (click)="alternarAba()"
            [style.background]="abaAtiva === 'usuarios' ? '#1976d2' : '#757575'"
            style="padding:0.5rem 1rem;color:white;border:none;border-radius:4px;cursor:pointer;">
            {{ abaAtiva === 'usuarios' ? 'Projetos' : 'Usuários' }}
          </button>
          <button (click)="sair()" style="padding:0.5rem 1rem;background:#e53935;color:white;border:none;border-radius:4px;cursor:pointer;">
            Sair
          </button>
        </div>
      </div>

      <!-- ABA PROJETOS -->
      <ng-container *ngIf="abaAtiva === 'projetos'">
        <div *ngIf="ehAdmin" style="background:white;padding:1.5rem;border-radius:8px;box-shadow:0 1px 4px rgba(0,0,0,0.1);margin-bottom:1.5rem;">
          <h3 style="margin-top:0;">Novo Projeto</h3>
          <div style="display:flex;gap:1rem;align-items:flex-end;">
            <div style="flex:1;">
              <input [(ngModel)]="novoNome" placeholder="Nome do projeto" style="width:100%;padding:0.5rem;border:1px solid #ddd;border-radius:4px;box-sizing:border-box;" />
            </div>
            <div style="flex:2;">
              <input [(ngModel)]="novaDescricao" placeholder="Descrição (opcional)" style="width:100%;padding:0.5rem;border:1px solid #ddd;border-radius:4px;box-sizing:border-box;" />
            </div>
            <button (click)="criar()" [disabled]="!novoNome.trim()" style="padding:0.5rem 1.5rem;background:#1976d2;color:white;border:none;border-radius:4px;cursor:pointer;">
              Criar
            </button>
          </div>
          <div *ngIf="erroAoCriar" style="color:#c00;font-size:0.875rem;margin-top:0.5rem;">{{ erroAoCriar }}</div>
        </div>

        <div *ngIf="carregando" style="text-align:center;padding:2rem;color:#666;">Carregando...</div>

        <div *ngFor="let projeto of projetos"
          style="background:white;padding:1.5rem;border-radius:8px;box-shadow:0 1px 4px rgba(0,0,0,0.1);margin-bottom:1rem;display:flex;justify-content:space-between;align-items:center;">
          <div (click)="abrir(projeto)" style="cursor:pointer;flex:1;">
            <h3 style="margin:0 0 0.25rem;">{{ projeto.nome }}</h3>
            <p style="margin:0;color:#666;font-size:0.875rem;">{{ projeto.descricao }}</p>
            <small style="color:#999;">{{ projeto.quantidadeMembros }} membro(s) · criado por {{ projeto.nomeProprietario }}</small>
          </div>
          <button *ngIf="ehAdmin" (click)="deletar(projeto.id)" style="padding:0.25rem 0.75rem;background:#e53935;color:white;border:none;border-radius:4px;cursor:pointer;margin-left:1rem;">
            Excluir
          </button>
        </div>

        <div *ngIf="erroAoDeletar" style="color:#c00;font-size:0.875rem;margin-top:0.5rem;">{{ erroAoDeletar }}</div>

        <div *ngIf="!carregando && projetos.length === 0" style="text-align:center;color:#666;padding:3rem;">
          {{ ehAdmin ? 'Nenhum projeto encontrado. Crie seu primeiro projeto acima.' : 'Você ainda não faz parte de nenhum projeto.' }}
        </div>
      </ng-container>

      <!-- ABA USUÁRIOS (só ADMIN) -->
      <ng-container *ngIf="abaAtiva === 'usuarios'">
        <div style="background:white;padding:1.5rem;border-radius:8px;box-shadow:0 1px 4px rgba(0,0,0,0.1);">
          <h3 style="margin-top:0;">Gerenciamento de Usuários</h3>

          <div *ngIf="erroUsuario" style="background:#fee;color:#c00;padding:0.75rem;border-radius:4px;margin-bottom:1rem;font-size:0.875rem;">
            {{ erroUsuario }}
          </div>
          <div *ngIf="sucessoUsuario" style="background:#efe;color:#2e7d32;padding:0.75rem;border-radius:4px;margin-bottom:1rem;font-size:0.875rem;">
            {{ sucessoUsuario }}
          </div>

          <table style="width:100%;border-collapse:collapse;">
            <thead>
              <tr style="background:#f5f5f5;">
                <th style="padding:0.75rem 1rem;text-align:left;font-size:0.875rem;color:#666;">Nome</th>
                <th style="padding:0.75rem 1rem;text-align:left;font-size:0.875rem;color:#666;">Email</th>
                <th style="padding:0.75rem 1rem;text-align:left;font-size:0.875rem;color:#666;">Perfil</th>
                <th style="padding:0.75rem 1rem;text-align:left;font-size:0.875rem;color:#666;">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let usuario of usuarios" style="border-top:1px solid #f0f0f0;">
                <td style="padding:0.75rem 1rem;font-weight:500;">{{ usuario.nome }}</td>
                <td style="padding:0.75rem 1rem;font-size:0.875rem;color:#555;">{{ usuario.email }}</td>
                <td style="padding:0.75rem 1rem;">
                  <span [style.background]="usuario.perfil === 'ADMIN' ? '#1976d2' : '#757575'"
                    style="border-radius:12px;padding:0.25rem 0.5rem;font-size:0.75rem;color:white;font-weight:600;">
                    {{ usuario.perfil }}
                  </span>
                </td>
                <td style="padding:0.75rem 1rem;">
                  <button *ngIf="usuario.id !== idUsuarioAtual"
                    (click)="alternarPerfil(usuario)"
                    [style.background]="usuario.perfil === 'ADMIN' ? '#e53935' : '#388e3c'"
                    style="padding:0.25rem 0.75rem;color:white;border:none;border-radius:4px;cursor:pointer;font-size:0.75rem;">
                    {{ usuario.perfil === 'ADMIN' ? 'Rebaixar para MEMBER' : 'Promover para ADMIN' }}
                  </button>
                  <span *ngIf="usuario.id === idUsuarioAtual" style="font-size:0.75rem;color:#999;">(você)</span>
                </td>
              </tr>
              <tr *ngIf="usuarios.length === 0">
                <td colspan="4" style="padding:2rem;text-align:center;color:#666;">Nenhum usuário encontrado.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </ng-container>
    </div>
  `
})
export class ProjectsComponent implements OnInit {
  projetos: RespostaProjeto[] = [];
  carregando = false;
  novoNome = '';
  novaDescricao = '';
  erroAoCriar = '';
  erroAoDeletar = '';

  abaAtiva: 'projetos' | 'usuarios' = 'projetos';
  ehAdmin = false;
  idUsuarioAtual = 0;
  usuarios: RespostaUsuario[] = [];
  erroUsuario = '';
  sucessoUsuario = '';

  constructor(
    private servicoProjeto: ServicoProjeto,
    private servicoAuth: ServicoAutenticacao,
    private servicoUsuario: ServicoUsuario,
    private roteador: Router
  ) {}

  ngOnInit(): void {
    this.ehAdmin = localStorage.getItem('perfil') === 'ADMIN';
    this.idUsuarioAtual = this.servicoAuth.obterIdUsuarioAtual();
    this.carregar();
  }

  carregar(): void {
    this.carregando = true;
    this.servicoProjeto.listar().subscribe({
      next: (data) => { this.projetos = data; this.carregando = false; },
      error: () => this.carregando = false
    });
  }

  criar(): void {
    this.erroAoCriar = '';
    this.servicoProjeto.criar({ nome: this.novoNome.trim(), descricao: this.novaDescricao.trim() }).subscribe({
      next: () => { this.novoNome = ''; this.novaDescricao = ''; this.carregar(); },
      error: (err: any) => { this.erroAoCriar = err.error?.detail || 'Erro ao criar projeto'; }
    });
  }

  deletar(id: number): void {
    if (!confirm('Excluir este projeto?')) return;
    this.erroAoDeletar = '';
    this.servicoProjeto.deletar(id).subscribe({
      next: () => this.carregar(),
      error: (err: any) => { this.erroAoDeletar = err.error?.detail || 'Erro ao excluir projeto. Verifique se você é ADMIN.'; }
    });
  }

  abrir(projeto: RespostaProjeto): void {
    this.roteador.navigate(['/projects', projeto.id]);
  }

  carregarUsuarios(): void {
    this.servicoUsuario.listarTodos().subscribe({
      next: (lista) => this.usuarios = lista,
      error: (err: any) => { this.erroUsuario = err.error?.detail || 'Erro ao carregar usuários'; }
    });
  }

  alternarPerfil(usuario: RespostaUsuario): void {
    this.erroUsuario = '';
    this.sucessoUsuario = '';
    const novoPerfil = usuario.perfil === 'ADMIN' ? 'MEMBER' : 'ADMIN';
    this.servicoUsuario.alterarPerfil(usuario.id, novoPerfil).subscribe({
      next: (atualizado) => {
        usuario.perfil = atualizado.perfil;
        this.sucessoUsuario = `Perfil de ${usuario.nome} alterado para ${atualizado.perfil}`;
      },
      error: (err: any) => { this.erroUsuario = err.error?.detail || 'Erro ao alterar perfil'; }
    });
  }

  alternarAba(): void {
    if (this.abaAtiva === 'usuarios') {
      this.abaAtiva = 'projetos';
    } else {
      this.abaAtiva = 'usuarios';
      this.carregarUsuarios();
    }
  }

  sair(): void {
    this.servicoAuth.sair();
  }
}
