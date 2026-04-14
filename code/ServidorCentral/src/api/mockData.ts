export interface Store {
  id: string;
  name: string;
  receptionStatus: 'received' | 'missing' | 'error';
  dailySales: number;
  stockAlerts: number;
  lastUpdate: string;
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

export const stores: Store[] = [
  { id: '1', name: 'UM Gualtar (Loja 01)', receptionStatus: 'received', dailySales: 2450.50, stockAlerts: 0, lastUpdate: '2026-04-13 20:00' },
  { id: '2', name: 'UM Azurém (Loja 02)', receptionStatus: 'received', dailySales: 1890.20, stockAlerts: 2, lastUpdate: '2026-04-13 20:15' },
  { id: '3', name: 'Braga Centro (Loja 03)', receptionStatus: 'received', dailySales: 3200.00, stockAlerts: 1, lastUpdate: '2026-04-13 19:45' },
  { id: '4', name: 'Guimarães Centro (Loja 04)', receptionStatus: 'error', dailySales: 0, stockAlerts: 0, lastUpdate: '-' },
  { id: '5', name: 'Barcelos (Loja 05)', receptionStatus: 'received', dailySales: 1560.80, stockAlerts: 5, lastUpdate: '2026-04-13 20:30' },
  { id: '6', name: 'Famalicão (Loja 06)', receptionStatus: 'received', dailySales: 2100.00, stockAlerts: 0, lastUpdate: '2026-04-13 20:05' },
  { id: '7', name: 'Vila Verde (Loja 07)', receptionStatus: 'missing', dailySales: 0, stockAlerts: 0, lastUpdate: '-' },
  { id: '8', name: 'Esposende (Loja 08)', receptionStatus: 'received', dailySales: 1200.40, stockAlerts: 0, lastUpdate: '2026-04-13 21:00' },
  { id: '9', name: 'Póvoa de Lanhoso (Loja 09)', receptionStatus: 'received', dailySales: 980.50, stockAlerts: 3, lastUpdate: '2026-04-13 20:50' },
  { id: '10', name: 'Vieira do Minho (Loja 10)', receptionStatus: 'received', dailySales: 850.00, stockAlerts: 0, lastUpdate: '2026-04-13 21:10' },
  { id: '11', name: 'Amares (Loja 11)', receptionStatus: 'received', dailySales: 1120.30, stockAlerts: 1, lastUpdate: '2026-04-13 20:40' },
  { id: '12', name: 'Cabeceiras de Basto (Loja 12)', receptionStatus: 'missing', dailySales: 0, stockAlerts: 0, lastUpdate: '-' },
];

export const shipments: Shipment[] = [
  { id: 'SH-001', storeId: '1', storeName: 'UM Gualtar', supplier: 'UM-Suprimentos', documentNumber: 'GUI-8821', date: '2026-04-13', status: 'received', totalValue: 450.00 },
  { id: 'SH-002', storeId: '2', storeName: 'UM Azurém', supplier: 'Logística-Norte', documentNumber: 'FAT-9902', date: '2026-04-13', status: 'pending', totalValue: 1200.50 },
  { id: 'SH-003', storeId: '1', storeName: 'UM Gualtar', supplier: 'Delta Cafés', documentNumber: 'GUI-1234', date: '2026-04-12', status: 'received', totalValue: 85.20 },
  { id: 'SH-004', storeId: '3', storeName: 'Braga Centro', supplier: 'Coca-Cola', documentNumber: 'GUI-5562', date: '2026-04-13', status: 'received', totalValue: 320.00 },
  { id: 'SH-005', storeId: '5', storeName: 'Barcelos', supplier: 'Luso-Distrib', documentNumber: 'FAT-1122', date: '2026-04-11', status: 'received', totalValue: 210.00 },
];
