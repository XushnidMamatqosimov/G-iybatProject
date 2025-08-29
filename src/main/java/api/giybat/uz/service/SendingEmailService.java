package api.giybat.uz.service;

import api.giybat.uz.enums.AppLanguages;
import api.giybat.uz.enums.SmsType;
import api.giybat.uz.exp.AppBadException;
import api.giybat.uz.util.JwtUtil;
import api.giybat.uz.util.RandomUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class SendingEmailService {
    @Value("${spring.mail.username}")
    private String fromAccount;
    @Value("${server.domain}")
    private String serverDomain;
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private EmailHistoryService emailHistoryService;
    private final Integer emailLimitCount = 3;

    public void sendRegistrationEmail(String email, Integer profileId, AppLanguages lang) {
        String subject = "Complete Registration";
        String body =
                "<!DOCTYPE html>\n" +
                        "<html lang=\"en\">\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\">\n" +
                        "    <title>Title</title>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "\n" +
                        "<h1>Complete Registration</h1>\n" +
                        "<p>Salom Yaxshimisiz</p>\n" +
                        "<p>\n" +
                        "    Please click to button to complete the registration\n" +
                        "    <a\n" +
                        "            style=\"padding: 10px 30px;\n" +
                        "       display: inline-block;\n" +
                        "       text-decoration: none;\n" +
                        "       color: white;\n" +
                        "       border-radius: 4px;\n" +
                        "       background-color: indianred;\"\n" +
                        "             href=\"%s/auth/registration/email-verification/%s?lang=%s\" ,target=\"_blank\">Click Here</a>\n" +
                        "</p>\n" +
                        "</body>\n" +
                        "</html>";
        body = String.format(body, serverDomain, JwtUtil.encodeForEmailVerf(profileId), lang.name());
        System.out.println(JwtUtil.encodeForEmailVerf(profileId));
        sendEmail(email, subject, body);
    }

    public void sendResetPasswordEmail(@NotBlank(message = "Username required") String email, AppLanguages lang) {
        String subject = "Reset Password Confirmation Code";
        String randomSMSCode = RandomUtil.getRandomSMSCode();
        String body = "Hello, This is your reset password confirmation code! "+randomSMSCode;
        checkAndSendMimeEmail(email, subject, body, randomSMSCode);
    }

    public void sendChangeUsernameEmail(String email, AppLanguages lang) {
        String subject = "Change Username";
        String randomSMSCode = RandomUtil.getRandomSMSCode();
        String body = "Hello, This is your change username confirmation code! "+randomSMSCode;
        checkAndSendMimeEmail(email, subject, body, randomSMSCode);
    }

    public void checkAndSendMimeEmail(String email, String subject, String body, String randomSMSCode) {
        //check
        Long count = emailHistoryService.smsCountByEmail(email);
        if (count >= emailLimitCount) {
            System.out.println("Email limit reached" + email);
            throw new AppBadException("Email limit reached");

        }
        // db-save
        emailHistoryService.saveSmsToDB(email, randomSMSCode, SmsType.RESET_PASSWORD);
        //send
        sendEmail(email, subject, body);
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            mimeMessage.setFrom(fromAccount);

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            CompletableFuture.runAsync(() -> {
                javaMailSender.send(mimeMessage);
            });
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }
}
