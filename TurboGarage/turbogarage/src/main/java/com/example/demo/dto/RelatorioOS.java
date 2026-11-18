package com.example.demo.dto;

import java.math.BigDecimal;

/**
 * Tudo que aparece no relatório de uma ordem de serviço CONCLUÍDA.
 */
public record RelatorioOS(
        Long numero,
        String status,
        String dataAbertura,
        String dataConclusao,
        String emitidoEm,
        String defeito,
        BigDecimal valor,
        String clienteNome,
        String clienteTelefone,
        String clienteEmail,
        String clienteEndereco,
        String veiculoPlaca,
        String veiculoModelo,
        Integer veiculoAno,
        String observacaoVeiculo
) {
}
