package com.hands_on.arquiteto.config;

// Importações do Spring AMQP (integração com RabbitMQ)
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
// Importa anotação que indica que esta classe contém configurações do Sprin
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Classe de configuração do RabbitMQ na aplicação.
 *
 * 🧠 Responsabilidade: Esta classe define toda a infraestrutura de mensageria
 * utilizada pelo
 * sistema, configurando os principais componentes necessários para comunicação
 * assíncrona entre
 * serviços.
 *
 * 📦 O que é configurado aqui: - Exchange: ponto central que recebe as
 * mensagens - Queue (fila):
 * onde as mensagens ficam armazenadas - Binding: ligação entre exchange e fila
 * com base em uma
 * routing key
 *
 * 🔁 Fluxo configurado: Quando um pedido é criado (OrderService), uma mensagem
 * pode ser enviada
 * para a exchange "order.exchange" com a routing key "order.created".
 *
 * Essa mensagem será roteada automaticamente para a fila "payment.queue", onde
 * poderá ser consumida
 * por outro serviço (ex: PaymentService).
 *
 * 🚀 Benefícios dessa abordagem: - Desacoplamento entre componentes da
 * aplicação - Processamento
 * assíncrono (não bloqueia requisições HTTP) - Maior resiliência (falhas não
 * quebram o fluxo
 * principal) - Facilidade de escalar consumidores separadamente
 *
 * ⚙️ Integração com Spring Boot: O Spring gerencia automaticamente os beans
 * definidos aqui, criando
 * e registrando a exchange, fila e binding no RabbitMQ no momento da
 * inicialização da aplicação.
 *
 * 📌 Observação: Essa configuração é a base para implementação de Producers
 * (envio de mensagens) e
 * Consumers (processamento de mensagens), que serão utilizados nas próximas
 * etapas.
 */
@Configuration
public class RabbitConfig {

    // ================================
    // CONSTANTES DE CONFIGURAÇÃO
    // ================================

    // Nome da exchange (ponto central de roteamento de mensagens)
    public static final String EXCHANGE = "order.exchange";
    // Nome da fila onde mensagens serão armazenadas
    public static final String QUEUE = "payment.queue";
    // Routing key usada para direcionar mensagens
    public static final String ROUTING_KEY = "order.created";

    // ================================
    // EXCHANGE
    // ================================
    @Bean
    public DirectExchange exchange() {
        /*
         * Cria uma Exchange do tipo DIRECT.
         *
         * 🧠 O que é uma Exchange? É o componente responsável por receber mensagens do
         * produtor e
         * decidir para qual fila elas devem ir.
         *
         * 🧠 Tipo DIRECT: Envia a mensagem para a fila que tiver a routing key EXATA.
         *
         * Exemplo: Se enviar mensagem com routing key "order.created", ela será enviada
         * para a fila
         * associada com essa mesma key.
         */
        return new DirectExchange(EXCHANGE);
    }

    // ================================
    // QUEUE
    // ================================
    @Bean
    public Queue queue() {
        /*
         * Cria uma fila no RabbitMQ.
         *
         * 🧠 O que é uma fila? É onde as mensagens ficam armazenadas até serem
         * consumidas.
         *
         * Neste caso: payment.queue → fila responsável por processar pagamentos
         *
         * Fluxo: OrderService → envia mensagem → RabbitMQ → Queue → Consumer
         * (PaymentService)
         */
        return new Queue(QUEUE);
    }

    // ================================
    // BINDING (LIGAÇÃO)
    // ================================
    @Bean
    public Binding binding() {
        /*
         * Cria a ligação entre: - Exchange - Queue - Routing Key
         *
         * 🧠 O que é Binding? É a regra que conecta uma fila a uma exchange.
         *
         * Aqui estamos dizendo:
         *
         * "Toda mensagem enviada para a exchange 'order.exchange' com routing key
         * 'order.created'
         * deve ir para a fila 'payment.queue'"
         *
         * Fluxo completo:
         *
         * Producer (OrderService) ↓ Exchange (order.exchange) ↓ (routing key:
         * order.created) Queue
         * (payment.queue) ↓ Consumer (PaymentService)
         */
        return BindingBuilder.bind(queue()).to(exchange()).with(ROUTING_KEY);
    }
}
