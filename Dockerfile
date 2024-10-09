FROM ubuntu:latest AS build

# Atualiza o repositório e instala o JDK 17
RUN apt-get update && apt-get install openjdk-17-jdk -y

# Instala Maven
RUN apt-get install maven -y

# Define o diretório de trabalho
WORKDIR /app

# Copia o projeto para o container
COPY . .

# Faz o build do projeto com Maven
RUN mvn clean install -DskipTests

# Cria a imagem final com o JDK
FROM openjdk:17-jdk-slim

# Exponha a porta 8080
EXPOSE 8080

# Define o profile ativo como produção
ENV SPRING_PROFILES_ACTIVE=prod

# Copia o JAR gerado para o ambiente de produção
COPY --from=build /target/maissaudepublica-0.0.1-SNAPSHOT.jar app.jar

# Executa o JAR da aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
