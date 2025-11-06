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
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock
    PagamentoRepository pagamentoRepository;

    @InjectMocks
    PagamentoService service;

    private Pagamento savedFromArgumentWithId() {
        // utilzinho para when(repo.save(...)) -> devolver o mesmo objeto com ID setado
        return null;
    }

    @Test
    @DisplayName("realizar(): Cartão — guarda somente os 4 últimos dígitos, valor com 2 casas, status pendente e ativo=true")
    void realizar_cartao_mascarado() {
        // arrange
        PagamentoDTO dto = new PagamentoDTO(
                456,
                "52998224725", // CPF válido
                MetodoPagamentoEnum.CARTAO_CREDITO,
                new BigDecimal("500"),               // sem casas decimais
                "4111111111111111"
        );

        when(pagamentoRepository.findByCodigoDebito(456)).thenReturn(Optional.empty());
        when(pagamentoRepository.save(any(Pagamento.class)))
                .thenAnswer(inv -> {
                    Pagamento p = inv.getArgument(0);
                    p.setId(1L);
                    return p;
                });

        // act
        Pagamento salvo = service.realizar(dto);

        // assert
        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getCpfCnpjPagador()).isEqualTo("52998224725");
        assertThat(salvo.getNumeroCartao()).isEqualTo("1111"); // <<< máscara!
        assertThat(salvo.getValorPagamento()).isEqualByComparingTo("500.00"); // <<< 2 casas
        assertThat(salvo.getStatus()).isEqualTo(StatusPagamentoEnum.PENDENTE_PROCESSAMENTO);
        assertThat(salvo.getAtivo()).isTrue();

        verify(pagamentoRepository).findByCodigoDebito(456);
        verify(pagamentoRepository).save(any(Pagamento.class));
    }

    @Test
    @DisplayName("realizar(): Boleto/Pix — não guarda número de cartão")
    void realizar_boleto_sem_cartao() {
        PagamentoDTO dto = new PagamentoDTO(
                789,
                "52998224725",
                MetodoPagamentoEnum.BOLETO,
                new BigDecimal("100.10"),
                null
        );

        when(pagamentoRepository.findByCodigoDebito(789)).thenReturn(Optional.empty());
        when(pagamentoRepository.save(any(Pagamento.class)))
                .thenAnswer(inv -> {
                    Pagamento p = inv.getArgument(0);
                    p.setId(2L);
                    return p;
                });

        Pagamento salvo = service.realizar(dto);

        assertThat(salvo.getNumeroCartao()).isNull();
        assertThat(salvo.getValorPagamento()).isEqualByComparingTo("100.10");
        verify(pagamentoRepository).save(any(Pagamento.class));
    }

    @Test
    @DisplayName("realizar(): impede duplicidade de código de débito")
    void realizar_codigo_debito_duplicado() {
        PagamentoDTO dto = new PagamentoDTO(
                123, "52998224725", MetodoPagamentoEnum.PIX, new BigDecimal("10.00"), null
        );
        when(pagamentoRepository.findByCodigoDebito(123)).thenReturn(Optional.of(new Pagamento()));

        assertThatThrownBy(() -> service.realizar(dto))
                .isInstanceOf(EntityExistsException.class)
                .hasMessageContaining("Já existe um pagamento");
    }

    @Test
    @DisplayName("realizar(): CPF/CNPJ inválido lança IllegalArgumentException")
    void realizar_documento_invalido() {
        PagamentoDTO dto = new PagamentoDTO(
                999, "00000000000", MetodoPagamentoEnum.PIX, new BigDecimal("1.00"), null
        );
        when(pagamentoRepository.findByCodigoDebito(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.realizar(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CPF/CNPJ inválido");
    }

    @Test
    @DisplayName("realizar(): método cartão sem número => erro")
    void realizar_cartao_sem_numero() {
        PagamentoDTO dto = new PagamentoDTO(
                400, "52998224725", MetodoPagamentoEnum.CARTAO_DEBITO, new BigDecimal("5.00"), null
        );
        when(pagamentoRepository.findByCodigoDebito(400)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.realizar(dto))
                .isInstanceOf(MetodoPagamentoException.class)
                .hasMessageContaining("requer o número do cartão");
    }

    @Test
    @DisplayName("realizar(): método que não é cartão com número => erro")
    void realizar_nao_cartao_com_numero() {
        PagamentoDTO dto = new PagamentoDTO(
                401, "52998224725", MetodoPagamentoEnum.PIX, new BigDecimal("5.00"), "4111111111111111"
        );
        when(pagamentoRepository.findByCodigoDebito(401)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.realizar(dto))
                .isInstanceOf(MetodoPagamentoException.class)
                .hasMessageContaining("não requer número do cartão");
    }

    @Nested
    class AtualizarStatus {

        @Test
        @DisplayName("atualizar(): PENDENTE -> SUCESSO ok")
        void pendente_para_sucesso() {
            Pagamento p = new Pagamento();
            p.setId(1L);
            p.setAtivo(true);
            p.setStatus(StatusPagamentoEnum.PENDENTE_PROCESSAMENTO);

            when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(p));
            when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(inv -> inv.getArgument(0));

            Pagamento salvo = service.atualizar(new AtualizarPagamentoDTO(1L, StatusPagamentoEnum.PROCESSADO_SUCESSO));

            assertThat(salvo.getStatus()).isEqualTo(StatusPagamentoEnum.PROCESSADO_SUCESSO);
        }

        @Test
        @DisplayName("atualizar(): SUCESSO -> qualquer => erro")
        void sucesso_nao_pode_mudar() {
            Pagamento p = new Pagamento();
            p.setId(1L);
            p.setAtivo(true);
            p.setStatus(StatusPagamentoEnum.PROCESSADO_SUCESSO);

            when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(p));

            assertThatThrownBy(() -> service.atualizar(new AtualizarPagamentoDTO(1L, StatusPagamentoEnum.PENDENTE_PROCESSAMENTO)))
                    .isInstanceOf(StatusPagamentoException.class);
        }

        @Test
        @DisplayName("atualizar(): FALHA -> PENDENTE ok; FALHA -> outro => erro")
        void falha_para_pendente_ou_erro() {
            Pagamento p = new Pagamento();
            p.setId(1L);
            p.setAtivo(true);
            p.setStatus(StatusPagamentoEnum.PROCESSADO_FALHA);

            when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(p));
            when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(inv -> inv.getArgument(0));

            // ok
            Pagamento salvo = service.atualizar(new AtualizarPagamentoDTO(1L, StatusPagamentoEnum.PENDENTE_PROCESSAMENTO));
            assertThat(salvo.getStatus()).isEqualTo(StatusPagamentoEnum.PENDENTE_PROCESSAMENTO);

            // agora tenta outra transição inválida
            p.setStatus(StatusPagamentoEnum.PROCESSADO_FALHA);
            assertThatThrownBy(() -> service.atualizar(new AtualizarPagamentoDTO(1L, StatusPagamentoEnum.PROCESSADO_SUCESSO)))
                    .isInstanceOf(StatusPagamentoException.class);
        }

        @Test
        @DisplayName("atualizar(): pagamento inativo => erro")
        void atualizar_inativo() {
            Pagamento p = new Pagamento();
            p.setId(1L);
            p.setAtivo(false);
            p.setStatus(StatusPagamentoEnum.PENDENTE_PROCESSAMENTO);

            when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(p));

            assertThatThrownBy(() -> service.atualizar(new AtualizarPagamentoDTO(1L, StatusPagamentoEnum.PROCESSADO_SUCESSO)))
                    .isInstanceOf(StatusPagamentoException.class)
                    .hasMessageContaining("inativo");
        }

        @Test
        @DisplayName("atualizar(): id inexistente => 404")
        void atualizar_inexistente() {
            when(pagamentoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizar(new AtualizarPagamentoDTO(99L, StatusPagamentoEnum.PROCESSADO_SUCESSO)))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class ExclusaoLogica {

        @Test
        @DisplayName("excluir(): PENDENTE e ativo=true => marca inativo")
        void excluir_ok() {
            Pagamento p = new Pagamento();
            p.setId(10L);
            p.setAtivo(true);
            p.setStatus(StatusPagamentoEnum.PENDENTE_PROCESSAMENTO);

            when(pagamentoRepository.findByIdAndAtivoTrue(10L)).thenReturn(Optional.of(p));

            service.excluir(10L);

            assertThat(p.getAtivo()).isFalse();
            verify(pagamentoRepository).save(p);
        }

        @Test
        @DisplayName("excluir(): status != PENDENTE => erro")
        void excluir_status_invalido() {
            Pagamento p = new Pagamento();
            p.setId(11L);
            p.setAtivo(true);
            p.setStatus(StatusPagamentoEnum.PROCESSADO_SUCESSO);

            when(pagamentoRepository.findByIdAndAtivoTrue(11L)).thenReturn(Optional.of(p));

            assertThatThrownBy(() -> service.excluir(11L))
                    .isInstanceOf(StatusPagamentoException.class);
        }

        @Test
        @DisplayName("excluir(): não encontrado/ativo=false => 404")
        void excluir_inexistente() {
            when(pagamentoRepository.findByIdAndAtivoTrue(12L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.excluir(12L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Test
    @DisplayName("buscarPorId(): repassa para repository")
    void buscarPorId() {
        Pagamento p = new Pagamento();
        p.setId(1L);
        when(pagamentoRepository.findById(1L)).thenReturn(Optional.of(p));

        Optional<Pagamento> opt = service.buscarPorId(1L);

        assertThat(opt).isPresent();
        verify(pagamentoRepository).findById(1L);
    }

    @Test
    @DisplayName("listar(): chama repository.findAll(Specification)")
    void listar_com_filtro() {
        when(pagamentoRepository.findAll(any(Specification.class))).thenReturn(List.of());

        var filtro = new FiltroPagamentoDTO(null, null, null);
        List<Pagamento> lista = service.listar(filtro);

        assertThat(lista).isEmpty();
        verify(pagamentoRepository).findAll(any(Specification.class));
    }
}
