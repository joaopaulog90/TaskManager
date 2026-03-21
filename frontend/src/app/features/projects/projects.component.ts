import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ServicoProjeto } from '../../core/services/project.service';
import { ServicoAutenticacao } from '../../core/services/auth.service';
import { RespostaProjeto } from '../../core/models/project.model';

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div style="max-width:900px;margin:0 auto;padding:2rem;">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:2rem;">
        <h1 style="margin:0;">Meus Projetos</h1>
        <button (click)="sair()" style="padding:0.5rem 1rem;background:#e53935;color:white;border:none;border-radius:4px;cursor:pointer;">
          Sair
        </button>
      </div>

      <div style="background:white;padding:1.5rem;border-radius:8px;box-shadow:0 1px 4px rgba(0,0,0,0.1);margin-bottom:1.5rem;">
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
        <button (click)="deletar(projeto.id)" style="padding:0.25rem 0.75rem;background:#e53935;color:white;border:none;border-radius:4px;cursor:pointer;margin-left:1rem;">
          Excluir
        </button>
      </div>

      <div *ngIf="erroAoDeletar" style="color:#c00;font-size:0.875rem;margin-top:0.5rem;">{{ erroAoDeletar }}</div>

      <div *ngIf="!carregando && projetos.length === 0" style="text-align:center;color:#666;padding:3rem;">
        Nenhum projeto encontrado. Crie seu primeiro projeto acima.
      </div>
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

  constructor(private servicoProjeto: ServicoProjeto, private servicoAuth: ServicoAutenticacao, private roteador: Router) {}

  ngOnInit(): void {
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

  sair(): void {
    this.servicoAuth.sair();
  }
}
