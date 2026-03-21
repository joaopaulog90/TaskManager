import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ServicoProjeto } from '../../core/services/project.service';
import { ServicoTarefa } from '../../core/services/task.service';
import { ServicoUsuario } from '../../core/services/user.service';
import { ServicoAutenticacao } from '../../core/services/auth.service';
import { ServicoToast } from '../../core/services/toast.service';
import { RespostaProjeto, RespostaMembro } from '../../core/models/project.model';
import { RespostaUsuario } from '../../core/models/auth.model';
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
        <div style="flex:1;"></div>
        <button *ngIf="ehAdmin" (click)="alternarMembros()"
          [style.background]="mostrarMembros ? '#1976d2' : '#757575'"
          style="padding:0.5rem 1rem;color:white;border:none;border-radius:4px;cursor:pointer;">
          Membros ({{ membros.length }})
        </button>
      </div>

      <!-- PAINEL DE MEMBROS (toggle) -->
      <div *ngIf="mostrarMembros" style="background:white;padding:1.5rem;border-radius:8px;box-shadow:0 1px 4px rgba(0,0,0,0.1);margin-bottom:1.5rem;">
        <h3 style="margin-top:0;margin-bottom:1rem;">Membros do Projeto</h3>

        <div *ngIf="erroMembro" style="background:#fee;color:#c00;padding:0.75rem;border-radius:4px;margin-bottom:1rem;font-size:0.875rem;">
          {{ erroMembro }}
        </div>

        <div style="display:flex;gap:2rem;flex-wrap:wrap;">
          <div style="flex:1;min-width:300px;">
            <h4 style="margin-top:0;color:#388e3c;">No projeto</h4>
            <div *ngFor="let membro of membros"
              style="display:flex;align-items:center;justify-content:space-between;padding:0.5rem 0.75rem;border-bottom:1px solid #f0f0f0;">
              <div>
                <span style="font-weight:500;">{{ membro.nomeUsuario }}</span>
                <span style="font-size:0.8rem;color:#999;margin-left:0.5rem;">{{ membro.emailUsuario }}</span>
                <span [style.background]="membro.perfil === 'ADMIN' ? '#1976d2' : '#757575'"
                  style="border-radius:12px;padding:0.15rem 0.4rem;font-size:0.65rem;color:white;font-weight:600;margin-left:0.5rem;">
                  {{ membro.perfil }}
                </span>
              </div>
              <button *ngIf="membro.idUsuario !== projeto?.idProprietario"
                (click)="removerMembro(membro.idUsuario)"
                style="padding:0.2rem 0.5rem;background:#e53935;color:white;border:none;border-radius:4px;cursor:pointer;font-size:0.75rem;">
                Remover
              </button>
              <span *ngIf="membro.idUsuario === projeto?.idProprietario" style="font-size:0.7rem;color:#999;">dono</span>
            </div>
            <div *ngIf="membros.length === 0" style="color:#999;font-size:0.875rem;padding:0.5rem 0;">Nenhum membro.</div>
          </div>

          <div style="flex:1;min-width:300px;">
            <h4 style="margin-top:0;color:#1976d2;">Disponíveis para adicionar</h4>
            <div *ngFor="let usuario of usuariosDisponiveis()"
              style="display:flex;align-items:center;justify-content:space-between;padding:0.5rem 0.75rem;border-bottom:1px solid #f0f0f0;">
              <div>
                <span style="font-weight:500;">{{ usuario.nome }}</span>
                <span style="font-size:0.8rem;color:#999;margin-left:0.5rem;">{{ usuario.email }}</span>
              </div>
              <button (click)="adicionarMembroPorId(usuario.id)"
                style="padding:0.2rem 0.5rem;background:#388e3c;color:white;border:none;border-radius:4px;cursor:pointer;font-size:0.75rem;">
                Adicionar
              </button>
            </div>
            <div *ngIf="usuariosDisponiveis().length === 0" style="color:#999;font-size:0.875rem;padding:0.5rem 0;">Todos os usuários já são membros.</div>
          </div>
        </div>
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
          <input [(ngModel)]="novaDescricao" placeholder="Descrição (opcional)" style="flex:2;min-width:180px;padding:0.5rem;border:1px solid #ddd;border-radius:4px;" />
        </div>
        <div style="display:flex;gap:0.75rem;flex-wrap:wrap;align-items:flex-end;margin-top:0.75rem;">
          <select [(ngModel)]="novaPrioridade" style="padding:0.5rem;border:1px solid #ddd;border-radius:4px;">
            <option value="LOW">LOW</option>
            <option value="MEDIUM">MEDIUM</option>
            <option value="HIGH">HIGH</option>
            <option value="CRITICAL">CRITICAL</option>
          </select>
          <select [(ngModel)]="novoIdResponsavel" style="padding:0.5rem;border:1px solid #ddd;border-radius:4px;">
            <option [ngValue]="null">Sem responsável</option>
            <option *ngFor="let membro of membros" [ngValue]="membro.idUsuario">{{ membro.nomeUsuario }}</option>
          </select>
          <div style="display:flex;align-items:center;gap:0.25rem;">
            <label style="font-size:0.8rem;color:#666;">Prazo:</label>
            <input [(ngModel)]="novoPrazo" type="date" style="padding:0.5rem;border:1px solid #ddd;border-radius:4px;" />
          </div>
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
              <th style="padding:0.75rem 1rem;text-align:left;font-size:0.875rem;color:#666;">Criado em</th>
              <th style="padding:0.75rem 1rem;text-align:left;font-size:0.875rem;color:#666;">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let tarefa of tarefas" style="border-top:1px solid #f0f0f0;">
              <td style="padding:0.75rem 1rem;">
                <div style="font-weight:500;">{{ tarefa.titulo }}</div>
                <div *ngIf="tarefa.descricao" style="font-size:0.75rem;color:#999;">{{ tarefa.descricao }}</div>
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
              <td style="padding:0.75rem 1rem;">
                <select [ngModel]="tarefa.idResponsavel" (ngModelChange)="alterarResponsavel(tarefa, $event)"
                  style="padding:0.25rem 0.4rem;border:1px solid #ddd;border-radius:4px;font-size:0.8rem;">
                  <option [ngValue]="null">—</option>
                  <option *ngFor="let membro of membros" [ngValue]="membro.idUsuario">{{ membro.nomeUsuario }}</option>
                </select>
              </td>
              <td style="padding:0.75rem 1rem;font-size:0.875rem;color:#555;">{{ tarefa.prazo ? (tarefa.prazo | date:'dd/MM/yyyy') : '—' }}</td>
              <td style="padding:0.75rem 1rem;">
                <div style="font-size:0.75rem;color:#999;">{{ tarefa.criadoEm | date:'dd/MM/yy HH:mm' }}</div>
                <div *ngIf="tarefa.atualizadoEm !== tarefa.criadoEm" style="font-size:0.65rem;color:#bbb;">atualizado {{ tarefa.atualizadoEm | date:'dd/MM/yy HH:mm' }}</div>
              </td>
              <td style="padding:0.75rem 1rem;">
                <button (click)="deletarTarefa(tarefa)" style="padding:0.25rem 0.5rem;background:#e53935;color:white;border:none;border-radius:4px;cursor:pointer;font-size:0.75rem;">
                  Excluir
                </button>
              </td>
            </tr>
            <tr *ngIf="tarefas.length === 0">
              <td colspan="7" style="padding:2rem;text-align:center;color:#666;">Nenhuma tarefa encontrada.</td>
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
export class ProjectDetailComponent implements OnInit, OnDestroy {
  projeto: RespostaProjeto | null = null;
  tarefas: RespostaTarefa[] = [];
  resumo: RespostaResumoTarefa | null = null;
  idProjeto!: number;

