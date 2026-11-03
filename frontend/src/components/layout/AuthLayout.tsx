import { Outlet } from "react-router";
import { Brand } from "./Brand";
import { Dot } from "lucide-react";

export function AuthLayout() {
  return (
    <div className="grid min-h-screen lg:grid-cols-2">
      <aside className="hidden flex-col justify-between bg-brand-900 p-12 text-white lg:flex">
        <Brand light />
        <div>
          <h1 className="font-display text-4xl leading-tight font-bold text-white">
            Alugue o carro certo, sem burocracia.
          </h1>
          <p className="mt-4 max-w-md text-brand-100">
            Cadastre seus dados, solicite o aluguel e acompanhe a análise
            financeira e o contrato em um só lugar.
          </p>
        </div>
        <p className="text-sm text-brand-100/70">
          Carrent <Dot className="inline-flex" /> Sistema de aluguel de automóveis
        </p>
      </aside>
      <main className="flex items-center justify-center px-4 py-10">
        <div className="w-full max-w-md">
          <div className="mb-8 lg:hidden">
            <Brand />
          </div>
          <Outlet />
        </div>
      </main>
    </div>
  );
}
