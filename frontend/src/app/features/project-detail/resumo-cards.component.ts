import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RespostaResumoTarefa } from '../../core/models/task.model';

@Component({
  selector: 'app-resumo-cards',
  standalone: true,
  imports: [CommonModule],
  styleUrls: ['./resumo-cards.component.scss'],
  templateUrl: './resumo-cards.component.html'
})
export class ResumoCardsComponent {
  @Input() resumo: RespostaResumoTarefa | null = null;

  entradas(): { label: string; count: number }[] {
    if (!this.resumo) return [];
    return Object.entries(this.resumo.porStatus).map(([label, count]) => ({ label, count }));
  }
}
