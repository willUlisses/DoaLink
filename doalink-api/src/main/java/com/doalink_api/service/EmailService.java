package com.doalink_api.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.recuperacao-senha.url-base}")
    private String urlBase;

    public void enviarCodigoVerificacao(String destinatario, String nome, String codigo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject("Código de confirmação — DoaLink");
            helper.setFrom("noreply@doalink.com.br");
            helper.setText(construirHtmlCodigo(nome, codigo), true);

            mailSender.send(message);
            log.info("E-mail de confirmação de cadastro enviado para {}", destinatario);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de confirmação para {}: {}", destinatario, e.getMessage(), e);
        }
    }

    private String construirHtmlCodigo(String nome, String codigo) {
        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                </head>
                <body style="margin:0;padding:0;background-color:#f8fafc;font-family:'Segoe UI',Arial,sans-serif;color:#1e293b;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f8fafc;padding:32px 16px;">
                    <tr>
                      <td align="center">
                        <table width="100%%" cellpadding="0" cellspacing="0"
                               style="background-color:#ffffff;border:1px solid #e2e8f0;border-radius:12px;
                                      max-width:520px;padding:36px;box-shadow:0 4px 12px rgba(0,0,0,0.05);">
                          <tr>
                            <td align="center" style="padding-bottom:20px;">
                              <h2 style="margin:0;color:#2563eb;font-size:24px;font-weight:700;">DoaLink</h2>
                              <p style="margin:6px 0 0;color:#64748b;font-size:14px;">Confirmação de e-mail</p>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:12px 0 20px;">
                              <p style="margin:0 0 12px;font-size:16px;color:#1e293b;">
                                Olá, <strong>%s</strong>!
                              </p>
                              <p style="margin:0 0 24px;font-size:14px;color:#475569;line-height:1.5;">
                                Seu código de confirmação para ativar sua conta no DoaLink é:
                              </p>
                              <!-- Code Display Box -->
                              <div style="text-align:center;margin:16px 0 24px;">
                                <div style="display:inline-block;background-color:#eff6ff;border:2px dashed #2563eb;
                                            border-radius:10px;padding:16px 36px;">
                                  <span style="font-size:34px;font-weight:800;letter-spacing:10px;color:#1d4ed8;font-family:monospace;">
                                    %s
                                  </span>
                                </div>
                              </div>
                              <!-- Warning -->
                              <div style="background-color:#fef3c7;border-left:4px solid #f59e0b;padding:12px 16px;border-radius:4px;margin-bottom:20px;">
                                <p style="margin:0;font-size:13px;color:#92400e;line-height:1.4;">
                                  <strong>Atenção:</strong> Este código expira em <strong>90 segundos</strong>.
                                </p>
                              </div>
                              <p style="margin:0;font-size:12px;color:#94a3b8;line-height:1.4;">
                                Se você não realizou este cadastro, desconsidere esta mensagem com segurança.
                              </p>
                            </td>
                          </tr>
                          <tr>
                            <td align="center" style="border-top:1px solid #f1f5f9;padding-top:20px;">
                              <p style="margin:0;font-size:12px;color:#94a3b8;">
                                © 2026 DoaLink. Mensagem automática gerada pelo sistema.
                              </p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(nome, codigo);
    }

    public void enviarRecuperacaoSenha(String destinatario, String nome, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject("Recuperação de senha — DoaLink");
            helper.setFrom("noreply@doalink.com.br");
            helper.setText(construirHtml(nome, token), true);

            mailSender.send(message);
            log.info("E-mail de recuperação enviado para {}", destinatario);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de recuperação para {}: {}", destinatario, e.getMessage());
        }
    }

    private String construirHtml(String nome, String token) {
        String link = urlBase + "/redefinir-senha?token=" + token;
        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                </head>
                <body style="margin:0;padding:0;background-color:#f5f5f5;font-family:'Segoe UI',Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f5f5f5;padding:40px 0;">
                    <tr>
                      <td align="center">
                        <table width="600" cellpadding="0" cellspacing="0"
                               style="background-color:#ffffff;border-radius:12px;overflow:hidden;
                                      box-shadow:0 4px 20px rgba(0,0,0,0.08);max-width:600px;">
                          <!-- Header -->
                          <tr>
                            <td style="background:linear-gradient(135deg,#2563eb,#1d4ed8);padding:40px 48px;text-align:center;">
                              <h1 style="margin:0;color:#ffffff;font-size:28px;font-weight:700;letter-spacing:-0.5px;">
                                Recuperação de Senha
                              </h1>
                              <p style="margin:8px 0 0;color:#bfdbfe;font-size:14px;">DoaLink — Conectando quem doa com quem precisa</p>
                            </td>
                          </tr>
                          <!-- Body -->
                          <tr>
                            <td style="padding:48px 48px 32px;">
                              <p style="margin:0 0 16px;font-size:18px;color:#1e293b;font-weight:600;">
                                Olá, %s! 
                              </p>
                              <p style="margin:0 0 24px;font-size:15px;color:#475569;line-height:1.6;">
                                Recebemos uma solicitação para redefinir a senha da sua conta no DoaLink.
                                Clique no botão abaixo para criar uma nova senha:
                              </p>
                              <!-- Button -->
                              <table width="100%%" cellpadding="0" cellspacing="0">
                                <tr>
                                  <td align="center" style="padding:8px 0 32px;">
                                    <a href="%s"
                                       style="display:inline-block;background:linear-gradient(135deg,#2563eb,#1d4ed8);
                                              color:#ffffff;text-decoration:none;font-size:16px;font-weight:600;
                                              padding:16px 48px;border-radius:8px;letter-spacing:0.3px;">
                                      Redefinir minha senha
                                    </a>
                                  </td>
                                </tr>
                              </table>
                              <!-- Warning -->
                              <div style="background-color:#fef3c7;border:1px solid #fcd34d;border-radius:8px;padding:16px 20px;margin-bottom:24px;">
                                <p style="margin:0;font-size:14px;color:#92400e;">
                                  <strong>Este link expira em 10 minutos.</strong>
                                  Após esse prazo, você precisará solicitar um novo link de recuperação.
                                </p>
                              </div>
                              <p style="margin:0 0 8px;font-size:14px;color:#64748b;line-height:1.6;">
                                Se você não solicitou a recuperação de senha, ignore este e-mail com segurança.
                                Sua senha permanecerá a mesma.
                              </p>
                              <p style="margin:0;font-size:13px;color:#94a3b8;">
                                Caso o botão não funcione, copie e cole o link abaixo no seu navegador:
                              </p>
                              <p style="margin:8px 0 0;font-size:12px;color:#2563eb;word-break:break-all;">
                                %s
                              </p>
                            </td>
                          </tr>
                          <!-- Footer -->
                          <tr>
                            <td style="background-color:#f8fafc;border-top:1px solid #e2e8f0;padding:24px 48px;text-align:center;">
                              <p style="margin:0;font-size:13px;color:#94a3b8;">
                                © 2026 DoaLink. Este é um e-mail automático — não responda a esta mensagem.
                              </p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(nome, link, link);
    }
}
