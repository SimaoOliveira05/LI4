const eur = new Intl.NumberFormat('pt-PT', {
  style: 'currency',
  currency: 'EUR',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

const intFmt = new Intl.NumberFormat('pt-PT');

export function formatEuro(v: number | null | undefined): string {
  return eur.format(Number(v ?? 0));
}

export function formatInt(v: number | null | undefined): string {
  return intFmt.format(Math.round(Number(v ?? 0)));
}

export function formatPercent(v: number | null | undefined): string {
  return `${Math.round(Number(v ?? 0))}%`;
}

const MESES_PT = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];

export function mensaisOrdenados(map: Record<string, number> | undefined): { labels: string[]; values: number[] } {
  if (!map) return { labels: [], values: [] };
  const chaves = Object.keys(map).sort();
  const labels = chaves.map((c) => {
    const m = c.match(/^(\d{4})-(\d{2})$/);
    if (!m) return c;
    const idx = parseInt(m[2], 10) - 1;
    return `${MESES_PT[idx] ?? m[2]}/${m[1].slice(2)}`;
  });
  const values = chaves.map((c) => map[c] ?? 0);
  return { labels, values };
}

export function topCategorias(map: Record<string, number> | undefined, n = 3): { labels: string[]; values: number[] } {
  if (!map) return { labels: [], values: [] };
  const ordenadas = Object.entries(map).sort((a, b) => b[1] - a[1]).slice(0, n);
  return {
    labels: ordenadas.map(([k]) => k),
    values: ordenadas.map(([, v]) => v),
  };
}

export function formatDataHora(iso: string | null | undefined): string {
  if (!iso) return '-';
  return iso.replace('T', ' ').slice(0, 16);
}

export function formatData(iso: string | null | undefined): string {
  if (!iso) return '-';
  const d = iso.slice(0, 10);
  const m = d.match(/^(\d{4})-(\d{2})-(\d{2})$/);
  return m ? `${m[3]}/${m[2]}/${m[1]}` : d;
}
