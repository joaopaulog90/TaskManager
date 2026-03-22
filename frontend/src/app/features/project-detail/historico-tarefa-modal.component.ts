import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RespostaHistoricoTarefa } from '../../core/models/task.model';

@Component({
  selector: 'app-historico-tarefa-modal',
  standalone: true,
  imports: [CommonModule],
  styleUrls: ['./historico-tarefa-modal.component.scss'],
  templateUrl: './historico-tarefa-modal.component.html'
})
export class HistoricoTarefaModalComponent {
  @Input() tituloTarefa = '';
  @Input() historico: RespostaHistoricoTarefa[] = [];
  @Output() fechar = new EventEmitter<void>();
}
