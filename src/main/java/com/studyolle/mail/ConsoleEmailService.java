package com.studyolle.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("local") // 로컬이라는 프로파일로 실행을 할 때만 사용 (개발환경)
@Component
/**
 * 콘솔로만 출력하는 이메일 구현체
 */
public class ConsoleEmailService implements EmailService {

    @Override
    public void sendEmail(EmailMessage emailMessage) {
        log.info("sent email: {}", emailMessage.getMessage());
    }
}