  novoTitulo = '';
  novaDescricao = '';
  novaPrioridade: PrioridadeTarefa = 'MEDIUM';
  novoIdResponsavel: number | null = null;
  novoPrazo = '';

  filtroStatus: StatusTarefa | '' = '';
  filtroPrioridade: PrioridadeTarefa | '' = '';
  termoBusca = '';
  temporizadorBusca: any;

  paginaAtual = 0;
  totalPaginas = 0;
  erroGlobal = '';

  ehAdmin = false;
  mostrarMembros = false;
  membros: RespostaMembro[] = [];
  todosUsuarios: RespostaUsuario[] = [];
  erroMembro = '';

  private intervalPolling: any;
  private idsAtribuidosConhecidos = new Set<number>();

  constructor(
    private rota: ActivatedRoute,
    private roteador: Router,
    private servicoProjeto: ServicoProjeto,
    private servicoTarefa: ServicoTarefa,
    private servicoUsuario: ServicoUsuario,
    private servicoAuth: ServicoAutenticacao,
    private servicoToast: ServicoToast
  ) {}

  ngOnInit(): void {
    this.ehAdmin = localStorage.getItem('perfil') === 'ADMIN';
    this.idProjeto = Number(this.rota.snapshot.paramMap.get('id'));
    this.servicoProjeto.buscar(this.idProjeto).subscribe(p => this.projeto = p);
    this.carregarTarefas();
    this.carregarResumo();
    this.carregarMembros();
    this.iniciarPolling();
  }

  ngOnDestroy(): void {
    clearInterval(this.intervalPolling);
  }

  private iniciarPolling(): void {
    this.intervalPolling = setInterval(() => this.verificarNovasAtribuicoes(), 3000);
  }

