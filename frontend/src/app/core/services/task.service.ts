import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RespostaTarefa, RequisicaoTarefa, RequisicaoAtualizacaoTarefa, RespostaResumoTarefa, RespostaPaginada, StatusTarefa, PrioridadeTarefa } from '../models/task.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicoTarefa {
  private readonly URL_API = `${environment.apiUrl}/api/projects`;

  constructor(private http: HttpClient) {}

  listar(idProjeto: number, filtros?: { status?: StatusTarefa; priority?: PrioridadeTarefa; page?: number; size?: number }): Observable<RespostaPaginada<RespostaTarefa>> {
    let params = new HttpParams();
    if (filtros?.status) params = params.set('status', filtros.status);
    if (filtros?.priority) params = params.set('priority', filtros.priority);
    params = params.set('page', String(filtros?.page ?? 0));
    params = params.set('size', String(filtros?.size ?? 20));
    return this.http.get<RespostaPaginada<RespostaTarefa>>(`${this.URL_API}/${idProjeto}/tasks`, { params });
  }

  obter(idProjeto: number, idTarefa: number): Observable<RespostaTarefa> {
    return this.http.get<RespostaTarefa>(`${this.URL_API}/${idProjeto}/tasks/${idTarefa}`);
  }

  criar(idProjeto: number, req: RequisicaoTarefa): Observable<RespostaTarefa> {
    return this.http.post<RespostaTarefa>(`${this.URL_API}/${idProjeto}/tasks`, req);
  }

  atualizar(idProjeto: number, idTarefa: number, req: RequisicaoAtualizacaoTarefa): Observable<RespostaTarefa> {
    return this.http.put<RespostaTarefa>(`${this.URL_API}/${idProjeto}/tasks/${idTarefa}`, req);
  }

  deletar(idProjeto: number, idTarefa: number): Observable<void> {
    return this.http.delete<void>(`${this.URL_API}/${idProjeto}/tasks/${idTarefa}`);
  }

  pesquisar(idProjeto: number, termo: string): Observable<RespostaTarefa[]> {
    return this.http.get<RespostaTarefa[]>(`${this.URL_API}/${idProjeto}/tasks/search`, { params: { q: termo } });
  }

  resumo(idProjeto: number): Observable<RespostaResumoTarefa> {
    return this.http.get<RespostaResumoTarefa>(`${this.URL_API}/${idProjeto}/tasks/summary`);
  }
}
