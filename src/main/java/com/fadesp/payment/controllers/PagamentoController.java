package com.fadesp.payment.controllers;

import com.fadesp.payment.domain.dtos.AtualizarPagamentoDTO;
import com.fadesp.payment.domain.dtos.FiltroPagamentoDTO;
import com.fadesp.payment.domain.dtos.PagamentoDTO;
import com.fadesp.payment.domain.entities.Pagamento;
import com.fadesp.payment.services.PagamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pagamentos")
@Tag(name = "Pagamentos", description = "API para realizar, consultar, atualizar status e excluir logicamente pagamentos")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @PostMapping
    @Operation(summary = "Criar/receber pagamento")
    @ApiResponse(responseCode = "201", description = "Pagamento criado")
    public ResponseEntity<Pagamento> criar(@RequestBody @Valid PagamentoDTO dto) {
        Pagamento pagamento = pagamentoService.realizar(dto);
        addLinks(pagamento);
        return ResponseEntity.created(URI.create("/pagamentos/" + pagamento.getId())).body(pagamento);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Atualizar status de pagamento")
    @ApiResponse(responseCode = "200", description = "Status atualizado")
    public ResponseEntity<Pagamento> atualizarStatus(@PathVariable Long id,
                                                     @RequestBody @Valid AtualizarPagamentoDTO dto) {
        Pagamento pagamento = pagamentoService.atualizar(new AtualizarPagamentoDTO(id, dto.status()));
        addLinks(pagamento);
        return ResponseEntity.ok(pagamento);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclusão lógica (apenas se PENDENTE_PROCESSAMENTO)")
    @ApiResponse(responseCode = "204", description = "Excluído logicamente")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        pagamentoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Listar/filtrar pagamentos")
    public ResponseEntity<List<Pagamento>> listar(@ParameterObject @ModelAttribute FiltroPagamentoDTO filtro) {
        List<Pagamento> pagamentos = pagamentoService.listar(filtro)
                .stream()
                .peek(this::addLinks)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pagamentos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por id")
    public ResponseEntity<Pagamento> buscar(@PathVariable Long id) {
        Pagamento pagamento = pagamentoService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pagamento não encontrado"));
        addLinks(pagamento);
        return ResponseEntity.ok(pagamento);
    }

    // ---------- HATEOAS ----------
    private void addLinks(Pagamento pagamento) {
        pagamento.add(
                linkTo(methodOn(PagamentoController.class).buscar(pagamento.getId()))
                        .withSelfRel().withType("GET"));
        pagamento.add(
                linkTo(methodOn(PagamentoController.class).listar(new FiltroPagamentoDTO(null, null, null)))
                        .withRel("listar").withType("GET"));
        // corpo pode ser null — HATEOAS só usa a assinatura
        pagamento.add(
                linkTo(methodOn(PagamentoController.class).atualizarStatus(pagamento.getId(), null))
                        .withRel("atualizar-status").withType("PUT"));
        pagamento.add(
                linkTo(methodOn(PagamentoController.class).excluir(pagamento.getId()))
                        .withRel("excluir").withType("DELETE"));
    }
}