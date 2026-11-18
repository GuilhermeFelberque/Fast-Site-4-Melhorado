package com.example.demo.model;

/**
 * MODEL: agendamento (tabela "agendamentos").
 * O cliente pede um dia e horário para levar o carro; o mecânico (admin) confirma ou recusa.
 */
public class Agendamento {

    // Situações possíveis
    public static final String PENDENTE = "PENDENTE";      // esperando o mecânico
    public static final String CONFIRMADO = "CONFIRMADO";
    public static final String RECUSADO = "RECUSADO";

    private Long id;
    private Long veiculoId;
    private String dataHora;     // texto pronto para a tela: dd/MM/yyyy HH:mm
    private String motivo;       // opcional, escrito pelo cliente
    private String status;
    private String resposta;     // opcional, escrita pelo mecânico
    private String dataPedido;

    // Vêm do JOIN com "veiculos" e "cadastros" (só leitura)
    private String veiculoPlaca;
    private String veiculoModelo;
    private String clienteNome;
    private String clienteTelefone;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVeiculoId() { return veiculoId; }
    public void setVeiculoId(Long veiculoId) { this.veiculoId = veiculoId; }

    public String getDataHora() { return dataHora; }
    public void setDataHora(String dataHora) { this.dataHora = dataHora; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResposta() { return resposta; }
    public void setResposta(String resposta) { this.resposta = resposta; }

    public String getDataPedido() { return dataPedido; }
    public void setDataPedido(String dataPedido) { this.dataPedido = dataPedido; }

    public String getVeiculoPlaca() { return veiculoPlaca; }
    public void setVeiculoPlaca(String veiculoPlaca) { this.veiculoPlaca = veiculoPlaca; }

    public String getVeiculoModelo() { return veiculoModelo; }
    public void setVeiculoModelo(String veiculoModelo) { this.veiculoModelo = veiculoModelo; }

    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }

    public String getClienteTelefone() { return clienteTelefone; }
    public void setClienteTelefone(String clienteTelefone) { this.clienteTelefone = clienteTelefone; }
}
