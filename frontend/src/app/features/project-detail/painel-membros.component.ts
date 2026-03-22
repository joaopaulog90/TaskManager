import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RespostaMembro } from '../../core/models/project.model';
import { RespostaUsuario } from '../../core/models/auth.model';

@Component({
  selector: 'app-painel-membros',
  standalone: true,
  imports: [CommonModule],
  styleUrls: ['./painel-membros.component.scss'],
  templateUrl: './painel-membros.component.html'
})
export class PainelMembrosComponent {
  @Input() membros: RespostaMembro[] = [];
  @Input() disponiveis: RespostaUsuario[] = [];
  @Input() idProprietario: number | undefined;
  @Input() erro = '';
  @Output() adicionar = new EventEmitter<number>();
  @Output() remover = new EventEmitter<number>();
}
