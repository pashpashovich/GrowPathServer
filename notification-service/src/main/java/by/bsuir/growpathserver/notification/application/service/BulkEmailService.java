package by.bsuir.growpathserver.notification.application.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import by.bsuir.growpathserver.notification.application.exception.EmailDeliveryException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkEmailService {

    private final JavaMailSender mailSender;

    @Value("${mailersend.from-email}")
    private String fromEmail;

    public void sendHtmlEmail(String to, String recipientName, String subject, String htmlBody) {
        sendHtmlEmail(to, recipientName, subject, htmlBody, List.of());
    }

    public void sendHtmlEmail(String to,
                              String recipientName,
                              String subject,
                              String htmlBody,
                              List<EmailAttachment> attachments) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            boolean hasAttachments = attachments != null && !attachments.isEmpty();
            MimeMessageHelper helper = new MimeMessageHelper(message, hasAttachments, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            if (hasAttachments) {
                for (EmailAttachment attachment : attachments) {
                    helper.addAttachment(
                            attachment.fileName(),
                            new ByteArrayResource(attachment.content()),
                            attachment.contentType()
                    );
                }
            }

            mailSender.send(message);
            log.info("Mailing email sent to {}", to);
        }
        catch (MessagingException e) {
            log.error("Failed to send mailing email to {}", to, e);
            throw new EmailDeliveryException(to, e);
        }
    }

    public record EmailAttachment(String fileName, byte[] content, String contentType) {
    }
}
