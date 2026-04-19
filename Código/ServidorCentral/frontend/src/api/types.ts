// Tipos partilhados entre o frontend e o backend (mantém os nomes Store/Shipment
// já usados pelos componentes existentes, adaptados ao formato real do servidor).

export interface Store {
  id: string;            // idLoja
  name: string;          // nomeLoja
  receptionStatus: 'received' | 'missing' | 'error';
  dailySales: number;    // 0 quando não calculado
  stockAlerts: number;   // 0 — servidor central não controla stock
  lastUpdate: string;    // hora da recepção do fecho ou '-'
}

export interface Shipment {
  id: string;
  storeId: string;
  storeName: string;
  supplier: string;
  documentNumber: string;
  date: string;
  status: 'received' | 'pending' | 'cancelled';
  totalValue: number;
}

export interface RawLoja {
  idLoja: string;
  nomeLoja: string;
}

export interface RawEstadoLoja {
  idLoja: string;
  nomeLoja: string;
  estado: 'received' | 'missing';
  horaRecepcao: string | null;
}

export interface RawMonitorRede {
  lojasEsperadas: number;
  lojasRecebidas: number;
  lojasPendentes: number;
  estadoPorLoja: RawEstadoLoja[];
}

export interface RawRemessa {
  id: number;
  idLoja: string;
  nomeLoja: string;
  nomeFornecedor: string;
  dataRecepcao: string;
  valorTotalGuia: number;
  estadoPagamento: 'PENDENTE_PAGAMENTO' | 'PAGA';
}

export interface DashboardGlobal {
  vendasTotais: number;
  totalTransacoes: number;
  ticketMedio: number;
  totalDevolucoes: number;
  percentagemNumerario: number;
  percentagemMultibanco: number;
  decomposicaoPorLoja: Array<{ idLoja: string; nomeLoja: string; vendas: number; transacoes: number; devolucoes: number; }>;
  vendasMensaisLoja1: Record<string, number>;
  vendasMensaisLoja2: Record<string, number>;
  vendasPorCategoria: Record<string, number>;
  lojasEsperadas: number;
  lojasSincronizadas: number;
}

export interface DashboardLoja {
  idLoja: string;
  nomeLoja: string;
  vendasTotais: number;
  totalTransacoes: number;
  totalDevolucoes: number;
  ticketMedio: number;
  historicoFechos: Array<{ id: number; idLoja: string; dataFecho: string; dataRecepcao: string; }>;
  logsAuditoria: Array<{ id: number; idLoja: string; acao: string; dataHora: string; nomeUtilizador: string; }>;
  vendasMensais: Record<string, number>;
  vendasPorCategoria: Record<string, number>;
}

export interface RelatorioLinha {
  data: string;
  idLoja: string;
  nomeLoja: string;
  categoria: string;
  vendas: number;
}
