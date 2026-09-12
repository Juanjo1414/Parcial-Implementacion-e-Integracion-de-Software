# ===== Etapa de build =====
# Compila la SPA con Node; esta imagen nunca llega a producción, solo genera
# el bundle estático final (carpeta dist/).
FROM node:20-alpine AS build
WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm install

COPY . .
RUN npm run build

# ===== Etapa de runtime =====
# Nginx sirve el bundle estático y hace de proxy inverso hacia el backend,
# para que el navegador solo vea un único origen (sin CORS en producción).
FROM nginx:1.27-alpine AS runtime

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=10s --timeout=3s --retries=5 \
  CMD wget -qO- http://localhost/ >/dev/null 2>&1 || exit 1

CMD ["nginx", "-g", "daemon off;"]
