import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RespostaProjeto, RequisicaoProjeto, RespostaMembro, RequisicaoMembro } from '../models/project.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicoProjeto {
  private readonly URL_API = `${environment.apiUrl}/api/projects`;

  constructor(private http: HttpClient) {}

  listar(): Observable<RespostaProjeto[]> {
    return this.http.get<RespostaProjeto[]>(this.URL_API);
  }

  buscar(id: number): Observable<RespostaProjeto> {
    return this.http.get<RespostaProjeto>(`${this.URL_API}/${id}`);
  }

  criar(req: RequisicaoProjeto): Observable<RespostaProjeto> {
    return this.http.post<RespostaProjeto>(this.URL_API, req);
  }

  atualizar(id: number, req: RequisicaoProjeto): Observable<RespostaProjeto> {
    return this.http.put<RespostaProjeto>(`${this.URL_API}/${id}`, req);
  }

  deletar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.URL_API}/${id}`);
  }

  listarMembros(idProjeto: number): Observable<RespostaMembro[]> {
    return this.http.get<RespostaMembro[]>(`${this.URL_API}/${idProjeto}/members`);
  }

  adicionarMembro(idProjeto: number, req: RequisicaoMembro): Observable<RespostaMembro> {
    return this.http.post<RespostaMembro>(`${this.URL_API}/${idProjeto}/members`, req);
  }

  removerMembro(idProjeto: number, idUsuario: number): Observable<void> {
    return this.http.delete<void>(`${this.URL_API}/${idProjeto}/members/${idUsuario}`);
  }
}
