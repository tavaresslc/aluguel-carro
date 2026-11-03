import { Link } from "react-router";
import { Form } from "../components/ui/Form";
import { Input } from "../components/ui/Field";
import { useAuth } from "../hooks/useAuth";
import { authService } from "../services/auth";

export default function Cadastro() {
  const { login } = useAuth();

  async function submit(data: FormData) {
    const email = String(data.get("email")).trim();
    const senha = String(data.get("senha"));
    if (senha !== data.get("confirmacao"))
      throw new Error("As senhas não coincidem.");
    await authService.cadastrar({
      nome: String(data.get("nome")).trim(),
      email,
      senha,
    });
    await login(email, senha);
  }

  return (
    <>
      <h1 className="text-2xl font-bold">Criar conta</h1>
      <p className="mt-1 mb-6 text-sm text-slate-600">
        Cadastre-se como cliente para solicitar aluguéis.
      </p>
      <Form fullWidth submitLabel="Criar conta" onSubmit={submit}>
        <Input
          label="Nome"
          name="nome"
          required
          maxLength={120}
          autoComplete="name"
        />
        <Input
          label="E-mail"
          name="email"
          type="email"
          required
          maxLength={160}
          autoComplete="email"
        />
        <Input
          label="Senha"
          name="senha"
          type="password"
          required
          minLength={8}
          maxLength={72}
          autoComplete="new-password"
          hint="Entre 8 e 72 caracteres."
        />
        <Input
          label="Confirmar senha"
          name="confirmacao"
          type="password"
          required
          minLength={8}
          maxLength={72}
          autoComplete="new-password"
        />
      </Form>
      <p className="mt-6 text-center text-sm text-slate-600">
        Já tem uma conta?{" "}
        <Link
          to="/login"
          className="font-medium text-brand-600 hover:underline"
        >
          Entrar
        </Link>
      </p>
    </>
  );
}
