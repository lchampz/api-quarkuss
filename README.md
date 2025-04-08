# 📚 API de Gerenciamento de Alunos, Escolas e Matrículas

Bem-vindo à API de Gerenciamento de Alunos, Escolas e Matrículas! Esta API foi desenvolvida utilizando o framework **Quarkus** e segue uma arquitetura modular para facilitar a manutenção e escalabilidade. A API permite gerenciar alunos, escolas e matrículas, oferecendo endpoints para CRUD completo e funcionalidades adicionais.

---

## **📂 Estrutura de Pastas**

A estrutura do projeto está organizada da seguinte forma:

```plaintext
src/main/java/org/acme
├── DTO
│   ├── InsertAlunoDTO.java          # DTO para criação de Aluno
│   ├── InsertEscolaDTO.java         # DTO para criação de Escola
│   └── InsertMatriculaDTO.java      # DTO para criação de Matrícula
├── controllers
│   ├── AlunoController.java         # Controlador para Alunos
│   ├── EscolaController.java        # Controlador para Escolas
│   └── MatriculaController.java     # Controlador para Matrículas
├── entities
│   ├── Aluno.java                   # Entidade Aluno
│   ├── Escola.java                  # Entidade Escola
│   └── Matricula.java               # Entidade Matrícula
├── exceptions
│   ├── AlunoException.java          # Exceção personalizada para Aluno
│   ├── EscolaException.java         # Exceção personalizada para Escola
│   └── MatriculaException.java      # Exceção personalizada para Matrícula
├── repositories
│   ├── AlunoRepository.java         # Repositório para Alunos
│   ├── EscolaRepository.java        # Repositório para Escolas
│   └── MatriculaRepository.java     # Repositório para Matrículas
├── ApiRoutes.java                   # Classe principal de roteamento
└── OpenApiConfig.java               # Configuração do OpenAPI (Swagger)
```

---

## **🛠️ Arquitetura**

A API segue uma arquitetura modular e bem definida:

1. **DTOs (Data Transfer Objects)**:

    - Responsáveis por transferir dados entre o cliente e o servidor.
    - Exemplo: `InsertAlunoDTO` é usado para criar um novo aluno.

2. **Entities**:

    - Representam as tabelas do banco de dados.
    - Exemplo: `Aluno`, `Escola`, `Matricula`.

3. **Repositories**:

    - Contêm a lógica de acesso ao banco de dados.
    - Utilizam o **Panache** para simplificar operações CRUD.

4. **Controllers**:

    - Lidam com as requisições HTTP e chamam os serviços necessários.
    - Exemplo: `AlunoController` gerencia os endpoints relacionados a alunos.

5. **Exceptions**:

    - Exceções personalizadas para tratar erros específicos de cada entidade.

6. **ApiRoutes**:

    - Classe principal que centraliza o roteamento da API.

7. **OpenAPI (Swagger)**:
    - Documentação automática gerada com base nos endpoints e anotações.

---

## **📘 Declaração das Entidades**

### **1. Aluno**

```java
@Entity
public class Aluno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String nome;
}
```

### **2. Escola**

```java
@Entity
public class Escola {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String nome;

    @Column(nullable = false)
    public int capacidade;
}
```

### **3. Matrícula**

```java
@Entity
public class Matricula {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String curso;

    @ManyToOne
    @JoinColumn(name = "escola_id", nullable = false)
    public Escola escola;

    @ManyToOne
    @JoinColumn(name = "aluno_id", nullable = false)
    public Aluno aluno;
}
```

---

## **🚀 Endpoints**

### **1. Alunos**

| Método   | Endpoint       | Descrição                     |
| -------- | -------------- | ----------------------------- |
| `GET`    | `/alunos`      | Lista todos os alunos         |
| `POST`   | `/alunos`      | Adiciona um novo aluno        |
| `PUT`    | `/alunos/{id}` | Atualiza os dados de um aluno |
| `DELETE` | `/alunos/{id}` | Remove um aluno               |

---

### **2. Escolas**

| Método   | Endpoint                   | Descrição                           |
| -------- | -------------------------- | ----------------------------------- |
| `GET`    | `/escolas`                 | Lista todas as escolas              |
| `POST`   | `/escolas`                 | Adiciona uma nova escola            |
| `PUT`    | `/escolas/{id}/capacidade` | Atualiza a capacidade de uma escola |
| `DELETE` | `/escolas/{id}`            | Remove uma escola                   |

---

### **3. Matrículas**

| Método   | Endpoint           | Descrição                   |
| -------- | ------------------ | --------------------------- |
| `GET`    | `/matriculas`      | Lista todas as matrículas   |
| `POST`   | `/matriculas`      | Adiciona uma nova matrícula |
| `DELETE` | `/matriculas/{id}` | Cancela uma matrícula       |

---

## **📘 Documentação Swagger**

A API utiliza o **Swagger UI** para documentar e testar os endpoints. Acesse a interface do Swagger em:

```
http://localhost:8080/q/openapi-ui
```

---

## **📦 Como Rodar o Projeto**

1. **Clone o repositório**:

   ```bash
   git clone https://github.com/seu-repositorio.git
   cd seu-repositorio
   ```

2. **Configure o banco de dados**:
   Atualize o arquivo `application.properties` com as credenciais do banco.

3. **Inicie a aplicação**:

   ```bash
   ./mvnw quarkus:dev
   ```

4. **Acesse a API**:
    - Swagger UI: [http://localhost:8080/q/openapi-ui](http://localhost:8080/q/openapi-ui)
    - Endpoints: [http://localhost:8080](http://localhost:8080)

---

## **📞 Suporte**

Se você tiver dúvidas ou problemas, entre em contato com a equipe de suporte:

- **E-mail**: suporte@exemplo.com
- **Site**: [https://www.exemplo.com/suporte](https://www.exemplo.com/suporte)

---

## **📝 Licença**

Este projeto está licenciado sob a licença **Apache 2.0**. Consulte o arquivo `LICENSE` para mais informações.

Código semelhante encontrado com 2 tipos de licença
