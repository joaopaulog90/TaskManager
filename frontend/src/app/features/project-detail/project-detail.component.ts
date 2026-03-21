import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ServicoProjeto } from '../../core/services/project.service';
import { ServicoTarefa } from '../../core/services/task.service';
import { RespostaProjeto } from '../../core/models/project.model';
import { RespostaTarefa, StatusTarefa, PrioridadeTarefa, RespostaResumoTarefa } from '../../core/models/task.model';

@Component({
  selector: 'app-project-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div style="max-width:1100px;margin:0 auto;padding:2rem;">

      <div style="display:flex;align-items:center;gap:1rem;margin-bottom:1.5rem;">
        <button (click)="voltar()" style="padding:0.5rem 1rem;background:#666;color:white;border:none;border-radius:4px;cursor:pointer;">← Projetos</button>
        <h1 style="margin:0;">{{ projeto?.nome }}</h1>
        <span style="color:#666;font-size:0.9rem;">{{ projeto?.descricao }}</span>
      </div>

      <div *ngIf="resumo" style="display:flex;gap:1rem;margin-bottom:1.5rem;flex-wrap:wrap;">
        <div *ngFor="let entrada of resumoPorStatus()" style="background:white;padding:1rem 1.5rem;border-radius:8px;box-shadow:0 1px 4px rgba(0,0,0,0.1);text-align:center;">
          <div style="font-size:1.5rem;font-weight:bold;">{{ entrada.count }}</div>
          <div style="font-size:0.75rem;color:#666;">{{ entrada.label }}</div>
        </div>
      </div>

      <div *ngIf="erroGlobal" style="background:#fee;color:#c00;padding:0.75rem;border-radius:4px;margin-bottom:1rem;font-size:0.875rem;">
        {{ erroGlobal }}
      </div>

      <div style="background:white;padding:1.5rem;border-radius:8px;box-shadow:0 1px 4px rgba(0,0,0,0.1);margin-bottom:1.5rem;">
        <h3 style="margin-top:0;">Nova Tarefa</h3>
        <div style="display:flex;gap:0.75rem;flex-wrap:wrap;align-items:flex-end;">
          <input [(ngModel)]="novoTitulo" placeholder="Título" style="flex:2;min-width:180px;padding:0.5rem;border:1px solid #ddd;border-radius:4px;" />
          <select [(ngModel)]="novaPrioridade" style="padding:0.5rem;border:1px solid #ddd;border-radius:4px;">
            <option value="LOW">LOW</option>
            <option value="MEDIUM">MEDIUM</option>
            <option value="HIGH">HIGH</option>
            <option value="CRITICAL">CRITICAL</option>
          </select>
          <input [(ngModel)]="novoPrazo" type="datetime-local" style="padding:0.5rem;border:1px solid #ddd;border-radius:4px;" />
          <button (click)="criarTarefa()" [disabled]="!novoTitulo.trim()" style="padding:0.5rem 1.5rem;background:#1976d2;color:white;border:none;border-radius:4px;cursor:pointer;">
            Criar
          </button>
        </div>
      </div>

      <div style="display:flex;gap:0.75rem;margin-bottom:1rem;flex-wrap:wrap;">
        <select [(ngModel)]="filtroStatus" (ngModelChange)="carregarTarefas()" style="padding:0.5rem;border:1px solid #ddd;border-radius:4px;">
          <option value="">Todos os status</option>
          <option value="TODO">TODO</option>
          <option value="IN_PROGRESS">IN_PROGRESS</option>
          <option value="DONE">DONE</option>
        </select>
        <select [(ngModel)]="filtroPrioridade" (ngModelChange)="carregarTarefas()" style="padding:0.5rem;border:1px solid #ddd;border-radius:4px;">
          <option value="">Todas as prioridades</option>
          <option value="LOW">LOW</option>
          <option value="MEDIUM">MEDIUM</option>
          <option value="HIGH">HIGH</option>
          <option value="CRITICAL">CRITICAL</option>
        </select>
        <input [(ngModel)]="termoBusca" (input)="aoBuscar()" placeholder="Buscar tarefas..." style="flex:1;min-width:180px;padding:0.5rem;border:1px solid #ddd;border-radius:4px;" />
      </div>

      <div style="background:white;border-radius:8px;box-shadow:0 1px 4px rgba(0,0,0,0.1);overflow:hidden;">
        <table style="width:100%;border-collapse:collapse;">
          <thead>
            <tr style="background:#f5f5f5;">
              <th style="padding:0.75rem 1rem;text-align:left;font-size:0.875rem;color:#666;">Título</th>
              <th style="padding:0.75rem 1rem;text-align:left;font-size:0.875rem;color:#666;">Status</th>
              <th style="padding:0.75rem 1rem;text-align:left;font-size:0.875rem;color:#666;">Prioridade</th>
              <th style="padding:0.75rem 1rem;text-align:left;font-size:0.875rem;color:#666;">Responsável</th>
              <th style="padding:0.75rem 1rem;text-align:left;font-size:0.875rem;color:#666;">Prazo</th>
              <th style="padding:0.75rem 1rem;text-align:left;font-size:0.875rem;color:#666;">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let tarefa of tarefas" style="border-top:1px solid #f0f0f0;">
              <td style="padding:0.75rem 1rem;">
                <div style="font-weight:500;">{{ tarefa.titulo }}</div>
                <div style="font-size:0.75rem;color:#999;">{{ tarefa.descricao }}</div>
              </td>
              <td style="padding:0.75rem 1rem;">
                <select [(ngModel)]="tarefa.status" (ngModelChange)="alterarStatus(tarefa, $event)"
                  [style.background]="corStatus(tarefa.status)"
                  style="border:none;border-radius:12px;padding:0.25rem 0.5rem;font-size:0.75rem;color:white;cursor:pointer;font-weight:600;">
                  <option value="TODO">TODO</option>
                  <option value="IN_PROGRESS">IN_PROGRESS</option>
                  <option value="DONE">DONE</option>
                </select>
              </td>
              <td style="padding:0.75rem 1rem;">
                <span [style.background]="corPrioridade(tarefa.prioridade)"
                  style="border-radius:12px;padding:0.25rem 0.5rem;font-size:0.75rem;color:white;font-weight:600;">
                  {{ tarefa.prioridade }}
                </span>
              </td>
              <td style="padding:0.75rem 1rem;font-size:0.875rem;color:#555;">{{ tarefa.nomeResponsavel || '—' }}</td>
              <td style="padding:0.75rem 1rem;font-size:0.875rem;color:#555;">{{ tarefa.prazo ? (tarefa.prazo | date:'dd/MM/yyyy') : '—' }}</td>
              <td style="padding:0.75rem 1rem;">
                <button (click)="deletarTarefa(tarefa)" style="padding:0.25rem 0.5rem;background:#e53935;color:white;border:none;border-radius:4px;cursor:pointer;font-size:0.75rem;">
                  Excluir
                </button>
              </td>
            </tr>
            <tr *ngIf="tarefas.length === 0">
              <td colspan="6" style="padding:2rem;text-align:center;color:#666;">Nenhuma tarefa encontrada.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div *ngIf="totalPaginas > 1" style="display:flex;justify-content:center;gap:0.5rem;margin-top:1rem;">
        <button *ngFor="let p of numerosPagina()" (click)="irParaPagina(p)"
          [style.background]="p === paginaAtual ? '#1976d2' : '#fff'"
          [style.color]="p === paginaAtual ? 'white' : '#333'"
          style="padding:0.25rem 0.75rem;border:1px solid #ddd;border-radius:4px;cursor:pointer;">
          {{ p + 1 }}
        </button>
      </div>
    </div>
  `
})
export class ProjectDetailComponent implements OnInit {
  projeto: RespostaProjeto | null = null;
  tarefas: RespostaTarefa[] = [];
  resumo: RespostaResumoTarefa | null = null;
  idProjeto!: number;

  novoTitulo = '';
  novaPrioridade: PrioridadeTarefa = 'MEDIUM';
  novoPrazo = '';

  filtroStatus: StatusTarefa | '' = '';
  filtroPrioridade: PrioridadeTarefa | '' = '';
  termoBusca = '';
  temporizadorBusca: any;

  paginaAtual = 0;
  totalPaginas = 0;
  erroGlobal = '';

  constructor(
    private rota: ActivatedRoute,
    private roteador: Router,
    private servicoProjeto: ServicoProjeto,
    private servicoTarefa: ServicoTarefa
  ) {}

  ngOnInit(): void {
    this.idProjeto = Number(this.rota.snapshot.paramMap.get('id'));
    this.servicoProjeto.buscar(this.idProjeto).subscribe(p => this.projeto = p);
    this.carregarTarefas();
    this.carregarResumo();
  }

  carregarTarefas(): void {
    const filtros: any = { page: this.paginaAtual };
    if (this.filtroStatus) filtros.status = this.filtroStatus;
    if (this.filtroPrioridade) filtros.priority = this.filtroPrioridade;
    this.servicoTarefa.listar(this.idProjeto, filtros).subscribe(res => {
      this.tarefas = res.conteudo;
      this.totalPaginas = res.totalPaginas;
    });
  }

  carregarResumo(): void {
    this.servicoTarefa.resumo(this.idProjeto).subscribe(s => this.resumo = s);
  }

  aoBuscar(): void {
    clearTimeout(this.temporizadorBusca);
    this.temporizadorBusca = setTimeout(() => {
      if (this.termoBusca.trim().length >= 2) {
        this.servicoTarefa.pesquisar(this.idProjeto, this.termoBusca.trim()).subscribe(res => this.tarefas = res);
      } else if (this.termoBusca.trim().length === 0) {
        this.carregarTarefas();
      }
    }, 400);
  }

  criarTarefa(): void {
    this.erroGlobal = '';
    const req: any = { titulo: this.novoTitulo.trim(), prioridade: this.novaPrioridade };
    if (this.novoPrazo) req.prazo = new Date(this.novoPrazo).toISOString();
    this.servicoTarefa.criar(this.idProjeto, req).subscribe({
      next: () => { this.novoTitulo = ''; this.novoPrazo = ''; this.carregarTarefas(); this.carregarResumo(); },
      error: (err: any) => { this.erroGlobal = err.error?.detail || 'Erro ao criar tarefa'; }
    });
  }

  alterarStatus(tarefa: RespostaTarefa, novoStatus: StatusTarefa): void {
    this.erroGlobal = '';
    const statusAnterior = tarefa.status;
    this.servicoTarefa.atualizar(this.idProjeto, tarefa.id, { status: novoStatus }).subscribe({
      next: (atualizada) => { Object.assign(tarefa, atualizada); this.carregarResumo(); },
      error: (err: any) => { tarefa.status = statusAnterior; this.erroGlobal = err.error?.detail || 'Erro ao atualizar status'; }
    });
  }

  deletarTarefa(tarefa: RespostaTarefa): void {
    if (!confirm('Excluir esta tarefa?')) return;
    this.servicoTarefa.deletar(this.idProjeto, tarefa.id).subscribe({
      next: () => { this.carregarTarefas(); this.carregarResumo(); }
    });
  }

  irParaPagina(pagina: number): void {
    this.paginaAtual = pagina;
    this.carregarTarefas();
  }

  numerosPagina(): number[] {
    return Array.from({ length: this.totalPaginas }, (_, i) => i);
  }

  resumoPorStatus(): { label: string; count: number }[] {
    if (!this.resumo) return [];
    return Object.entries(this.resumo.porStatus).map(([label, count]) => ({ label, count }));
  }

  corStatus(status: StatusTarefa): string {
    return ({ TODO: '#9e9e9e', IN_PROGRESS: '#1976d2', DONE: '#388e3c' } as Record<string, string>)[status] ?? '#666';
  }

  corPrioridade(prioridade: PrioridadeTarefa): string {
    return ({ LOW: '#43a047', MEDIUM: '#fb8c00', HIGH: '#e53935', CRITICAL: '#7b1fa2' } as Record<string, string>)[prioridade] ?? '#666';
  }

  voltar(): void {
    this.roteador.navigate(['/projects']);
  }
}
