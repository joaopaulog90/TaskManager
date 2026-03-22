import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DragDropModule, CdkDragDrop, transferArrayItem, moveItemInArray } from '@angular/cdk/drag-drop';
import { ServicoProjeto } from '../../core/services/project.service';
import { ServicoTarefa } from '../../core/services/task.service';
import { ServicoUsuario } from '../../core/services/user.service';
import { ServicoAutenticacao } from '../../core/services/auth.service';
import { ServicoToast } from '../../core/services/toast.service';
import { RespostaProjeto, RespostaMembro } from '../../core/models/project.model';
import { RespostaUsuario } from '../../core/models/auth.model';
import { RespostaTarefa, RespostaHistoricoTarefa, StatusTarefa, PrioridadeTarefa, RespostaResumoTarefa } from '../../core/models/task.model';
import { ResumoCardsComponent } from './resumo-cards.component';
import { HistoricoTarefaModalComponent } from './historico-tarefa-modal.component';
import { PainelMembrosComponent } from './painel-membros.component';
import { FormularioTarefaComponent, NovaTarefa } from './formulario-tarefa.component';

@Component({
  selector: 'app-project-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, DragDropModule, ResumoCardsComponent, HistoricoTarefaModalComponent, PainelMembrosComponent, FormularioTarefaComponent],
  styleUrls: ['./project-detail.component.scss'],
  templateUrl: './project-detail.component.html'
})
export class ProjectDetailComponent implements OnInit, OnDestroy {
  projeto: RespostaProjeto | null = null;
  tarefas: RespostaTarefa[] = [];
  resumo: RespostaResumoTarefa | null = null;
  idProjeto!: number;

  filtroStatus: StatusTarefa | '' = '';
  filtroPrioridade: PrioridadeTarefa | '' = '';
  termoBusca = '';
  temporizadorBusca: any;

  historicoTarefa: RespostaTarefa | null = null;
  historico: RespostaHistoricoTarefa[] = [];

  paginaAtual = 0;
  totalPaginas = 0;
  erroGlobal = '';

  ehAdmin = false;
  mostrarMembros = false;
  membros: RespostaMembro[] = [];
  todosUsuarios: RespostaUsuario[] = [];
  erroMembro = '';

  visao: 'lista' | 'board' = 'board';
  colunas: { status: StatusTarefa; label: string; tarefas: RespostaTarefa[] }[] = [];
  statusList: string[] = ['TODO', 'IN_PROGRESS', 'DONE'];

