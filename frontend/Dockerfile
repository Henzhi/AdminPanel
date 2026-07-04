# Frontend Dockerfile - Multi-stage build
# Stage 1: Build Vue app
FROM node:20-alpine AS builder
WORKDIR /build
COPY package*.json ./
RUN npm install --no-audit --no-fund
COPY . .
RUN npm run build

# Stage 2: Serve with Nginx
FROM nginx:alpine
COPY --from=builder /build/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
