# syntax=docker/dockerfile:1
FROM ubuntu:latest AS build

# Atualiza o repositório e instala o JDK 17
RUN apt-get update && apt-get install openjdk-17-jdk -y

# Instala Maven
RUN apt-get install maven -y

# Copia o projeto para o container
COPY . .

# Faz o build do projeto com Maven (sem rodar os testes - eles precisam de um banco de dados
# de verdade, que nao existe durante o build da imagem; o gate de JUnit roda separado, no
# pipeline.yml). O cache mount do ~/.m2 evita rebaixar as mesmas dependencias a cada build -
# nao pula nenhum passo, so reaproveita o que ja foi baixado antes.
RUN --mount=type=cache,target=/root/.m2 mvn clean install -DskipTests

# Cria a imagem final, so com o JRE (a imagem "openjdk" foi descontinuada no Docker Hub)
FROM eclipse-temurin:17-jre-alpine

# Exponha a porta 8080
EXPOSE 8080

# Define o profile ativo como produção
ENV SPRING_PROFILES_ACTIVE=prod

# Copia o JAR gerado para o ambiente de produção (glob em vez do nome fixo - o artifactId do
# pom.xml e "mais_saude_publica", com underscore, entao "maissaudepublica-..." nunca existiu)
COPY --from=build /target/*.jar app.jar

# Executa o JAR da aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
