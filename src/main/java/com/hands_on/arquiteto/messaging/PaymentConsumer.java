package com.hands_on.arquiteto.messaging;

import java.util.Random;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.hands_on.arquiteto.config.RabbitConfig;
import com.hands_on.arquiteto.entity.Order;

/**
 * ======== COMPONENTE: CONSUMIDOR DE MENSAGERIA (RabbitMQ) ========
 *
 * Esta classe representa um consumidor de mensagens dentro de uma arquitetura
 * orientada a eventos.
 *
 * RESPONSABILIDADE PRINCIPAL: - Escutar mensagens enviadas para uma fila do
 * RabbitMQ - Processar
 * eventos relacionados a pagamentos de pedidos (Order)
 *
 * CONTEXTO ARQUITETURAL: - Producer (OrderPublisher) envia mensagem - RabbitMQ
 * armazena na fila -
 * Este Consumer consome e processa a mensagem
 *
 * Fluxo: OrderService → OrderPublisher → RabbitMQ → PaymentConsumer
 */

@Component // Registra esta classe como um Bean gerenciado pelo Spring
public class PaymentConsumer {

    /**
     * ========= MÉTODO CONSUMIDOR DE MENSAGENS =========
     *
     * @RabbitListener: - Anotação do Spring AMQP - Define que este método "escuta"
     *                  uma fila
     *                  específica do RabbitMQ
     *
     *                  QUEUE: - Definida em RabbitConfig.QUEUE - Todas as mensagens
     *                  enviadas para
     *                  essa fila serão recebidas aqui
     *
     *                  OBSERVAÇÃO: - O Spring automaticamente desserializa a
     *                  mensagem em um objeto
     *                  Order - Isso depende de configuração de serializer (Jackson
     *                  por padrão)
     */
    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void process(Order order) {

        /**
         * LOG de entrada do processamento
         *
         * Aqui estamos simulando o início do processamento do pagamento para o pedido
         * recebido via
         * mensagem.
         */
        System.out.println("Processando pagamento para o pedido: " + order.getId());

        /**
         * SIMULAÇÃO DE FALHA ALEATÓRIA
         *
         * - Random().nextBoolean() gera true ou false aleatoriamente
         *
         * COMPORTAMENTO: - Se true → simula falha no pagamento - Se false → pagamento
         * "sucesso"
         *
         * IMPORTANTE: - Isso simula instabilidade de serviços externos - Muito comum em
         * integrações
         * reais (pagamentos, APIs externas)
         */
        if (new Random().nextBoolean()) {
            throw new RuntimeException("Falha no pagamento (Consumer)");
        }

        /**
         * CASO DE SUCESSO
         *
         * Se não ocorrer exceção: - Significa que o pagamento foi "processado com
         * sucesso" - Em
         * sistemas reais, aqui poderia ocorrer: → atualização de status no banco →
         * envio de email →
         * emissão de nota fiscal
         */
        System.out.println("Pagamento processado com sucesso.");
    }
}
