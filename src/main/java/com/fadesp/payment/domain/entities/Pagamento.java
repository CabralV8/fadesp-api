package com.fadesp.payment.domain.entities;

import com.fadesp.payment.domain.enums.MetodoPagamentoEnum;
import com.fadesp.payment.domain.enums.StatusPagamentoEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;

@Entity
@Table(
        name = "pagamento",
        indexes = {
                // use os NOME DAS COLUNAS no banco (snake_case)
                @Index(name = "idx_pagamento_codigo_debito", columnList = "codigo_debito"),
                @Index(name = "idx_pagamento_cpf_cnpj", columnList = "cpf_cnpj_pagador"),
                @Index(name = "idx_pagamento_status", columnList = "status")
        }
)
public class Pagamento extends RepresentationModel<Pagamento> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull @Positive
    @Column(name = "codigo_debito", nullable = false, updatable = false)
    private Integer codigoDebito;

    @NotBlank
    @Size(min = 11, max = 14)
    @Column(name = "cpf_cnpj_pagador", length = 14, nullable = false)
    private String cpfCnpjPagador;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pagamento", nullable = false, length = 30)
    private MetodoPagamentoEnum metodoPagamento;

    @Size(max = 20)
    @Column(name = "numero_cartao", length = 20)
    private String numeroCartao;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 15, fraction = 2)
    @Column(name = "valor_pagamento", nullable = false, precision = 17, scale = 2)
    private BigDecimal valorPagamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private StatusPagamentoEnum status = StatusPagamentoEnum.PENDENTE_PROCESSAMENTO;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = Boolean.TRUE;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getCodigoDebito() { return codigoDebito; }
    public void setCodigoDebito(Integer codigoDebito) { this.codigoDebito = codigoDebito; }

    public String getCpfCnpjPagador() { return cpfCnpjPagador; }
    public void setCpfCnpjPagador(String cpfCnpjPagador) { this.cpfCnpjPagador = cpfCnpjPagador; }

    public MetodoPagamentoEnum getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(MetodoPagamentoEnum metodoPagamento) { this.metodoPagamento = metodoPagamento; }

    public String getNumeroCartao() { return numeroCartao; }
    public void setNumeroCartao(String numeroCartao) { this.numeroCartao = numeroCartao; }

    public BigDecimal getValorPagamento() { return valorPagamento; }
    public void setValorPagamento(BigDecimal valorPagamento) { this.valorPagamento = valorPagamento; }

    public StatusPagamentoEnum getStatus() { return status; }
    public void setStatus(StatusPagamentoEnum status) { this.status = status; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
