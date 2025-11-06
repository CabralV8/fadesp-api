package com.fadesp.payment.domain.dtos;

import com.fadesp.payment.domain.enums.MetodoPagamentoEnum;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PagamentoDTO(

        @NotNull(message = "O código do débito é obrigatório.")
        @Positive(message = "O código do débito deve ser um inteiro positivo.")
        Integer codigoDebito,

        @NotBlank(message = "O CPF ou CNPJ do pagador é obrigatório.")
        @Size(min = 11, max = 14, message = "O CPF/CNPJ deve conter entre 11 e 14 dígitos.")
        String cpfCnpjPagador,

        @NotNull(message = "O método de pagamento é obrigatório.")
        MetodoPagamentoEnum metodoPagamento,

        @NotNull(message = "O valor do pagamento é obrigatório.")
        @DecimalMin(value = "0.01", message = "O valor do pagamento deve ser maior que zero.")
        @Digits(integer = 15, fraction = 2, message = "O valor deve possuir no máximo 15 dígitos inteiros e 2 decimais.")
        BigDecimal valorPagamento,

        @Size(max = 20, message = "O número do cartão deve possuir no máximo 20 caracteres.")
        String numeroCartao
) { }