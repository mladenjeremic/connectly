import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    // /api/** (uključujući /api/images/**) ide na Spring Boot backend
    proxy: { "/api": "http://localhost:8080" },
  },
});
