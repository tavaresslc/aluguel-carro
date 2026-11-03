import { BrowserRouter, Navigate, Route, Routes } from "react-router";
import { AppLayout } from "./components/layout/AppLayout";
import { AuthLayout } from "./components/layout/AuthLayout";
import { GuestOnly, RequireAuth } from "./components/layout/RouteGuards";
import { AuthProvider } from "./context/AuthProvider";
import { useAuth } from "./hooks/useAuth";
import Automoveis from "./pages/Automoveis";
import Cadastro from "./pages/Cadastro";
import ContratoDetalhe from "./pages/ContratoDetalhe";
import Contratos from "./pages/Contratos";
import Login from "./pages/Login";
import PedidoDetalhe from "./pages/PedidoDetalhe";
import Pedidos from "./pages/Pedidos";
import Perfil from "./pages/Perfil";

function Home() {
  const { usuario, isCliente } = useAuth();
  const precisaCompletar = isCliente && !usuario?.contratante?.dadosCompletos;
  return <Navigate to={precisaCompletar ? "/perfil" : "/pedidos"} replace />;
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route element={<GuestOnly />}>
            <Route element={<AuthLayout />}>
              <Route path="/login" element={<Login />} />
              <Route path="/cadastro" element={<Cadastro />} />
            </Route>
          </Route>
          <Route element={<RequireAuth />}>
            <Route index element={<Home />} />
            <Route element={<AppLayout />}>
              <Route path="/automoveis" element={<Automoveis />} />
              <Route path="/pedidos" element={<Pedidos />} />
              <Route path="/pedidos/:id" element={<PedidoDetalhe />} />
              <Route path="/contratos" element={<Contratos />} />
              <Route path="/contratos/:id" element={<ContratoDetalhe />} />
              <Route path="/perfil" element={<Perfil />} />
            </Route>
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
