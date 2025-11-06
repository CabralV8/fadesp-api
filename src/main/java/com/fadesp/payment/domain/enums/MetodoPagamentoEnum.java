package com.fadesp.payment.domain.enums;

import java.util.EnumSet;
import java.util.Set;

public enum MetodoPagamentoEnum {
    BOLETO,
    PIX,
    CARTAO_CREDITO,
    CARTAO_DEBITO;

    public static MetodoPagamentoEnum fromString(String metodo) {
        for (MetodoPagamentoEnum m : values()) {
            if (m.name().equalsIgnoreCase(metodo)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Método de pagamento inválido: " + metodo);
    }

    public String getNome() {
        return name();
    }

    // ajuda a validar regras da service
    public static Set<MetodoPagamentoEnum> getMetodosCartao() {
        return EnumSet.of(CARTAO_CREDITO, CARTAO_DEBITO);
    }
}