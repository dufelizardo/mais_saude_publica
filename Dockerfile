# Etapa de build
FROM ubuntu:latest AS build

# Instalar JDK e Maven em uma única etapa para reduzir camadas
RUN apt-get update && apt-get install -y openjdk-17-jdk maven

# Copiar o código fonte do projeto para a imagem
COPY . .

# Rodar o build do Maven para gerar o arquivo .jar
RUN mvn clean install

# Etapa de execução
FROM openjdk:17-jdk-slim

# Expor a porta 8080
EXPOSE 8080

# Definir o perfil ativo como "prod" (perfil de produção)
ENV SPRING_PROFILES_ACTIVE=prod

# Copiar o JAR gerado da etapa de build para a etapa de execução
COPY --from=build /target/maissaudepublica-0.0.1-SNAPSHOT.jar app.jar

# Definir o comando de inicialização da aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
