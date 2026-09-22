package com.mnu.ryokanmaker.service;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

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

    // ---------------------------------------------------------------
    // 회원(고객) 대상 메일. 이미 가입/답변/결제가 DB에 반영된 뒤에 보내는 알림이므로
    // 발송에 실패해도 본 업무가 실패하지 않도록 예외는 로그만 남기고 삼킨다.
    // ---------------------------------------------------------------

    /** 회원가입 완료 안내 */
    public void sendSignupComplete(String toEmail, String nickname) {
        String subject = "[清流庵] 회원가입이 완료되었습니다";
        String text = nickname + "님, 가입해 주셔서 감사합니다.\n\n"
                + "회원가입이 정상적으로 완료되었습니다. 로그인 후 객실 예약과 1:1 문의를 이용하실 수 있습니다.";
        sendQuietly(toEmail, subject, text);
    }

    /** 1:1 문의에 관리자 답변이 등록되었을 때 안내 */
    public void sendInquiryAnswered(String toEmail, String inquiryTitle, String answerContent) {
        String subject = "[清流庵] 문의하신 내용에 답변이 등록되었습니다";
        String text = "문의하신 내용에 답변이 등록되었습니다.\n\n"
                + "■ 문의 제목\n" + inquiryTitle + "\n\n"
                + "■ 답변 내용\n" + answerContent + "\n\n"
                + "추가 문의가 있으시면 마이페이지의 1:1 문의에서 남겨주세요.";
        sendQuietly(toEmail, subject, text);
    }

    /** 결제까지 끝나 객실 예약이 완료되었을 때 안내 */
    public void sendReservationConfirmed(String toEmail, String nickname, String ryokanName, String orderId,
            String roomName, String planName, LocalDate checkIn, LocalDate checkOut, Integer people,
            Integer totalAmount) {
        String subject = "[清流庵] 객실 예약이 완료되었습니다";
        String text = nickname + "님, 객실 예약과 결제가 완료되었습니다.\n\n"
                + "■ 예약 정보\n"
                + "숙소 : " + (ryokanName != null ? ryokanName : "-") + "\n"
                + "예약(주문)번호 : " + orderId + "\n"
                + "객실 : " + (roomName != null ? roomName : "-") + "\n"
                + "플랜 : " + (planName != null ? planName : "-") + "\n"
                + "체크인 : " + checkIn + "\n"
                + "체크아웃 : " + checkOut + "\n"
                + "인원 : " + (people != null ? people + "명" : "-") + "\n"
                + "결제 금액 : " + (totalAmount != null ? String.format("%,d", totalAmount) + "원" : "-") + "\n\n"
                + "즐거운 숙박이 되시길 바랍니다.";
        sendQuietly(toEmail, subject, text);
    }

    private void sendQuietly(String to, String subject, String text) {
        try {
            send(to, subject, text);
        } catch (RuntimeException e) {
            log.warn("메일 발송 실패 (to={}, subject={})", to, subject, e);
        }
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
