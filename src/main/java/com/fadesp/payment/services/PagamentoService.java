package com.fadesp.payment.services;

import com.fadesp.payment.domain.dtos.AtualizarPagamentoDTO;
import com.fadesp.payment.domain.dtos.FiltroPagamentoDTO;
import com.fadesp.payment.domain.dtos.PagamentoDTO;
import com.fadesp.payment.domain.entities.Pagamento;
import com.fadesp.payment.domain.enums.MetodoPagamentoEnum;
import com.fadesp.payment.domain.enums.StatusPagamentoEnum;
import com.fadesp.payment.exceptions.MetodoPagamentoException;
import com.fadesp.payment.exceptions.StatusPagamentoException;
import com.fadesp.payment.repositories.PagamentoRepository;
import com.fadesp.payment.repositories.PagamentoSpecification;
import com.fadesp.payment.util.IsCpfCnpj;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class PagamentoService {

    private static final Logger log = LoggerFactory.getLogger(PagamentoService.class);
    private final PagamentoRepository pagamentoRepository;

    public PagamentoService(PagamentoRepository pagamentoRepository) {
        this.pagamentoRepository = pagamentoRepository;
    }

    // =========================================================================
    // CRIAR PAGAMENTO
    // =========================================================================
    @Transactional
    public Pagamento realizar(PagamentoDTO dto) {
        log.info("[Pagamento] Iniciando criação de pagamento…");
        validarNovoPagamento(dto);

        Pagamento pagamento = new Pagamento();
        pagamento.setCodigoDebito(dto.codigoDebito());
        pagamento.setCpfCnpjPagador(normalizarCpfCnpj(dto.cpfCnpjPagador()));
        pagamento.setMetodoPagamento(dto.metodoPagamento());
        pagamento.setNumeroCartao(definirCartao(dto.metodoPagamento(), dto.numeroCartao()));
        pagamento.setValorPagamento(dto.valorPagamento().setScale(2, RoundingMode.HALF_UP));
        pagamento.setStatus(StatusPagamentoEnum.PENDENTE_PROCESSAMENTO);
        pagamento.setAtivo(Boolean.TRUE);

        Pagamento salvo = pagamentoRepository.save(pagamento);
        log.info("[Pagamento] Criado com ID {}", salvo.getId());
        return salvo;
    }

    // =========================================================================
    // ATUALIZAR STATUS
    // =========================================================================
    @Transactional
    public Pagamento atualizar(AtualizarPagamentoDTO dto) {
        log.info("[Pagamento] Atualizando status do pagamento {}…", dto.id());

        if (dto.status() == null) {
            throw new StatusPagamentoException("Status é obrigatório.");
        }

        Pagamento pagamento = pagamentoRepository.findById(dto.id())
                .orElseThrow(() -> new EntityNotFoundException("Pagamento não encontrado."));

        if (Boolean.FALSE.equals(pagamento.getAtivo())) {
            throw new StatusPagamentoException("Pagamento inativo não pode ter o status alterado.");
        }

        final StatusPagamentoEnum atual = pagamento.getStatus();
        final StatusPagamentoEnum novo = dto.status();

        switch (atual) {
            case PENDENTE_PROCESSAMENTO -> {
                if (novo == StatusPagamentoEnum.PROCESSADO_SUCESSO || novo == StatusPagamentoEnum.PROCESSADO_FALHA) {
                    pagamento.setStatus(novo);
                } else {
                    throw new StatusPagamentoException(
                            "Transição inválida: de PENDENTE_PROCESSAMENTO só para PROCESSADO_SUCESSO ou PROCESSADO_FALHA."
                    );
                }
            }
            case PROCESSADO_SUCESSO ->
                    throw new StatusPagamentoException("Pagamento já processado com sucesso; status não pode ser alterado.");
            case PROCESSADO_FALHA -> {
                if (novo == StatusPagamentoEnum.PENDENTE_PROCESSAMENTO) {
                    pagamento.setStatus(novo);
                } else {
                    throw new StatusPagamentoException(
                            "Transição inválida: de PROCESSADO_FALHA só para PENDENTE_PROCESSAMENTO."
                    );
                }
            }
            default -> throw new StatusPagamentoException("Status atual inválido.");
        }

        Pagamento salvo = pagamentoRepository.save(pagamento);
        log.info("[Pagamento] Status atualizado: {} -> {}", atual, salvo.getStatus());
        return salvo;
    }

    // =========================================================================
    // EXCLUSÃO LÓGICA
    // =========================================================================
    @Transactional
    public void excluir(Long id) {
        log.info("[Pagamento] Exclusão lógica do pagamento {}…", id);

        Pagamento pagamento = pagamentoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Pagamento não encontrado ou já inativo."));

        if (pagamento.getStatus() == StatusPagamentoEnum.PENDENTE_PROCESSAMENTO) {
            pagamento.setAtivo(Boolean.FALSE);
            pagamentoRepository.save(pagamento);
            log.info("[Pagamento] Marcado como inativo (exclusão lógica).");
        } else {
            throw new StatusPagamentoException(
                    "Somente pagamentos com status PENDENTE_PROCESSAMENTO podem ser excluídos logicamente."
            );
        }
    }

    // =========================================================================
    // CONSULTAS
    // =========================================================================
    @Transactional(readOnly = true)
    public Optional<Pagamento> buscarPorId(Long id) {
        return pagamentoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Pagamento> listar(FiltroPagamentoDTO filtro) {
        Specification<Pagamento> spec = Specification.where(PagamentoSpecification.comFiltro(filtro));
        return pagamentoRepository.findAll(spec);
    }

    // =========================================================================
    // VALIDAÇÕES
    // =========================================================================
    private void validarNovoPagamento(PagamentoDTO dto) {
        log.info("[Pagamento] Validando novo pagamento…");

        if (pagamentoRepository.findByCodigoDebito(dto.codigoDebito()).isPresent()) {
            throw new EntityExistsException("Já existe um pagamento com o código de débito informado.");
        }

        if (!IsCpfCnpj.isCpfOrCnpj(dto.cpfCnpjPagador())) {
            throw new IllegalArgumentException("CPF/CNPJ inválido.");
        }

        boolean metodoEhCartao = MetodoPagamentoEnum.getMetodosCartao().contains(dto.metodoPagamento());
        boolean temNumeroCartao = StringUtils.hasText(dto.numeroCartao());

        if (metodoEhCartao && !temNumeroCartao) {
            throw new MetodoPagamentoException("Método de pagamento com cartão requer o número do cartão.");
        }
        if (!metodoEhCartao && temNumeroCartao) {
            throw new MetodoPagamentoException("O método de pagamento selecionado não requer número do cartão.");
        }
    }

    // -------------------------------------------------------------------------
    private String normalizarCpfCnpj(String valor) {
        if (!StringUtils.hasText(valor)) return null;
        return valor.trim();
    }

    private String definirCartao(MetodoPagamentoEnum metodo, String numeroCartao) {
        if (metodo == MetodoPagamentoEnum.CARTAO_CREDITO || metodo == MetodoPagamentoEnum.CARTAO_DEBITO) {
            if (!StringUtils.hasText(numeroCartao)) return null;
            String n = numeroCartao.trim();
            return (n.length() <= 4) ? n : n.substring(n.length() - 4); // guarda só os 4 últimos
        }
        return null;
    }
}