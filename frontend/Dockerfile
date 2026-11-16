# ---------- Stage 1: build the React app ----------
FROM node:22-alpine AS build
WORKDIR /app

# Install dependencies first so Docker can cache them between builds
COPY package.json package-lock.json ./
RUN npm ci

# Then copy the source code and build the static files into /app/dist
COPY . .
RUN npm run build

# ---------- Stage 2: serve it with nginx ----------
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
