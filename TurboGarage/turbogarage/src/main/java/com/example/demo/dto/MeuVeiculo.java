package com.example.demo.dto;

import com.example.demo.model.OrdemServico;

import java.util.List;

/**
 * O que a área do cliente recebe: um veículo dele + as ordens de serviço desse veículo.
 */
public record MeuVeiculo(
        Long id,
        String placa,
        String modelo,
        Integer ano,
        String observacao,
        List<OrdemServico> ordens
) {
}
