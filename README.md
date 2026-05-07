***

# 📘 Arquitetura Assíncrona com Mensageria

## 📌 Visão Geral

Nesta fase você evolui o sistema de um modelo **síncrono acoplado** para um modelo **assíncrono baseado em eventos** usando RabbitMQ.

Aqui começa a transição de um backend tradicional para uma arquitetura mais **resiliente, desacoplada e escalável**.

***

# 🎯 Objetivo da Fase

*   Eliminar chamadas diretas entre serviços
*   Introduzir comunicação assíncrona
*   Utilizar filas e eventos
*   Reduzir acoplamento entre componentes

***

# 🐇 INÍCIO — Subindo a Infraestrutura

## 📦 Executando RabbitMQ com Docker

```bash
docker run -d --name rabbit \
-p 5672:5672 -p 15672:15672 \
rabbitmq:3-management
```

***

## 🌐 Painel de controle

    http://localhost:15672

**Credenciais:**

    user: guest
    password: guest

***

## 🧭 Estrutura Conceitual

```text
Producer → Exchange → Queue → Consumer
```

***

## 🧩 Organograma da Mensageria

```text
[OrderService]
      ↓
[Exchange]
      ↓
[Queue]
      ↓
[PaymentService (Consumer)]
```

***

# 🔄 MEIO — Transformando o Fluxo

## 🔴 Antes (Fase 1 — acoplado)

```text
OrderService → PaymentService → EmailService
```

***

## 🟢 Agora (Fase 2 — desacoplado)

```text
OrderService → Evento → Fila → Consumer
```

***

## 📊 Comparação visual

### Antes (síncrono)

```text
[OrderService]
      ↓
[PaymentService] ❗ dependência direta
```

***

### Depois (assíncrono)

```text
[OrderService]
      ↓
[OrderPublisher]
      ↓
[Exchange]
      ↓
[Queue]
      ↓
[PaymentConsumer]
```

***

# 🧠 Criando o Producer

```java
@Service
public class OrderPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishOrderCreated(Order order) {
        rabbitTemplate.convertAndSend(
            "order.exchange",
            "order.created",
            order
        );
    }
}
```

***

## 📌 Responsabilidade do Producer

*   Não chama mais o PaymentService
*   Apenas publica um evento
*   Não conhece o consumidor

***

# 📥 Criando o Consumer

```java
@RabbitListener(queues = "payment.queue")
public void process(Order order) {
    System.out.println("Processando pagamento: " + order.getId());
}
```

***

## 📌 Responsabilidade do Consumer

*   Escutar eventos
*   Processar independentemente
*   Não impactar o produtor

***

# 🔗 Fluxo Completo

```text
[Client]
   ↓
[OrderController]
   ↓
[OrderService]
   ↓
[OrderPublisher]
   ↓
[Exchange]
   ↓
[Queue]
   ↓
[PaymentConsumer]
```

***

# 🧪 Testando o Fluxo

## ✅ Cenário normal

```text
Pedido criado ✅
Evento enviado ✅
Consumer processa ✅
HTTP 200 ✅
```

***

# ⚠️ ERRO REAL DA FASE 2 (IMPORTANTE)

Ao subir a aplicação, você pode se deparar com:

    java.lang.SecurityException:
    Attempt to deserialize unauthorized class
    com.hands_on.arquiteto.entity.Order

***

## ❌ O que está acontecendo?

👉 A mensagem chegou na fila
👉 O consumer tentou ler
❌ O Spring **bloqueou a desserialização**

***

## 🧠 Por que isso acontece?

Na Fase 1:

```text
Java → Java (método direto)
```

Na Fase 2:

```text
Java → bytes → fila → bytes → Java
```

👉 Agora existe **serialização**

***

## 🔐 Segurança do Spring AMQP

O Spring bloqueia por padrão porque:

> Desserializar objetos arbitrários pode permitir execução de código malicioso (RCE)

👉 Ou seja: **é uma proteção de segurança**

***

## 📌 Ponto exato do erro

Você enviou:

```java
Order (objeto Java)
```

E o Spring disse:

```text
"Eu não confio nessa classe vindo da fila"
```

***

# ✅ SOLUÇÕES

## 🟡 1. Trust All (apenas para estudo)

```properties
spring.amqp.deserialization.trust.all=true
```

OU

```bash
SPRING_AMQP_DESERIALIZATION_TRUST_ALL=true
```

✅ Resolve rápido
❌ Inseguro

***

## 🟢 2. Whitelist (melhor opção agora)

```java
@Bean
public MessageConverter messageConverter() {
    Jackson2JsonMessageConverter converter =
        new Jackson2JsonMessageConverter();

    converter.setTrustedPackages(
        "com.hands_on.arquiteto.entity"
    );

    return converter;
}
```

✅ Seguro
✅ Controlado
✅ Funciona bem na fase atual

***

## 🔵 3. A forma correta (arquitetura de verdade)

👉 **Não enviar entidades pela fila**

### ❌ Errado

```text
Enviar entidade JPA
```

***

### ✅ Certo (evento)

```java
public record OrderCreatedEvent(
    UUID orderId,
    BigDecimal amount
) {}
```

***

### Producer

```java
rabbitTemplate.convertAndSend(
    "order.exchange",
    "order.created",
    new OrderCreatedEvent(order.getId(), order.getAmount())
);
```

***

### Consumer

```java
@RabbitListener(queues = "payment.queue")
public void process(OrderCreatedEvent event) {
    ...
}
```

***

## ✅ Benefícios dessa abordagem

*   Desacoplamento real
*   Sem dependência de JPA
*   Mais segurança
*   Mais escalável

***

# 🚨 Por que o erro repete no log?

```text
Consumer falha
   ↓
Mensagem volta para fila
   ↓
Consumer tenta novamente
   ↓
Falha de novo
```

🔁 Loop infinito

***

👉 Isso prepara você para:

*   Retry
*   DLQ (Dead Letter Queue)

(**Fase 3**)

***

# 🚀 Resultado Final da Fase

## ✅ Ganhos

*   Desacoplamento
*   Resiliência
*   Escalabilidade
*   Evolução independente

***

# ⚠️ Novos Desafios

*   Retry de mensagens
*   Processamento duplicado
*   Consistência eventual

***

# 🧠 Conclusão

A Fase 2 é onde você entende:

> Sistemas distribuídos não trocam objetos, trocam **dados serializados**

***

## 💬 Em uma frase:

```text
"Eu não envio objetos. Eu envio eventos."
```

***

👉 Vamos continuar a estudar, vamos para a Fase 3
