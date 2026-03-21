export interface RespostaProjeto {
  id: number;
  nome: string;
  descricao: string;
  idProprietario: number;
  nomeProprietario: string;
  criadoEm: string;
  quantidadeMembros: number;
}

export interface RequisicaoProjeto {
  nome: string;
  descricao?: string;
}

export interface RespostaMembro {
  idUsuario: number;
  nomeUsuario: string;
  emailUsuario: string;
  papel: 'ADMIN' | 'MEMBER';
  entradoEm: string;
}

export interface RequisicaoMembro {
  idUsuario: number;
  papel: 'ADMIN' | 'MEMBER';
}
