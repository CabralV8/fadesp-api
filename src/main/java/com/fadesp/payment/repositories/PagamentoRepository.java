package com.fadesp.payment.repositories;

import com.fadesp.payment.domain.entities.Pagamento;
import com.fadesp.payment.domain.enums.StatusPagamentoEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long>, JpaSpecificationExecutor<Pagamento> {

    Optional<Pagamento> findByCodigoDebito(Integer codigoDebito);

    Optional<Pagamento> findByIdAndAtivoTrue(Long id);

    Page<Pagamento> findAllByAtivoTrue(Pageable pageable);

    Page<Pagamento> findAllByAtivoTrueAndCodigoDebito(Integer codigoDebito, Pageable pageable);


    Page<Pagamento> findAllByAtivoTrueAndCpfCnpjPagador(String cpfCnpjPagador, Pageable pageable);

    Page<Pagamento> findAllByAtivoTrueAndStatus(StatusPagamentoEnum status, Pageable pageable);
}

