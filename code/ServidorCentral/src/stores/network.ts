import { defineStore } from 'pinia';
import { fetchStores, fetchShipments } from '@/api/networkService';
import type { Store, Shipment } from '@/api/mockData';

export const useNetworkStore = defineStore('network', {
  state: () => ({
    stores: [] as Store[],
    shipments: [] as Shipment[],
    loading: false,
    error: null as string | null,
  }),
  actions: {
    async loadStores() {
      if (this.stores.length > 0) return;
      this.loading = true;
      try {
        this.stores = await fetchStores();
      } catch (e) {
        this.error = 'Falha ao carregar lojas';
      } finally {
        this.loading = false;
      }
    },
    async loadShipments() {
      if (this.shipments.length > 0) return;
      this.loading = true;
      try {
        this.shipments = await fetchShipments();
      } catch (e) {
        this.error = 'Falha ao carregar remessas';
      } finally {
        this.loading = false;
      }
    },
    async refreshStores() {
      this.loading = true;
      try {
        this.stores = await fetchStores();
      } catch (e) {
        this.error = 'Falha ao atualizar lojas';
      } finally {
        this.loading = false;
      }
    }
  },
  getters: {
    totalSales: (state) => state.stores.reduce((acc, s) => acc + s.dailySales, 0),
    totalAlerts: (state) => state.stores.reduce((acc, s) => acc + s.stockAlerts, 0),
    storesReceived: (state) => state.stores.filter(s => s.receptionStatus === 'received').length,
    storesMissing: (state) => state.stores.filter(s => s.receptionStatus === 'missing').length,
    storesError: (state) => state.stores.filter(s => s.receptionStatus === 'error').length,
    
    getFilteredShipments: (state) => (storeId: string | null) => {
      if (!storeId || storeId === 'all') return state.shipments;
      return state.shipments.filter(s => s.storeId === storeId);
    }
  }
});
