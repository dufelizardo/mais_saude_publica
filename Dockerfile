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
COPY --from=build /target/*.jar /tmp/app.jar

# Subida mais rápida com Class Data Sharing (ADR-0209). O jar é extraído (o CDS precisa do classpath
# em arquivos) e a aplicação faz uma "subida de treino" que só monta o contexto do Spring e sai
# (spring.context.exit=onRefresh), sem banco: o Flyway fica desligado, o Hibernate não consulta os
# metadados da conexão e o pool não tenta conectar. As classes carregadas ficam em app.jsa e a JVM de
# produção as reaproveita. Se o treino falhar, a imagem sai sem o arquivo e a JVM sobe sem CDS
# (-Xshare:auto): mais lenta, nunca quebrada.
WORKDIR /app
RUN java -Djarmode=tools -jar /tmp/app.jar extract --destination /app && rm /tmp/app.jar \
 && (java -XX:ArchiveClassesAtExit=/app/app.jsa -Dspring.context.exit=onRefresh \
      -Dspring.flyway.enabled=false \
      -Dspring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false \
      -Dspring.datasource.hikari.initialization-fail-timeout=-1 \
      -Dspring.datasource.url=jdbc:postgresql://127.0.0.1:1/treino \
      -Dspring.datasource.username=treino -Dspring.datasource.password=treino \
      -Dapp.security.jwt-secret=treino-do-cds-treino-do-cds-treino-do-cds-1234567890 \
      -jar /app/app.jar > /tmp/treino.log 2>&1 \
     || (echo "Treino do CDS falhou; a imagem segue sem o arquivo." && tail -20 /tmp/treino.log)) \
 && ls -la /app

# Executa o JAR da aplicação, com o arquivo do CDS quando existir
ENTRYPOINT ["java", "-XX:SharedArchiveFile=/app/app.jsa", "-Xshare:auto", "-jar", "/app/app.jar"]
