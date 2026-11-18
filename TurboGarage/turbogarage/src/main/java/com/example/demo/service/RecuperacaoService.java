package com.example.demo.service;

import com.example.demo.dto.NovaSenhaRequest;
import com.example.demo.dto.RecuperarEmailRequest;
import com.example.demo.model.Cadastro;
import com.example.demo.repository.CadastroRepository;
import com.example.demo.repository.RecuperacaoRepository;
import com.example.demo.repository.RecuperacaoRepository.CodigoPendente;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.util.SenhaUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.List;

/**
 * SERVICE: recuperação de senha e de email.
 *
 * Senha (2 passos):
 *  1) pedirCodigo: gera um código de 6 dígitos, guarda só o HASH (BCrypt), vale 15 min, e envia.
 *  2) trocarSenha: confere o código; certo -> grava a senha nova (BCrypt) e o código não vale mais.
 *     5 erros bloqueiam o código.
 *
 * Email: telefone + CEP do cadastro -> mostra o email com parte escondida (jo***@gmail.com).
 */
@Service
public class RecuperacaoService {

    private static final int MINUTOS_VALIDADE = 15;
    private static final int MAX_TENTATIVAS = 5;
    private static final SecureRandom SORTEIO = new SecureRandom();   // aleatório seguro (não dá para prever)

    private final UsuarioRepository usuarioRepository;
    private final CadastroRepository cadastroRepository;
    private final RecuperacaoRepository recuperacaoRepository;
    private final EmailService emailService;

    public RecuperacaoService(UsuarioRepository usuarioRepository, CadastroRepository cadastroRepository,
                              RecuperacaoRepository recuperacaoRepository, EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.cadastroRepository = cadastroRepository;
        this.recuperacaoRepository = recuperacaoRepository;
        this.emailService = emailService;
    }

    // Passo 1. Não avisa se o email existe ou não (para ninguém descobrir quem é cliente).
    public void pedirCodigo(String emailDigitado) {
        String email = emailDigitado.trim().toLowerCase();
        if (!usuarioRepository.existsByEmail(email)) {
            return;   // mesma resposta para quem não existe
        }
        String codigo = String.format("%06d", SORTEIO.nextInt(1_000_000));
        Timestamp expira = new Timestamp(System.currentTimeMillis() + MINUTOS_VALIDADE * 60_000L);

        recuperacaoRepository.invalidarAnteriores(email);
        recuperacaoRepository.inserir(email, SenhaUtil.criptografar(codigo), expira);
        emailService.enviarCodigo(email, codigo, MINUTOS_VALIDADE);
    }

    // Passo 2
    @Transactional
    public void trocarSenha(NovaSenhaRequest dados) {
        String email = dados.email().trim().toLowerCase();
        CodigoPendente pendente = recuperacaoRepository.ultimoPendente(email)
                .orElseThrow(() -> invalido("Código inválido ou expirado. Peça um novo código."));

        if (pendente.expiraEm().getTime() < System.currentTimeMillis()) {
            recuperacaoRepository.marcarUsado(pendente.id());
            throw invalido("Código expirado. Peça um novo código.");
        }
        if (pendente.tentativas() >= MAX_TENTATIVAS) {
            recuperacaoRepository.marcarUsado(pendente.id());
            throw invalido("Muitas tentativas erradas. Peça um novo código.");
        }
        if (!SenhaUtil.conferir(dados.codigo(), pendente.codigoHash())) {
            recuperacaoRepository.somarTentativa(pendente.id());
            int restam = MAX_TENTATIVAS - pendente.tentativas() - 1;
            throw invalido("Código errado. " + (restam > 0 ? "Restam " + restam + " tentativa(s)." : "Peça um novo código."));
        }

        usuarioRepository.atualizarSenha(email, SenhaUtil.criptografar(dados.novaSenha()));
        recuperacaoRepository.marcarUsado(pendente.id());
    }

    // Recuperar email: telefone + CEP
    public String recuperarEmail(RecuperarEmailRequest dados) {
        String cep = formatarCep(dados.cep());
        String telefone = soNumeros(dados.telefone());
        List<Cadastro> candidatos = cadastroRepository.findAtivosByCep(cep);
        return candidatos.stream()
                .filter(c -> soNumeros(c.getTelefone()).equals(telefone) && c.getEmail() != null)
                .map(c -> esconder(c.getEmail()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Não encontramos um cadastro com esse telefone e CEP"));
    }

    // joao.silva@gmail.com -> jo********@gmail.com
    static String esconder(String email) {
        int arroba = email.indexOf('@');
        if (arroba <= 0) return "***";
        String nome = email.substring(0, arroba);
        int visiveis = Math.min(2, nome.length());
        return nome.substring(0, visiveis) + "*".repeat(Math.max(3, nome.length() - visiveis)) + email.substring(arroba);
    }

    private static String soNumeros(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }

    private static String formatarCep(String cep) {
        String d = soNumeros(cep);
        return d.substring(0, 5) + "-" + d.substring(5);
    }

    private static ResponseStatusException invalido(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }
}
