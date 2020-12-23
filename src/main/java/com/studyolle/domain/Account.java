package com.studyolle.domain;

import lombok.*;
import org.apache.tomcat.jni.Local;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    @Id @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;

    private boolean emailVerified; // email 인증이 된 계정인지

    private String emailCheckToken; // email을 검증할 토큰값

    private LocalDateTime emailCheckTokenGeneratedAt; // emailCheckToken을 생성 시간

    private LocalDateTime joinedAt;

    private String bio; // 자기소개

    private String url;

    private String occupation; // 직업

    private String location;

    @Lob @Basic(fetch = FetchType.EAGER) // String => varcahr(255) / @Lob == text
    // 이미지 같은 경우는 유저를 로딩할때 종종 같이 쓰일거 같아서 FetchType.EAGER로 줬음
    private String profileImage;

    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb = true; // 스터디가 만들어 졌다는 결과

    private boolean studyEnrollmentResultByEmail; // 모임 가입신청 결과

    private boolean studyEnrollmentResultByWeb = true;

    private boolean studyUpdatedByEmail; // 스터디 갱신된 정보

    private boolean studyUpdatedByWeb = true;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    /**
     * 이메일체크 랜덤 토큰 생성, 토큰 생성 시간 저장
     */
    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailCheckTokenGeneratedAt = LocalDateTime.now();
    }

    /**
     * 가입 인증 통과 시
     */
    public void completeSignUp() {
        this.emailVerified = true;
        this.joinedAt = LocalDateTime.now();
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);

    }

    /**
     * 이메일 재전송 가능 여부 파악
     */
    public boolean canSendConfirmEmail() {
        // isBefore() 메소드 : 두 개의 날짜와 시간 객체를 비교하여 현재 객체가 명시된 객체보다 앞선 시간인지를 비교함
        return this.emailCheckTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(1));
    }
}