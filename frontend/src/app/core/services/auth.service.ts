import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { RespostaAutenticacao, RequisicaoLogin, RequisicaoCadastro } from '../models/auth.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ServicoAutenticacao {
  private readonly URL_API = `${environment.apiUrl}/api/auth`;

  constructor(private http: HttpClient, private roteador: Router) {}

  autenticar(req: RequisicaoLogin): Observable<RespostaAutenticacao> {
    return this.http.post<RespostaAutenticacao>(`${this.URL_API}/login`, req).pipe(
      tap(res => {
        localStorage.setItem('token', res.token);
        localStorage.setItem('userId', String(res.idUsuario));
        localStorage.setItem('email', res.email);
      })
    );
  }

  cadastrar(req: RequisicaoCadastro): Observable<any> {
    return this.http.post(`${this.URL_API}/register`, req);
  }

  sair(): void {
    localStorage.clear();
    this.roteador.navigate(['/login']);
  }

  estaAutenticado(): boolean {
    return !!localStorage.getItem('token');
  }

  obterIdUsuarioAtual(): number {
    return Number(localStorage.getItem('userId'));
  }
}