  private intervalPolling: any;
  private idsAtribuidosConhecidos = new Set<number>();
  private primeiroPolling = true;

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
    this.verificarNovasAtribuicoes();
    this.intervalPolling = setInterval(() => this.verificarNovasAtribuicoes(), 6000);
  }

  private verificarNovasAtribuicoes(): void {
    const meuId = this.servicoAuth.obterIdUsuarioAtual();
    this.servicoTarefa.listar(this.idProjeto, { page: 0, size: 100 }).subscribe(res => {
      if (this.primeiroPolling) {
        this.primeiroPolling = false;
      } else {
        for (const tarefa of res.conteudo) {
          if (tarefa.idResponsavel === meuId && !this.idsAtribuidosConhecidos.has(tarefa.id)) {
            this.servicoToast.mostrar(`Tarefa "${tarefa.titulo}" foi atribuída a você`, 'info');
          }
        }
      }
      this.atualizarIdsConhecidos(res.conteudo, meuId);

      const temFiltroAtivo = this.filtroStatus || this.filtroPrioridade || this.termoBusca.trim();
      if (!temFiltroAtivo) {
        this.tarefas = res.conteudo;
        this.totalPaginas = res.totalPaginas;
        this.atualizarColunas();
      }
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

  private atualizarColunas(): void {
    this.colunas = [
      { status: 'TODO' as StatusTarefa, label: 'TODO', tarefas: this.tarefas.filter(t => t.status === 'TODO') },
      { status: 'IN_PROGRESS' as StatusTarefa, label: 'IN PROGRESS', tarefas: this.tarefas.filter(t => t.status === 'IN_PROGRESS') },
      { status: 'DONE' as StatusTarefa, label: 'DONE', tarefas: this.tarefas.filter(t => t.status === 'DONE') }
    ];
  }

  aoSoltar(event: CdkDragDrop<RespostaTarefa[]>, novoStatus: StatusTarefa): void {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
      return;
    }
    const tarefa = event.previousContainer.data[event.previousIndex];
    const statusAnterior = tarefa.status;

    if (statusAnterior === 'DONE' && novoStatus === 'TODO') {
      this.servicoToast.mostrar('Tarefa DONE não pode voltar para TODO', 'erro');
      return;
    }

    transferArrayItem(event.previousContainer.data, event.container.data, event.previousIndex, event.currentIndex);
    tarefa.status = novoStatus;

    this.erroGlobal = '';
    this.servicoTarefa.atualizar(this.idProjeto, tarefa.id, { status: novoStatus }).subscribe({
      next: (atualizada) => {
        Object.assign(tarefa, atualizada);
        this.carregarResumo();
      },
      error: (err: any) => {
        tarefa.status = statusAnterior;
        transferArrayItem(event.container.data, event.previousContainer.data, event.currentIndex, event.previousIndex);
        const mensagem = err.error?.detail || 'Erro ao atualizar status';
        this.erroGlobal = mensagem;
        this.servicoToast.mostrar(mensagem, 'erro');
      }
    });
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
      this.atualizarColunas();
    });
  }

  carregarResumo(): void {
    this.servicoTarefa.resumo(this.idProjeto).subscribe(s => this.resumo = s);
  }

  aoBuscar(): void {
    clearTimeout(this.temporizadorBusca);
    this.temporizadorBusca = setTimeout(() => {
      if (this.termoBusca.trim().length >= 2) {
        this.servicoTarefa.pesquisar(this.idProjeto, this.termoBusca.trim()).subscribe(res => {
          this.tarefas = res;
          this.atualizarColunas();
        });
      } else if (this.termoBusca.trim().length === 0) {
        this.carregarTarefas();
      }
    }, 400);
  }

  criarTarefa(novaTarefa: NovaTarefa): void {
    this.erroGlobal = '';
    this.servicoTarefa.criar(this.idProjeto, novaTarefa as any).subscribe({
      next: (tarefa) => {
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

    if (statusAnterior === 'DONE' && novoStatus === 'TODO') {
      tarefa.status = statusAnterior;
      this.servicoToast.mostrar('Tarefa DONE não pode voltar para TODO', 'erro');
      return;
    }

    this.servicoTarefa.atualizar(this.idProjeto, tarefa.id, { status: novoStatus }).subscribe({
      next: (atualizada) => { Object.assign(tarefa, atualizada); this.carregarResumo(); this.atualizarColunas(); },
      error: (err: any) => {
        tarefa.status = statusAnterior;
        const mensagem = err.error?.detail || 'Erro ao atualizar status';
        this.erroGlobal = mensagem;
        this.servicoToast.mostrar(mensagem, 'erro');
      }
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

  verHistorico(tarefa: RespostaTarefa): void {
    this.historicoTarefa = tarefa;
    this.historico = [];
    this.servicoTarefa.historico(this.idProjeto, tarefa.id).subscribe(lista => this.historico = lista);
  }

  fecharHistorico(): void {
    this.historicoTarefa = null;
    this.historico = [];
  }

  notificarSeAtribuidoAMim(tarefa: RespostaTarefa): void {
    const meuId = this.servicoAuth.obterIdUsuarioAtual();
    if (tarefa.idResponsavel === meuId) {
      this.idsAtribuidosConhecidos.add(tarefa.id);
      this.servicoToast.mostrar(`Tarefa "${tarefa.titulo}" foi atribuída a você`, 'info');
    }
  }

  voltar(): void {
    this.roteador.navigate(['/projects']);
  }
}
