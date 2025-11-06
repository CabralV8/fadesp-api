# FADESP - Desafio Técnico Nível 1

O desafio consiste no desenvolvimento de uma **API REST** para o processamento de pagamentos de débitos, contemplando **pessoas físicas e jurídicas**.  
A aplicação possibilita **realizar, consultar, atualizar e excluir pagamentos**, bem como atualizar o **status de cada transação**.

## Objetivo

Ao receber um pagamento, a API deve armazená-lo no banco de dados com o status **“Pendente de Processamento”**.  
Posteriormente, uma aplicação externa poderá atualizar o status de **“Pendente”** para **“Processado”**.

Além disso, a API deve permitir:

- A listagem de todos os pagamentos registrados.

- O filtro de pagamentos conforme critérios informados.

## Tecnologias Utilizadas

- Java 17

- Spring Boot 3

- Spring Data JPA

- H2 Database (em memória, para testes)

- Swagger / Springdoc OpenAPI (documentação interativa)

- Maven

- Docker / Docker Compose

## Execução local
### 1. Clonar o repositório

git clone https://github.com/CabralV8/fadesp-api.git

cd payment

### 2. Construir e executar

mvn clean install
mvn spring-boot:run

A aplicação será executada em:
http://localhost:8080

### Execução via Docker

#### 1. Gerar a imagem
   docker build -t payment:latest .
#### 2. Executar o container
   docker run --rm -p 8080:8080 --name payment payment:latest
### 3. Acessos úteis

- Swagger: http://localhost:8080/swagger-ui.html

- H2 Console: http://localhost:8080/h2-console

#### Configuração para login no H2:

- JDBC URL: jdbc:h2:mem:testdb
- User Name: sa
- Password: 

## Documentação da API

A documentação interativa está disponível em:

http://localhost:8080/swagger-ui.html

Nela constam:

- Todos os endpoints

- Parâmetros de entrada e saída

- Exemplos de requisição e resposta

- Códigos de status HTTP retornados

## Scripts e Coleções

- Em src/main/resources/collections há uma coleção pronta para uso no Postman ou Insomnia.

- Um script SQL com dados de exemplo está localizado em src/main/resources/inserts.sql.

#### Dica:
Renomeie o arquivo para data.sql para que o Spring Boot o execute automaticamente na inicialização.

#  Autor

Valberton Cabral |
Desenvolvedor Java 
 ### Email: valbertonviana@gmail.com
 ### GitHub:  https://github.com/CabralV8
 ### LinkedIn: www.linkedin.com/in/valbertoncabral
