import { Car, Plus } from "lucide-react";
import { useState } from "react";
import { useNavigate } from "react-router";
import { AutomovelCard } from "../components/automovel/AutomovelCard";
import { AutomovelFormModal } from "../components/automovel/AutomovelFormModal";
import { PedidoFormModal } from "../components/pedido/PedidoFormModal";
import { Button } from "../components/ui/Button";
import { DataState } from "../components/ui/DataState";
import { EmptyState } from "../components/ui/EmptyState";
import { Select } from "../components/ui/Field";
import { PageHeader } from "../components/ui/PageHeader";
import { useAuth } from "../hooks/useAuth";
import { useFetch } from "../hooks/useFetch";
import { automovelService } from "../services/automoveis";

const filters = [
  { value: "", label: "Todos" },
  { value: "true", label: "Disponíveis" },
  { value: "false", label: "Indisponíveis" },
];

export default function Automoveis() {
  const { isAgente, isCliente } = useAuth();
  const navigate = useNavigate();
  const [filter, setFilter] = useState("");
  const [novo, setNovo] = useState(false);
  const [solicitado, setSolicitado] = useState<number>();
  const automoveis = useFetch(
    () => automovelService.listar(filter ? filter === "true" : undefined),
    filter,
  );

  return (
    <>
      <PageHeader
        title="Automóveis"
        subtitle="Veículos cadastrados no sistema."
        actions={
          isAgente && (
            <Button onClick={() => setNovo(true)}>
              <Plus className="size-4" aria-hidden />
              Novo automóvel
            </Button>
          )
        }
      />
      <div className="mb-5 max-w-xs">
        <Select
          label="Situação"
          name="filtro"
          value={filter}
          onChange={(event) => setFilter(event.target.value)}
          options={filters}
        />
      </div>
      <DataState query={automoveis}>
        {(lista) =>
          lista.length === 0 ? (
            <EmptyState
              icon={Car}
              title="Nenhum automóvel encontrado"
              text="Ajuste o filtro ou cadastre um novo veículo."
            />
          ) : (
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {lista.map((automovel) => (
                <AutomovelCard
                  key={automovel.id}
                  automovel={automovel}
                  action={
                    isCliente && (
                      <Button
                        variant="secondary"
                        size="sm"
                        disabled={!automovel.disponivel}
                        onClick={() => setSolicitado(automovel.id)}
                      >
                        Solicitar aluguel
                      </Button>
                    )
                  }
                />
              ))}
            </div>
          )
        }
      </DataState>
      {novo && (
        <AutomovelFormModal
          onClose={() => setNovo(false)}
          onSuccess={automoveis.reload}
        />
      )}
      {solicitado && (
        <PedidoFormModal
          automovelId={solicitado}
          onClose={() => setSolicitado(undefined)}
          onSuccess={(pedido) => navigate(`/pedidos/${pedido.id}`)}
        />
      )}
    </>
  );
}
