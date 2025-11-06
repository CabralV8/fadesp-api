package com.fadesp.payment.domain.dtos;

import com.fadesp.payment.domain.enums.StatusPagamentoEnum;

public record FiltroPagamentoDTO(
        Integer codigoDebito,
        String cpfCnpj,
        StatusPagamentoEnum status
) { }