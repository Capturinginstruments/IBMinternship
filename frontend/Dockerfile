# ─── Stage 1: Build React app ──────────────────────────────────────────────────
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci --silent
COPY . .
RUN npm run build

# ─── Stage 2: Serve with Nginx ─────────────────────────────────────────────────
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf.template
EXPOSE 80
CMD ["sh", "-c", "export BACKEND_HOST=${BACKEND_HOST:-http://farmer-backend:8080} && envsubst '${BACKEND_HOST}' < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'"]
