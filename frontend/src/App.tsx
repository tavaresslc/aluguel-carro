import { BrowserRouter, Route, Routes } from "react-router";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<h1>Página Inicial</h1>} />
        <Route path="/qualquer" element={<h1>Página Qualquer</h1>} />
      </Routes>
    </BrowserRouter>
  );
}
