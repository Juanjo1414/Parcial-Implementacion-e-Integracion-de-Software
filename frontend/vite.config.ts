import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// Durante "npm run dev" el frontend corre en :5173 y necesita que las
// llamadas a /api lleguen al backend en :8080 sin toparse con CORS.
// En producción (Nginx) el proxy equivalente se resuelve en nginx.conf.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
});
