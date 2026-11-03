const KEY = "carrent.session";

interface Session {
  token: string;
  expiraEm: number;
}

export function readSession(): Session | null {
  try {
    const session = JSON.parse(sessionStorage.getItem(KEY) ?? "null") as Session | null;
    if (typeof session?.token !== "string" || session.expiraEm <= Date.now()) {
      return null;
    }
    return session;
  } catch {
    return null;
  }
}

export function saveSession(session: Session) {
  try {
    sessionStorage.setItem(KEY, JSON.stringify(session));
  } catch {
    // sem armazenamento disponível: a sessão vale apenas até recarregar a página
  }
}

export function clearSession() {
  try {
    sessionStorage.removeItem(KEY);
  } catch {
    // nada a limpar
  }
}
