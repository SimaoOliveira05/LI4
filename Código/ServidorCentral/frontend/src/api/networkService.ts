import { http } from './http';
import type {
  Store,
  Shipment,
  RawMonitorRede,
  RawRemessa,
  RawLoja,
  DashboardGlobal,
  DashboardLoja,
  RelatorioLinha,
} from './types';

function mapShipmentStatus(estado: RawRemessa['estadoPagamento']): Shipment['status'] {
  return estado === 'PAGA' ? 'received' : 'pending';
}

export async function fetchStores(): Promise<Store[]> {
  const monitor = await http.get<RawMonitorRede>('/api/monitor');
  return monitor.estadoPorLoja.map((l) => ({
    id: l.idLoja,
    name: l.nomeLoja,
    receptionStatus: l.estado,
    dailySales: 0,
    stockAlerts: 0,
    lastUpdate: l.horaRecepcao ?? '-',
  }));
}

export async function fetchShipments(idLoja?: string, estadoPagamento?: string): Promise<Shipment[]> {
  const rem = await http.get<RawRemessa[]>('/api/remessas', { idLoja, estadoPagamento });
  return rem.map((r) => ({
    id: `SH-${String(r.id).padStart(4, '0')}`,
    storeId: r.idLoja,
    storeName: r.nomeLoja,
    supplier: r.nomeFornecedor,
    documentNumber: `#${r.id}`,
    date: r.dataRecepcao,
    status: mapShipmentStatus(r.estadoPagamento),
    totalValue: r.valorTotalGuia,
  }));
}

export function fetchLojas(): Promise<RawLoja[]> {
  return http.get<RawLoja[]>('/api/lojas');
}

export function fetchDashboardGlobal(inicio?: string, fim?: string): Promise<DashboardGlobal> {
  return http.get<DashboardGlobal>('/api/dashboard/global', { inicio, fim });
}

export function fetchDashboardLoja(idLoja: string, inicio?: string, fim?: string): Promise<DashboardLoja> {
  return http.get<DashboardLoja>(`/api/dashboard/loja/${encodeURIComponent(idLoja)}`, { inicio, fim });
}

export function fetchMonitor(data?: string): Promise<RawMonitorRede> {
  return http.get<RawMonitorRede>('/api/monitor', { data });
}

export function fetchRelatorio(
  idLoja?: string,
  inicio?: string,
  fim?: string,
  categoria?: string,
): Promise<RelatorioLinha[]> {
  return http.get<RelatorioLinha[]>('/api/relatorios', { idLoja, inicio, fim, categoria });
}
