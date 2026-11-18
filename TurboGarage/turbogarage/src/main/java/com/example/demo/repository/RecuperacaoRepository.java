package com.example.demo.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Optional;

/**
 * REPOSITORY: tabela "recuperacao_senha" (códigos de recuperação).
 */
@Repository
public class RecuperacaoRepository {

    /** Um código pendente: id, hash do código, validade e quantas vezes erraram. */
    public record CodigoPendente(Long id, String codigoHash, Timestamp expiraEm, int tentativas) {
    }

    private final JdbcTemplate jdbc;

    public RecuperacaoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Ao pedir um código novo, os anteriores daquele email deixam de valer
    public void invalidarAnteriores(String email) {
        jdbc.update("UPDATE recuperacao_senha SET usado = 1 WHERE email = ? AND usado = 0", email);
    }

    public void inserir(String email, String codigoHash, Timestamp expiraEm) {
        jdbc.update("INSERT INTO recuperacao_senha (email, codigo_hash, expira_em, tentativas, usado, criado_em) VALUES (?, ?, ?, 0, 0, ?)",
                email, codigoHash, expiraEm, new Timestamp(System.currentTimeMillis()));
    }

    // O código mais recente ainda não usado daquele email
    public Optional<CodigoPendente> ultimoPendente(String email) {
        return jdbc.query(
                "SELECT id, codigo_hash, expira_em, tentativas FROM recuperacao_senha WHERE email = ? AND usado = 0 ORDER BY id DESC LIMIT 1",
                (rs, linha) -> new CodigoPendente(rs.getLong("id"), rs.getString("codigo_hash"),
                        rs.getTimestamp("expira_em"), rs.getInt("tentativas")),
                email).stream().findFirst();
    }

    public void somarTentativa(Long id) {
        jdbc.update("UPDATE recuperacao_senha SET tentativas = tentativas + 1 WHERE id = ?", id);
    }

    public void marcarUsado(Long id) {
        jdbc.update("UPDATE recuperacao_senha SET usado = 1 WHERE id = ?", id);
    }
}
