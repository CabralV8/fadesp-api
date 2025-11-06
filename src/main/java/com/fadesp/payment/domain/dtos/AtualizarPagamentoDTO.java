package com.fadesp.payment.domain.dtos;


import com.fadesp.payment.domain.enums.StatusPagamentoEnum;
import jakarta.validation.constraints.NotNull;

public record AtualizarPagamentoDTO(

        @NotNull(message = "O ID do pagamento é obrigatório.")
        Long id,

        @NotNull(message = "O status é obrigatório.")
        StatusPagamentoEnum status
) { }