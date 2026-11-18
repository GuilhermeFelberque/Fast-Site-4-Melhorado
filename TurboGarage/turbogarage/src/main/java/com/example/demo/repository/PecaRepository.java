package com.example.demo.repository;

import com.example.demo.model.Peca;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY: tabela "pecas" (Spring JDBC).
 */
@Repository
public class PecaRepository {

    private static final String SELECT_BASE =
            "SELECT id, codigo, nome, categoria, quantidade, estoque_minimo, preco FROM pecas ";

    private final JdbcTemplate jdbc;

    public PecaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Peca> mapper = (rs, linha) -> {
        Peca p = new Peca();
        p.setId(rs.getLong("id"));
        p.setCodigo(rs.getString("codigo"));
        p.setNome(rs.getString("nome"));
        p.setCategoria(rs.getString("categoria"));
        p.setQuantidade(rs.getInt("quantidade"));
        p.setEstoqueMinimo(rs.getInt("estoque_minimo"));
        p.setPreco(rs.getBigDecimal("preco"));
        return p;
    };

    // READ: todas, em ordem alfabética
    public List<Peca> findAll() {
        return jdbc.query(SELECT_BASE + "ORDER BY nome", mapper);
    }

    public Optional<Peca> findById(Long id) {
        return jdbc.query(SELECT_BASE + "WHERE id = ?", mapper, id).stream().findFirst();
    }

    // Código repetido? (idIgnorado: na edição, não conta a própria peça)
    public boolean existsCodigo(String codigo, Long idIgnorado) {
        Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM pecas WHERE codigo = ? AND id <> ?",
                Integer.class, codigo, idIgnorado == null ? 0L : idIgnorado);
        return total != null && total > 0;
    }

    // CREATE
    public Long insert(Peca p) {
        KeyHolder chave = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO pecas (codigo, nome, categoria, quantidade, estoque_minimo, preco) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNome());
            ps.setString(3, p.getCategoria());
            ps.setInt(4, p.getQuantidade());
            ps.setInt(5, p.getEstoqueMinimo());
            ps.setBigDecimal(6, p.getPreco());
            return ps;
        }, chave);
        return chave.getKey().longValue();
    }

    // UPDATE
    public void update(Long id, Peca p) {
        jdbc.update("UPDATE pecas SET codigo = ?, nome = ?, categoria = ?, quantidade = ?, estoque_minimo = ?, preco = ? WHERE id = ?",
                p.getCodigo(), p.getNome(), p.getCategoria(), p.getQuantidade(), p.getEstoqueMinimo(), p.getPreco(), id);
    }

    // Entrada (+) ou saída (-). O "quantidade + ? >= 0" impede o estoque de ficar negativo.
    // Devolve quantas linhas mudaram: 0 = não tinha peça suficiente.
    public int movimentar(Long id, int diferenca) {
        return jdbc.update("UPDATE pecas SET quantidade = quantidade + ? WHERE id = ? AND quantidade + ? >= 0",
                diferenca, id, diferenca);
    }

    // DELETE
    public void delete(Long id) {
        jdbc.update("DELETE FROM pecas WHERE id = ?", id);
    }
}
