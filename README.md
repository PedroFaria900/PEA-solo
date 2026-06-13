# PEA-solo
Aplicação móvel PWA para gestão, compra e validação de títulos de transporte (Bilhética).

---

## 🚀 Como Iniciar o Ambiente de Desenvolvimento Local

Siga estes passos por ordem para limpar, configurar e arrancar com todos os serviços.

### 1. Configuração Inicial / Reset da Base de Dados
Execute estes comandos apenas na primeira vez ou se precisar de reiniciar a base de dados do zero:

```bash
# A. (Opcional) Limpar dados anteriores de Docker e volumes
make clean-local

# B. Gerar o script SQL com dados de sementes (seed.sql)
make seed-generate

# C. Iniciar a infraestrutura postgres + redis temporariamente para popular
make dev-infra

# D. Popular a base de dados
make seed-local

# E. Registar o utilizador de teste padrão (maria@email.com / password123)
make test-user

# F. Aplicar os indexes de performance
make indexes-local

# G. Instalar as dependências do Frontend
make frontend-install

# H. Parar a infraestrutura temporária
make dev-stop
```

### 2. Execução Diária (Para desenvolver)
No dia a dia, para correr a aplicação, basta executar estes dois comandos (em terminais separados):

* **Terminal 1 (Backend + DB):**
  ```bash
  make dev
  ```

* **Terminal 2 (Frontend):**
  ```bash
  make frontend-dev
  ```

---

## 👥 Credenciais de Teste

Depois de iniciar o frontend e backend, aceda a **[http://localhost:3000](http://localhost:3000)** no seu browser e use a seguinte conta de teste padrão:

*   **Email:** `maria@email.com`
*   **Palavra-passe:** `password123`