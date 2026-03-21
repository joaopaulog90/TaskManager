import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RespostaUsuario } from '../models/auth.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicoUsuario {
  private readonly URL_API = `${environment.apiUrl}/api/users`;

  constructor(private http: HttpClient) {}

  listarTodos(): Observable<RespostaUsuario[]> {
    return this.http.get<RespostaUsuario[]>(this.URL_API);
  }

  buscarPorEmail(email: string): Observable<RespostaUsuario> {
    return this.http.get<RespostaUsuario>(`${this.URL_API}/search`, { params: { email } });
  }

  alterarPerfil(id: number, perfil: 'ADMIN' | 'MEMBER'): Observable<RespostaUsuario> {
    return this.http.patch<RespostaUsuario>(`${this.URL_API}/${id}/profile`, { perfil });
  }
}
