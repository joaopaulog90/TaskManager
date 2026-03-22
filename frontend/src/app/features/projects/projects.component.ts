import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ServicoProjeto } from '../../core/services/project.service';
import { ServicoAutenticacao } from '../../core/services/auth.service';
import { ServicoUsuario } from '../../core/services/user.service';
import { RespostaProjeto } from '../../core/models/project.model';
import { RespostaUsuario } from '../../core/models/auth.model';

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrls: ['./projects.component.scss'],
  templateUrl: './projects.component.html'
})
export class ProjectsComponent implements OnInit {
  projetos: RespostaProjeto[] = [];
  carregando = false;
  novoNome = '';
  novaDescricao = '';
  erroAoCriar = '';
  erroAoDeletar = '';

  abaAtiva: 'projetos' | 'usuarios' = 'projetos';
  ehAdmin = false;
  idUsuarioAtual = 0;
  usuarios: RespostaUsuario[] = [];
  erroUsuario = '';
  sucessoUsuario = '';

  constructor(
    private servicoProjeto: ServicoProjeto,
    private servicoAuth: ServicoAutenticacao,
    private servicoUsuario: ServicoUsuario,
    private roteador: Router
  ) {}

  ngOnInit(): void {
    this.ehAdmin = localStorage.getItem('perfil') === 'ADMIN';
    this.idUsuarioAtual = this.servicoAuth.obterIdUsuarioAtual();
    this.carregar();
  }

  carregar(): void {
    this.carregando = true;
    this.servicoProjeto.listar().subscribe({
      next: (data) => { this.projetos = data; this.carregando = false; },
      error: () => this.carregando = false
    });
  }

  criar(): void {
    this.erroAoCriar = '';
    this.servicoProjeto.criar({ nome: this.novoNome.trim(), descricao: this.novaDescricao.trim() }).subscribe({
      next: () => { this.novoNome = ''; this.novaDescricao = ''; this.carregar(); },
      error: (err: any) => { this.erroAoCriar = err.error?.detail || 'Erro ao criar projeto'; }
    });
  }

  deletar(id: number): void {
    if (!confirm('Excluir este projeto?')) return;
    this.erroAoDeletar = '';
    this.servicoProjeto.deletar(id).subscribe({
      next: () => this.carregar(),
      error: (err: any) => { this.erroAoDeletar = err.error?.detail || 'Erro ao excluir projeto. Verifique se você é ADMIN.'; }
    });
  }

  abrir(projeto: RespostaProjeto): void {
    this.roteador.navigate(['/projects', projeto.id]);
  }

  carregarUsuarios(): void {
    this.servicoUsuario.listarTodos().subscribe({
      next: (lista) => this.usuarios = lista,
      error: (err: any) => { this.erroUsuario = err.error?.detail || 'Erro ao carregar usuários'; }
    });
  }

  alternarPerfil(usuario: RespostaUsuario): void {
    this.erroUsuario = '';
    this.sucessoUsuario = '';
    const novoPerfil = usuario.perfil === 'ADMIN' ? 'MEMBER' : 'ADMIN';
    this.servicoUsuario.alterarPerfil(usuario.id, novoPerfil).subscribe({
      next: (atualizado) => {
        usuario.perfil = atualizado.perfil;
        this.sucessoUsuario = `Perfil de ${usuario.nome} alterado para ${atualizado.perfil}`;
      },
      error: (err: any) => { this.erroUsuario = err.error?.detail || 'Erro ao alterar perfil'; }
    });
  }

  alternarAba(): void {
    if (this.abaAtiva === 'usuarios') {
      this.abaAtiva = 'projetos';
    } else {
      this.abaAtiva = 'usuarios';
      this.carregarUsuarios();
    }
  }

  sair(): void {
    this.servicoAuth.sair();
  }
}
