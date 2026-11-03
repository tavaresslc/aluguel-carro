const currency = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

export const formatCurrency = (value: number) => currency.format(value);

export function formatDate(iso: string) {
  const [year, month, day] = iso.slice(0, 10).split("-");
  return `${day}/${month}/${year}`;
}

export const formatDateTime = (iso: string) =>
  new Date(iso).toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" });

export const today = () => new Date().toLocaleDateString("en-CA");

export function maskCpf(value: string) {
  const digits = value.replace(/\D/g, "").slice(0, 11);
  return digits
    .replace(/^(\d{3})(\d)/, "$1.$2")
    .replace(/^(\d{3})\.(\d{3})(\d)/, "$1.$2.$3")
    .replace(/\.(\d{3})(\d)/, ".$1-$2");
}

export const vehicleName = (automovel: { marca: string; modelo: string }) => `${automovel.marca} ${automovel.modelo}`;
