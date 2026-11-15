import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // Any request to /api/... is forwarded to the Spring Boot backend.
    // This means the React code can call "/api/resources" directly
    // and we don't need any CORS setup in Spring Boot.
    proxy: {
      "/api": "http://localhost:8080",
    },
  },
});
