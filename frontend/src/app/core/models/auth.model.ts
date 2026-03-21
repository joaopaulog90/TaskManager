export interface RespostaUsuario {
  id: number;
  nome: string;
  email: string;
  perfil: 'ADMIN' | 'MEMBER';
}

export interface RequisicaoLogin {
  email: string;
  senha: string;
}

export interface RequisicaoCadastro {
  nome: string;
  email: string;
  senha: string;
}

export interface RespostaAutenticacao {
  token: string;
  idUsuario: number;
  email: string;
  perfil: 'ADMIN' | 'MEMBER';
}
