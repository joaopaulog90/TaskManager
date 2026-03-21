export type StatusTarefa = 'TODO' | 'IN_PROGRESS' | 'DONE';
export type PrioridadeTarefa = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface RespostaTarefa {
  id: number;
  titulo: string;
  descricao: string;
  status: StatusTarefa;
  prioridade: PrioridadeTarefa;
  idResponsavel: number | null;
  nomeResponsavel: string | null;
  idProjeto: number;
  criadoEm: string;
  atualizadoEm: string;
  prazo: string | null;
}

export interface RequisicaoTarefa {
  titulo: string;
  descricao?: string;
  prioridade: PrioridadeTarefa;
  idResponsavel?: number;
  prazo?: string;
}

export interface RequisicaoAtualizacaoTarefa {
  titulo?: string;
  descricao?: string;
  status?: StatusTarefa;
  prioridade?: PrioridadeTarefa;
  idResponsavel?: number | null;
  idResponsavelFornecido?: boolean;
  prazo?: string;
}

export interface RespostaResumoTarefa {
  porStatus: Record<string, number>;
  porPrioridade: Record<string, number>;
}

export interface RespostaPaginada<T> {
  conteudo: T[];
  pagina: number;
  tamanho: number;
  totalElementos: number;
  totalPaginas: number;
}
