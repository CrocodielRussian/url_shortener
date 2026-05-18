FROM node:20-alpine AS frontend-builder
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ .
RUN npm run build -- --configuration=production

FROM nginx:alpine
COPY --from=frontend-builder /app/dist/shortUrl/browser /usr/share/nginx/html

RUN chmod -R 755 /usr/share/nginx/html

COPY nginx/nginx.conf /etc/nginx/conf.d/default.conf

