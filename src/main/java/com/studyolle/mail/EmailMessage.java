package com.studyolle.mail;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailMessage {

    private String to; // 누구한테 보내는지

    private String subject; // 제목

    private String message; // 보낼 메시지
}
