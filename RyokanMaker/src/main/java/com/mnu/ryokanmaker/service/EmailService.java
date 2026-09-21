package com.mnu.ryokanmaker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailParseException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    // spring.mail.host가 설정되어 있지 않으면 빈이 생성되지 않으므로
    // required=false로 두어 메일 설정 전에도 앱이 정상 기동되게 함
    @Autowired(required = false)
    private JavaMailSender emailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Value("${admin.notify.email:}")
    private String adminNotifyEmail;

    /** 관리자 계정 신청이 새로 들어왔을 때 운영자에게 알림 발송 */
    public void sendAdminRequestNotification(String ryokanName, String applicantName, String applicantEmail,
            String applicantTel, String requestMessage) {
        if (adminNotifyEmail == null || adminNotifyEmail.isBlank()) {
            return;
        }
        String subject = "[清流庵 플랫폼] 새 관리자 계정 신청이 접수되었습니다";
        String text = "새로운 관리자 계정 신청이 접수되었습니다.\n\n"
                + "료칸(업체) 이름 : " + ryokanName + "\n"
                + "담당자 이름 : " + applicantName + "\n"
                + "이메일 : " + applicantEmail + "\n"
                + "연락처 : " + (applicantTel != null && !applicantTel.isBlank() ? applicantTel : "-") + "\n"
                + "신청 내용 : " + (requestMessage != null && !requestMessage.isBlank() ? requestMessage : "-") + "\n\n"
                + "관리자 페이지의 '계정 신청 관리'에서 승인/반려할 수 있습니다.";
        send(adminNotifyEmail, subject, text);
    }

    /** 관리자 계정 승인 시 아이디/임시 비밀번호 발송 */
    public void sendAdminCredentials(String toEmail, String ryokanName, String adminId, String tempPassword) {
        String subject = "[清流庵 플랫폼] 관리자 계정이 발급되었습니다";
        String text = ryokanName + " 담당자님, 관리자 계정 신청이 승인되었습니다.\n\n"
                + "아이디 : " + adminId + "\n"
                + "임시 비밀번호 : " + tempPassword + "\n\n"
                + "로그인 후 반드시 비밀번호를 변경해주세요.";
        send(toEmail, subject, text);
    }

    /** 관리자 계정 신청이 반려되었을 때 발송 */
    public void sendAdminRequestRejected(String toEmail, String ryokanName) {
        String subject = "[清流庵 플랫폼] 관리자 계정 신청 결과 안내";
        String text = ryokanName + " 담당자님, 죄송합니다. 관리자 계정 신청이 반려되었습니다.\n"
                + "문의 사항은 답장으로 남겨주세요.";
        send(toEmail, subject, text);
    }

    private void send(String to, String subject, String text) {
        if (emailSender == null) {
            throw new IllegalStateException("spring.mail.* 설정이 되어있지 않습니다. application.properties를 확인하세요.");
        }
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setTo(to);
            if (mailFrom != null && !mailFrom.isBlank()) {
                helper.setFrom(mailFrom);
            }
            helper.setSubject(subject);
            helper.setText(text, false);
            emailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new MailParseException(e);
        }
    }
}
