# Sugestão de extensões

 - [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)

 - [Container Tools](https://marketplace.visualstudio.com/items?itemName=ms-azuretools.vscode-containers)

 - [Spring Boot Extension Pack](https://marketplace.visualstudio.com/items?itemName=vmware.vscode-boot-dev-pack)

 # Como executar o projeto
 No terminal execute o comando `docker compose up`



Registrar Novo Cliente

    Método: POST
    URL: localhost:8081/auth/register

{
    "name": "Matheus",
    "email": "kapi@teste.com",
    "password": "senha12345678"
}

Criar Pedido

    Método: POST
    URL:localhost:8081/laundry/orders

{
    "clothesType": "Jeans e Camisetas",
    "washType": "NORMAL",
    "notes": "Cuidado com a cor"
}

Ver pedidos

localhost:8081/laundry/orders/my <------------------ Ver pedidos GET
localhost:8081/laundry/orders/4746713b-63da-4f9e-bf46-b7417796aaa9/cancel <------------------ deletar um pedido POST
localhost:8081/laundry/orders/9e9cbefa-35f9-469a-ad5c-7e4f264f1320/pay <-------------------- pagar sem preço POST
| POST | /laundry/orders/{id}/cancel | Cancela um pedido pendente. 

Login como Administrador

    Método: POST
    URL: localhost:8081/auth/login/password
{
    "email": "admin@lavanderia.com",
    "password": "admin123"
}

localhost:8081/users Listar usuários GET
localhost:8081/laundry/admin/orders ver as ordens GET

Precificar um Pedido

    URL: localhost:8081/laundry/admin/orders/{{pedido_id}}/price PUT

{
    "price": 45.50
}

localhost:8081/laundry/admin/orders/{{pedido_id}}/complete PUT

