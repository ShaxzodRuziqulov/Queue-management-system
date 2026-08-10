package com.example.queuemanagementsystem.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetCode(String email, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Parolni tiklash kodi");
            helper.setText(buildPasswordResetHtml(code), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Email xabarini tayyorlashda xatolik yuz berdi", e);
        }
    }

    private String buildPasswordResetHtml(String code) {
        return """
                <!doctype html>
                <html lang="uz">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Parolni tiklash</title>
                </head>
                <body style="margin:0;padding:0;background:#f4f7fb;font-family:Arial,sans-serif;color:#172033;">
                  <div style="max-width:560px;margin:0 auto;padding:32px 16px;">
                    <div style="background:#ffffff;border:1px solid #e6ebf2;border-radius:14px;overflow:hidden;">
                      <div style="background:#4f46e5;padding:22px 28px;color:#ffffff;">
                        <div style="font-size:20px;font-weight:700;">Navbat</div>
                        <div style="font-size:14px;opacity:.9;margin-top:4px;">Parolni tiklash so'rovi</div>
                      </div>

                      <div style="padding:28px;">
                        <h1 style="margin:0 0 12px;font-size:22px;line-height:1.3;color:#111827;">
                          Tasdiqlash kodi
                        </h1>
                        <p style="margin:0 0 20px;font-size:15px;line-height:1.6;color:#4b5563;">
                          Parolni almashtirish uchun quyidagi 6 xonali kodni kiriting.
                        </p>

                        <div style="background:#f8fafc;border:1px dashed #cbd5e1;border-radius:12px;padding:18px;text-align:center;margin:24px 0;">
                          <div style="font-size:34px;line-height:1;letter-spacing:8px;font-weight:700;color:#111827;">
                            %s
                          </div>
                        </div>

                        <p style="margin:0 0 8px;font-size:14px;line-height:1.6;color:#4b5563;">
                          Kod 10 daqiqa amal qiladi.
                        </p>
                        <p style="margin:0;font-size:14px;line-height:1.6;color:#6b7280;">
                          Agar bu so'rovni siz yubormagan bo'lsangiz, xabarni e'tiborsiz qoldiring.
                        </p>
                      </div>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(code);
    }
}
