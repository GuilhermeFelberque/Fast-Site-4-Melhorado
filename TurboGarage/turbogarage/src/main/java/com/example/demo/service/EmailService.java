package com.example.demo.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * SERVICE: envia o código de recuperação.
 *  - Se o email estiver configurado no application.properties (spring.mail.*), manda email de verdade.
 *  - Se não estiver, mostra o código no CONSOLE (modo de teste, bom para a apresentação).
 */
@Service
public class EmailService {

    private final ObjectProvider<JavaMailSender> mailSender;

    @Value("${spring.mail.username:}")
    private String remetente;

    // ObjectProvider: o JavaMailSender só existe quando spring.mail.host está configurado
    public EmailService(ObjectProvider<JavaMailSender> mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCodigo(String para, String codigo, int minutos) {
        String texto = "Olá!\n\nSeu código para criar uma nova senha na Turbo Garage é: " + codigo
                + "\n\nEle vale por " + minutos + " minutos e só pode ser usado uma vez."
                + "\nSe você não pediu, ignore este email: sua senha continua a mesma.\n\nTurbo Garage";

        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            mostrarNoConsole(para, codigo, minutos);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(remetente);
            msg.setTo(para);
            msg.setSubject("Turbo Garage - código para recuperar sua senha");
            msg.setText(texto);
            sender.send(msg);
            System.out.println(">>> Código de recuperação enviado por email para " + para);
        } catch (Exception e) {
            // Email mal configurado: não trava o sistema, mostra no console
            System.out.println(">>> Falha ao enviar email (" + e.getMessage() + ")");
            mostrarNoConsole(para, codigo, minutos);
        }
    }

    private void mostrarNoConsole(String para, String codigo, int minutos) {
        System.out.println("==============================================");
        System.out.println(" RECUPERAÇÃO DE SENHA (modo de teste, sem email)");
        System.out.println(" Para: " + para);
        System.out.println(" Código: " + codigo + "  (vale " + minutos + " minutos)");
        System.out.println("==============================================");
    }
}
