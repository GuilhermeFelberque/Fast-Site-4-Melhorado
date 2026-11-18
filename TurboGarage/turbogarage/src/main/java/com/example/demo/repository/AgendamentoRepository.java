package com.example.demo.repository;

import com.example.demo.model.Agendamento;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY: tabela "agendamentos" (Spring JDBC).
 * Os JOINs trazem a placa do carro e o nome/telefone do cliente para o mecânico.
 */
@Repository
public class AgendamentoRepository {

    private static final String SELECT_BASE =
            "SELECT a.id, a.veiculo_id, a.data_hora, a.motivo, a.status, a.resposta, a.data_pedido, "
                    + "v.placa, v.modelo, c.nome AS cliente_nome, c.telefone AS cliente_telefone "
                    + "FROM agendamentos a "
                    + "JOIN veiculos v ON v.id = a.veiculo_id "
                    + "LEFT JOIN cadastros c ON c.id = v.cadastro_id ";

    private final JdbcTemplate jdbc;

    public AgendamentoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static String formatar(Timestamp t, String padrao) {
        return t == null ? "" : new SimpleDateFormat(padrao).format(t);
    }

    private final RowMapper<Agendamento> mapper = (rs, linha) -> {
        Agendamento a = new Agendamento();
        a.setId(rs.getLong("id"));
        a.setVeiculoId(rs.getLong("veiculo_id"));
        a.setDataHora(formatar(rs.getTimestamp("data_hora"), "dd/MM/yyyy HH:mm"));
        a.setMotivo(rs.getString("motivo"));
        a.setStatus(rs.getString("status"));
        a.setResposta(rs.getString("resposta"));
        a.setDataPedido(formatar(rs.getTimestamp("data_pedido"), "dd/MM/yyyy HH:mm"));
        a.setVeiculoPlaca(rs.getString("placa"));
        a.setVeiculoModelo(rs.getString("modelo"));
        a.setClienteNome(rs.getString("cliente_nome"));
        a.setClienteTelefone(rs.getString("cliente_telefone"));
        return a;
    };

    // Todos (para o mecânico): os próximos primeiro
    public List<Agendamento> findAll() {
        return jdbc.query(SELECT_BASE + "ORDER BY a.data_hora", mapper);
    }

    // Só os do cliente
    public List<Agendamento> findByClienteId(Long clienteId) {
        return jdbc.query(SELECT_BASE + "WHERE v.cadastro_id = ? ORDER BY a.data_hora DESC", mapper, clienteId);
    }

    public Optional<Agendamento> findById(Long id) {
        return jdbc.query(SELECT_BASE + "WHERE a.id = ?", mapper, id).stream().findFirst();
    }

    // Já existe agendamento pendente ou confirmado nesse horário? (idIgnorado = não conta ele mesmo)
    public boolean horarioOcupado(Timestamp dataHora, Long idIgnorado) {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM agendamentos WHERE data_hora = ? AND status IN ('PENDENTE', 'CONFIRMADO') AND id <> ?",
                Integer.class, dataHora, idIgnorado == null ? 0L : idIgnorado);
        return total != null && total > 0;
    }

    // Já existe OUTRO agendamento CONFIRMADO nesse horário? (usado quando o mecânico confirma)
    public boolean horarioOcupadoConfirmado(Timestamp dataHora, Long idIgnorado) {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM agendamentos WHERE data_hora = ? AND status = 'CONFIRMADO' AND id <> ?",
                Integer.class, dataHora, idIgnorado);
        return total != null && total > 0;
    }

    // Quantos agendamentos usam este veículo (para não excluir o veículo)
    public int contarPorVeiculo(Long veiculoId) {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM agendamentos WHERE veiculo_id = ?", Integer.class, veiculoId);
        return total == null ? 0 : total;
    }

    // CREATE: sempre começa PENDENTE
    public Long insert(Long veiculoId, Timestamp dataHora, String motivo) {
        Timestamp agora = new Timestamp(System.currentTimeMillis() / 1000 * 1000);
        KeyHolder chave = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO agendamentos (veiculo_id, data_hora, motivo, status, data_pedido) VALUES (?, ?, ?, 'PENDENTE', ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, veiculoId);
            ps.setTimestamp(2, dataHora);
            ps.setString(3, motivo);
            ps.setTimestamp(4, agora);
            return ps;
        }, chave);
        return chave.getKey().longValue();
    }

    // Validação do mecânico
    public void atualizarStatus(Long id, String status, String resposta) {
        jdbc.update("UPDATE agendamentos SET status = ?, resposta = ? WHERE id = ?", status, resposta, id);
    }

    // Data e hora "cruas" do agendamento (para conferir conflito na confirmação)
    public Timestamp dataHoraDe(Long id) {
        return jdbc.queryForObject("SELECT data_hora FROM agendamentos WHERE id = ?", Timestamp.class, id);
    }
}
