import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RespostaMembro } from '../../core/models/project.model';
import { PrioridadeTarefa } from '../../core/models/task.model';

export interface NovaTarefa {
  titulo: string;
  descricao?: string;
  prioridade: PrioridadeTarefa;
  idResponsavel?: number;
  prazo?: string;
}

@Component({
  selector: 'app-formulario-tarefa',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrls: ['./formulario-tarefa.component.scss'],
  templateUrl: './formulario-tarefa.component.html'
})
export class FormularioTarefaComponent {
  @Input() membros: RespostaMembro[] = [];
  @Output() criar = new EventEmitter<NovaTarefa>();

  titulo = '';
  descricao = '';
  prioridade: PrioridadeTarefa = 'MEDIUM';
  idResponsavel: number | null = null;
  prazo = '';

  submeter(): void {
    if (!this.titulo.trim()) return;
    const tarefa: NovaTarefa = {
      titulo: this.titulo.trim(),
      prioridade: this.prioridade
    };
    if (this.descricao.trim()) tarefa.descricao = this.descricao.trim();
    if (this.idResponsavel) tarefa.idResponsavel = this.idResponsavel;
    if (this.prazo) tarefa.prazo = this.prazo + 'T23:59:59';
    this.criar.emit(tarefa);
    this.titulo = '';
    this.descricao = '';
    this.idResponsavel = null;
    this.prazo = '';
  }
}
