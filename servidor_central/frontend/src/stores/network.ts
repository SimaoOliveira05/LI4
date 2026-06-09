import { defineStore } from 'pinia';
import { fetchStores, fetchShipments } from '@/api/networkService';
import type { Store, Shipment } from '@/api/types';

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
    getFilteredShipments: (state) => (storeId: string | null) => {
      if (!storeId || storeId === 'all') return state.shipments;
      return state.shipments.filter(s => s.storeId === storeId);
    }
  }
});
