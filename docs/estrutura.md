# 📁 Estrutura do Projeto - BLK Burguer

## 🧠 Como o sistema funciona

Interface → Controller → Service → Repository → Banco

---

## 📦 Pastas principais

### controller/

Controla ações da interface (JavaFX)
Ex: clique de botão, abrir tela, salvar dados

### service/

Regras de negócio do sistema
Ex: calcular pedido, validar login, controlar estoque

### repository/

Acesso ao banco de dados (MySQL)
Ex: salvar, buscar, atualizar dados

### model/

Estrutura dos dados (tabelas)
Ex: Produto, Cliente, Pedido, Usuário

---

## 🎨 Interface

### resources/fxml/

Telas do sistema

### resources/css/

Estilo visual

### resources/images/

Imagens

---

## 🔧 Outras pastas

### util/

Funções auxiliares (conexão banco, alertas)

### config/

Configurações do sistema

---

## ⚠️ Regras importantes

* Não colocar SQL no controller
* Não colocar lógica no repository
* Sempre usar o fluxo: Controller → Service → Repository
