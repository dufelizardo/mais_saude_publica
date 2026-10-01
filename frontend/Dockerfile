FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npx ng build --configuration production

FROM nginx:1.27-alpine AS runtime
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
# Versao da release, calculada no CI a partir do pom.xml (ver publish-image.yml). Sem ela (build
# local), nenhum version.json e gerado e a tela simplesmente nao exibe versao.
ARG APP_VERSION=
RUN if [ -n "$APP_VERSION" ]; then printf '{"version":"%s"}\n' "$APP_VERSION" > /usr/share/nginx/html/version.json; fi
EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]
