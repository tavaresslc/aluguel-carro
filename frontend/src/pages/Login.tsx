import { Link } from "react-router";
import { Form } from "../components/ui/Form";
import { Input } from "../components/ui/Field";
import { useAuth } from "../hooks/useAuth";

export default function Login() {
  const { login } = useAuth();

  return (
    <>
      <h1 className="text-2xl font-bold">Entrar</h1>
      <p className="mt-1 mb-6 text-sm text-slate-600">
        Acesse sua conta para continuar.
      </p>
      <Form
        fullWidth
        submitLabel="Entrar"
        onSubmit={(data) =>
          login(String(data.get("email")).trim(), String(data.get("senha")))
        }
      >
        <Input
          label="E-mail"
          name="email"
          type="email"
          required
          autoComplete="username"
          maxLength={160}
        />
        <Input
          label="Senha"
          name="senha"
          type="password"
          required
          autoComplete="current-password"
          maxLength={72}
        />
      </Form>
      <p className="mt-6 text-center text-sm text-slate-600">
        Ainda não tem conta?{" "}
        <Link
          to="/cadastro"
          className="font-medium text-brand-600 hover:underline"
        >
          Cadastre-se
        </Link>
      </p>
    </>
  );
}
