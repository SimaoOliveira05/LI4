import { stores, shipments, type Store, type Shipment } from './mockData';

export async function fetchStores(): Promise<Store[]> {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(stores);
    }, 500);
  });
}

export async function fetchShipments(): Promise<Shipment[]> {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(shipments);
    }, 500);
  });
}
