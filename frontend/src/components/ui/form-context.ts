import { createContext } from "react";

export const FormErrorsContext = createContext<Record<string, string>>({});
