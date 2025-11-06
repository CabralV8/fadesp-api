package com.fadesp.payment.domain.enums;

public enum StatusPagamentoEnum {
    PENDENTE_PROCESSAMENTO,
    PROCESSADO_SUCESSO,
    PROCESSADO_FALHA;

    public static StatusPagamentoEnum fromString(String status) {
        for (StatusPagamentoEnum s : values()) {
            if (s.name().equalsIgnoreCase(status)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Status de pagamento inválido: " + status);
    }

    public String getNome() {
        return name();
    }

    public boolean isPendente() {
        return this == PENDENTE_PROCESSAMENTO;
    }

    public boolean isSucesso() {
        return this == PROCESSADO_SUCESSO;
    }

    public boolean isFalha() {
        return this == PROCESSADO_FALHA;
    }
}
