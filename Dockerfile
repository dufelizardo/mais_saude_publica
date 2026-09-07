FROM ubuntu:latest AS build

# Atualiza o repositório e instala o JDK 17
RUN apt-get update && apt-get install openjdk-17-jdk -y

# Instala Maven
RUN apt-get install maven -y

# Copia o projeto para o container
COPY . .

# Faz o build do projeto com Maven
RUN mvn clean install

# Cria a imagem final, so com o JRE (a imagem "openjdk" foi descontinuada no Docker Hub)
FROM eclipse-temurin:17-jre-alpine

# Exponha a porta 8080
EXPOSE 8080

# Define o profile ativo como produção
ENV SPRING_PROFILES_ACTIVE=prod

# Copia o JAR gerado para o ambiente de produção
COPY --from=build /target/maissaudepublica-0.0.1-SNAPSHOT.jar app.jar

# Executa o JAR da aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