  private verificarNovasAtribuicoes(): void {
    const meuId = this.servicoAuth.obterIdUsuarioAtual();
    this.servicoTarefa.listar(this.idProjeto, { page: 0, size: 100 }).subscribe(res => {
      for (const tarefa of res.conteudo) {
        if (tarefa.idResponsavel === meuId && !this.idsAtribuidosConhecidos.has(tarefa.id)) {
          this.servicoToast.mostrar(`Tarefa "${tarefa.titulo}" foi atribuída a você`, 'info');
        }
      }
      this.atualizarIdsConhecidos(res.conteudo, meuId);
      this.tarefas = res.conteudo;
      this.totalPaginas = res.totalPaginas;
    });
    this.carregarResumo();
  }

  private atualizarIdsConhecidos(tarefas: RespostaTarefa[], meuId: number): void {
    this.idsAtribuidosConhecidos.clear();
    for (const t of tarefas) {
      if (t.idResponsavel === meuId) {
        this.idsAtribuidosConhecidos.add(t.id);
      }
    }
  }

  alternarMembros(): void {
    this.mostrarMembros = !this.mostrarMembros;
    if (this.mostrarMembros) {
      this.carregarMembros();
      this.servicoUsuario.listarTodos().subscribe(lista => this.todosUsuarios = lista);
    }
  }

  usuariosDisponiveis(): RespostaUsuario[] {
    const idsMembros = new Set(this.membros.map(m => m.idUsuario));
    return this.todosUsuarios.filter(u => !idsMembros.has(u.id));
  }

  carregarTarefas(): void {
    const filtros: any = { page: this.paginaAtual };
    if (this.filtroStatus) filtros.status = this.filtroStatus;
    if (this.filtroPrioridade) filtros.priority = this.filtroPrioridade;
    this.servicoTarefa.listar(this.idProjeto, filtros).subscribe(res => {
      this.tarefas = res.conteudo;
      this.totalPaginas = res.totalPaginas;
      this.atualizarIdsConhecidos(res.conteudo, this.servicoAuth.obterIdUsuarioAtual());
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
    const req: any = {
      titulo: this.novoTitulo.trim(),
      descricao: this.novaDescricao.trim() || undefined,
      prioridade: this.novaPrioridade
    };
    if (this.novoIdResponsavel) req.idResponsavel = this.novoIdResponsavel;
    if (this.novoPrazo) req.prazo = this.novoPrazo + 'T23:59:59';
    const responsavelSelecionado = this.novoIdResponsavel;
    this.servicoTarefa.criar(this.idProjeto, req).subscribe({
      next: (tarefa) => {
        this.novoTitulo = '';
        this.novaDescricao = '';
        this.novoIdResponsavel = null;
        this.novoPrazo = '';
        this.carregarTarefas();
        this.carregarResumo();
        this.notificarSeAtribuidoAMim(tarefa);
      },
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

  alterarResponsavel(tarefa: RespostaTarefa, novoIdResponsavel: number | null): void {
    this.erroGlobal = '';
    this.servicoTarefa.atualizar(this.idProjeto, tarefa.id, { idResponsavel: novoIdResponsavel }).subscribe({
      next: (atualizada) => {
        Object.assign(tarefa, atualizada);
        this.notificarSeAtribuidoAMim(atualizada);
      },
      error: (err: any) => { this.erroGlobal = err.error?.detail || 'Erro ao atribuir responsável'; this.carregarTarefas(); }
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

  carregarMembros(): void {
    this.servicoProjeto.listarMembros(this.idProjeto).subscribe(lista => this.membros = lista);
  }

  adicionarMembroPorId(idUsuario: number): void {
    this.erroMembro = '';
    this.servicoProjeto.adicionarMembro(this.idProjeto, { idUsuario }).subscribe({
      next: () => this.carregarMembros(),
      error: (err: any) => { this.erroMembro = err.error?.detail || 'Erro ao adicionar membro'; }
    });
  }

  removerMembro(idUsuario: number): void {
    if (!confirm('Remover este membro do projeto?')) return;
    this.servicoProjeto.removerMembro(this.idProjeto, idUsuario).subscribe({
      next: () => this.carregarMembros(),
      error: (err: any) => { this.erroMembro = err.error?.detail || 'Erro ao remover membro'; }
    });
  }

  notificarSeAtribuidoAMim(tarefa: RespostaTarefa): void {
    const meuId = this.servicoAuth.obterIdUsuarioAtual();
    if (tarefa.idResponsavel === meuId) {
      this.servicoToast.mostrar(`Tarefa "${tarefa.titulo}" foi atribuída a você`, 'info');
    }
  }

  voltar(): void {
    this.roteador.navigate(['/projects']);
  }
}
