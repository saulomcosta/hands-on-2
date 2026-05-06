package com.hands_on.arquiteto.service;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import com.hands_on.arquiteto.entity.Order;
import com.hands_on.arquiteto.integration.EmailService;
import com.hands_on.arquiteto.integration.PaymentService;
import com.hands_on.arquiteto.repository.OrderRepository;

/**
 * CAMADA: SERVICE (Regra de Negócio / Orquestração do Domínio)
 *
 * Responsabilidade principal: - Implementar regras de negócio da aplicação - Orquestrar chamadas
 * entre: → Banco de dados (Repository) → Serviços externos (Integration Layer) - Controlar o fluxo
 * completo de criação de um pedido (Order)
 *
 * IMPORTANTE: Esta camada NÃO expõe endpoints HTTP e NÃO deve saber nada sobre HTTP. Ela também NÃO
 * deve conter SQL direto (isso é do Repository).
 *
 * Ela é o "coração" da lógica da aplicação.
 *
 * Fluxo típico: Controller → Service → Repository + Integrations
 */
@Service
public class OrderService {

    /**
     * Repositório responsável por persistência no banco de dados.
     *
     * Aqui usamos o padrão Repository do Spring Data JPA: - abstrai SQL - trabalha com entidades
     * Java
     */
    private final OrderRepository orderRepository;
    /**
     * Serviço externo responsável por pagamento.
     *
     * Representa integração com sistemas externos (gateway de pagamento, API etc)
     */
    private final PaymentService paymentService;
    /**
     * Serviço externo responsável por envio de email.
     *
     * Também é uma integração (pode ser SMTP, API externa, etc)
     */
    private final EmailService emailService;

    /**
     * Construtor com injeção de dependências (Dependency Injection).
     *
     * O Spring automaticamente injeta: - Repository - PaymentService - EmailService
     *
     * Isso reduz acoplamento e facilita testes unitários.
     */
    public OrderService(OrderRepository orderRepository, PaymentService paymentService,
            EmailService emailService) {
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
        this.emailService = emailService;
    }

    /**
     * CASO DE USO PRINCIPAL: Criar pedido (Order)
     *
     * Este método representa o fluxo completo de negócio:
     *
     * 1. Cria um pedido com status inicial "CREATED" 2. Persiste no banco de dados 3. Processa
     * pagamento (serviço externo) 4. Envia email de confirmação 5. Atualiza status para "COMPLETED"
     * 6. Persiste novamente o estado final
     *
     * IMPORTANTE (conceito de arquitetura): - Este método está acoplado a um fluxo sequencial
     * crítico - Se pagamento falhar, o fluxo quebra (não há tratamento de rollback aqui) - Em
     * sistemas reais, isso deveria ser transacional (@Transactional) para garantir consistência do
     * banco
     *
     * POSSÍVEL PROBLEMA ATUAL: - Order pode ser salva como CREATED - Payment pode falhar - Email
     * não será enviado - Sistema pode ficar inconsistente
     *
     * @param amount valor do pedido recebido da camada Controller
     * @return Order final com status atualizado
     */
    public Order createOrder(BigDecimal amount) {
        /**
         * 1. Criação da entidade em memória Status inicial: CREATED (pedido ainda não finalizado)
         */
        Order savedOrder = Order.builder().amount(amount).status("CREATED").build();
        /**
         * 2. Persistência inicial no banco de dados Aqui o pedido já existe fisicamente no
         * PostgreSQL
         */
        orderRepository.save(savedOrder);
        /**
         * 3. Integração com sistema externo de pagamento
         *
         * Possível ponto de falha: - Timeout - Falha simulada (como no seu Random) - Serviço
         * indisponível
         */
        paymentService.processPayment();
        /**
         * 4. Integração com serviço de email
         *
         * Objetivo: - Notificar cliente que pedido foi processado
         */
        emailService.sendEmail();
        /**
         * 5. Atualização de estado do pedido Se chegou até aqui, assume sucesso total do fluxo
         */
        savedOrder.setStatus("COMPLETED");
        /**
         * 6. Persistência final do estado atualizado
         */
        return orderRepository.save(savedOrder);
    }
}
