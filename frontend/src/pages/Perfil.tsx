import { useEffect } from "react";
import { Alert } from "../components/ui/Alert";
import { Card, InfoGrid } from "../components/ui/Card";
import { DataState } from "../components/ui/DataState";
import { PageHeader } from "../components/ui/PageHeader";
import { ContratanteForm } from "../components/usuario/ContratanteForm";
import { useAuth } from "../hooks/useAuth";
import { useFetch } from "../hooks/useFetch";
import { usuarioService } from "../services/usuarios";
import { perfilLabel } from "../utils/labels";

export default function Perfil() {
  const { usuario, isCliente, atualizarUsuario } = useAuth();
  const perfil = useFetch(usuarioService.me);

  useEffect(() => {
    if (perfil.data) atualizarUsuario(perfil.data);
  }, [perfil.data, atualizarUsuario]);

  return (
    <>
      <PageHeader title="Meu perfil" subtitle="Dados da sua conta." />
      <DataState query={perfil}>
        {() =>
          usuario && (
            <div className="space-y-6">
              <Card title="Conta">
                <InfoGrid
                  items={[
                    { label: "Nome", value: usuario.nome },
                    { label: "E-mail", value: usuario.email },
                    { label: "Perfil", value: perfilLabel[usuario.perfil] },
                    ...(usuario.nomeInstituicao
                      ? [
                          {
                            label: "Instituição",
                            value: usuario.nomeInstituicao,
                          },
                        ]
                      : []),
                  ]}
                />
              </Card>
              {isCliente && (
                <Card title="Dados do contratante">
                  {!usuario.contratante?.dadosCompletos && (
                    <div className="mb-5">
                      <Alert tone="warning">
                        Informe seus dados pessoais e ao menos um vínculo
                        empregatício (até 3) para poder solicitar aluguéis.
                      </Alert>
                    </div>
                  )}
                  <ContratanteForm
                    contratante={usuario.contratante}
                    onSuccess={atualizarUsuario}
                  />
                </Card>
              )}
            </div>
          )
        }
      </DataState>
    </>
  );
}
