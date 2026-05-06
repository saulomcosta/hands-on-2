package com.hands_on.arquiteto.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * CAMADA: ENTITY (Modelo de Domínio / Persistência)
 *
 * Responsabilidade principal: - Representar uma tabela no banco de dados (PostgreSQL) - Ser o
 * modelo de dados da aplicação - Ser gerenciada pelo JPA/Hibernate
 *
 * IMPORTANTE: - Esta classe NÃO contém regras de negócio complexas - Ela NÃO deve conter lógica de
 * serviço - Ela é uma representação direta da tabela "orders"
 *
 * O Hibernate usa essa classe para: - Criar tabelas automaticamente (DDL) - Mapear linhas do banco
 * para objetos Java - Persistir objetos automaticamente
 */
@Entity
/**
 * Define explicitamente o nome da tabela no banco de dados.
 *
 * Sem isso, o Hibernate usaria "order" por padrão (o que causaria erro, pois ORDER é palavra
 * reservada no PostgreSQL).
 *
 * Aqui foi definido como "orders" para evitar conflito.
 */
@Table(name = "orders")
/**
 * Lombok: gera automaticamente getters e setters Evita código boilerplate repetitivo
 */
@Getter
@Setter
/**
 * Gera construtor vazio (necessário para JPA)
 */
@NoArgsConstructor
/**
 * Gera construtor com todos os campos
 */
@AllArgsConstructor
/**
 * Builder pattern: Permite criar objetos de forma mais legível e flexível
 *
 * Exemplo: Order.builder() .amount(BigDecimal.valueOf(100)) .status("CREATED") .build();
 */
@Builder(toBuilder = true)
public class Order implements Serializable {


    /**
     * CHAVE PRIMÁRIA (Primary Key)
     *
     * @Id → indica que este campo é identificador único
     *
     * @GeneratedValue → deixa o JPA gerar o valor automaticamente
     *
     *                 Tipo UUID: - garante unicidade global - evita colisão de IDs em sistemas
     *                 distribuídos
     */
    @Id
    @GeneratedValue
    private UUID id;
    /**
     * Valor do pedido
     *
     * BigDecimal é usado ao invés de double/float porque: - evita erros de precisão financeira - é
     * padrão em sistemas de pagamento
     */
    private BigDecimal amount;
    /**
     * Status do pedido
     *
     * Exemplos de valores possíveis: - CREATED (criado) - COMPLETED (finalizado) - FAILED (falhou)
     *
     * OBS: Em sistemas mais robustos, isso seria um ENUM.
     */
    private String status;

}
