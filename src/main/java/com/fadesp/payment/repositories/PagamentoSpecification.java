package com.fadesp.payment.repositories;

import com.fadesp.payment.domain.dtos.FiltroPagamentoDTO;
import com.fadesp.payment.domain.entities.Pagamento;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class PagamentoSpecification {

    private PagamentoSpecification() {
        // classe utilitária: não deve ser instanciada
    }

    public static Specification<Pagamento> comFiltro(FiltroPagamentoDTO filtro) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro.codigoDebito() != null) {
                predicates.add(builder.equal(root.get("codigoDebito"), filtro.codigoDebito()));
            }

            if (StringUtils.hasText(filtro.cpfCnpj())) {
                predicates.add(builder.equal(root.get("cpfCnpj"), filtro.cpfCnpj()));
            }

            if (filtro.status() != null) {
                predicates.add(builder.equal(root.get("status"), filtro.status()));
            }

            // garante apenas registros ativos
            predicates.add(builder.isTrue(root.get("ativo")));

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}