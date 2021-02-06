package com.studyolle.account;

import com.studyolle.account.form.SignUpForm;
import com.studyolle.config.AppProperties;
import com.studyolle.domain.Account;
import com.studyolle.domain.Tag;
import com.studyolle.domain.Zone;
import com.studyolle.mail.EmailMessage;
import com.studyolle.mail.EmailService;
import com.studyolle.settings.form.Notifications;
import com.studyolle.settings.form.Profile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final TemplateEngine templateEngine; // 타임리프의 가장 핵심적인 클래스
    private final AppProperties appProperties;

    /**
     * 회원 가입
     */
    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm); // 회원 저장
        sendSignUpConfirmEmail(newAccount); // 메일 보내기
        return newAccount;
    }

    //== 회원 저장 ==//
    public Account saveNewAccount(@ModelAttribute @Valid SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));

        // Account 타입의 인스턴스가 만들어지고 signUpForm에 들어있는 데이터로 채워짐
        Account account = modelMapper.map(signUpForm, Account.class);

        account.generateEmailCheckToken(); // 토큰 만들기
        /*Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(passwordEncoder.encode(signUpForm.getPassword()))
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .studyUpdatedByWeb(true)
                .build();*/
        return accountRepository.save(account);
    }

    /**
     * 메일 전송 기능
     */
    //== 회원가입 시 인증메일 전송 ==//
    public void sendSignUpConfirmEmail(Account newAccount) {
        Context context = new Context(); // model과 같은 역할
        context.setVariable("link", "/check-email-token?token=" + newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail());
        context.setVariable("nickname", newAccount.getNickname());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("message", "스터디올래 서비스를 사용하려면 링크를 클릭하세요");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context); // prefix :=> template / postfix :=> .html

        //== 메일 전송 폼 객체 생성 ==//
        EmailMessage emailMessage = EmailMessage.builder()
                .to(newAccount.getEmail())
                .subject("스터디올래, 회원 가입 인증")
                .message(message)
                .build();

        //== 메일 전송 ==//
        emailService.sendEmail(emailMessage);
    }

    //== 패스워드 없이 로그인 버튼 클릭 시 인증메일 전송 ==//
    public void sendLoginLink(Account account) {
        account.generateEmailCheckToken(); // 이메일체크 랜덤 토큰 생성, 토큰 생성 시간 저장

        Context context = new Context(); // model과 같은 역할
        context.setVariable("link", "/login-by-email?token=" + account.getEmailCheckToken() +
                "&email=" + account.getEmail());
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", "스터디올래 로그인하기");
        context.setVariable("message", "로그인 하려면 링크를 클릭하세요");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context); // prefix :=> template / postfix :=> .html

        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("스터디올래, 로그인 링크")
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

    public void login(Account account) {
        // 로그인 할 때 토큰 만들어서 셋팅하기
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
        // 첫번째 전달인자로 Principal을 넣어 준다, Principal로 account 객체를 넣어서 @AuthenticationPrincipal을 사용하기 위해 UserAccount 클래스를 만듬
                new UserAccount(account), // UserAccount를 Principal 객체로 썼음
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(token); // 만든 토큰값 넣기, view와 테스트에서 authenticated()로 이용할 수 있음
    }

    @Transactional(readOnly = true) // 데이터를 변경하는 기능 메소드가 아니기 때문
    @Override
    /*
        "/login" post 요청시
        화면에서 입력한 이용자의 이름(username)을 가지고 시큐리티 내부에서 loadUserByUsername() 메소드를 호출하여
        DB에 있는 이용자의 정보를 UserDetails 형으로 가져온다.
     */
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(emailOrNickname);

        if (account == null) {
            account = accountRepository.findByNickname(emailOrNickname);
        }

        if (account == null) {
            throw new UsernameNotFoundException(emailOrNickname);
        }

        return new UserAccount(account); // principal에 해당하는 객체를 넘김
    }

    public void completeSignUp(Account account) {
        account.completeSignUp();
//        login(account); 강의에는 있는데 필요없어 보여서 일단 주석처리!!
    }

    public void updateProfile(Account account, Profile profile) {
        modelMapper.map(profile, account);
        accountRepository.save(account); // save()는 id 값이 있는지 없는지 보고 있으면 merge를 시킨다
    }

    public void updatePassword(Account account, String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account); // 세션에 있는 account는 detached 상태이기 때문에 명시적으로 merge
    }

    public void updateNotifications(Account account, Notifications notifications) {
        modelMapper.map(notifications, account);
        accountRepository.save(account);
    }

    public void updateNickname(Account account, String nickname) {
        // account 는 detached 상태이기 때문에 변경감지(더티체킹)이 작동 안한다
        account.setNickname(nickname);
        accountRepository.save(account); // 명시적으로 save 해주기 (save 할 때 merge 가 일어난다)
        login(account); // 로그인도 다시 해줘야함!
    }

    /**
     * 관심 주제 태그
     */
    //-- 관심 주제 태그 조회하기 --//
    public Set<Tag> getTags(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId()); // Tag, Account 는 @ManyToMany 연관관계이기 때문에 조인쿼리 나감
        return byId.orElseThrow().getTags(); // 없으면(null이면) 에러를 던지고 있으면 태그 정보 리턴
    }

    //== 관심 주제 태그 추가하기 ==//
    public void addTag(Account account, Tag tag) {
        /* account 객체가 detached 상태이기 때문에 먼저 읽어와야한다
           findById와 get 두 가지 방법이 있음
           1. findById() 하는 순간 Eager패치라서 무조건 읽어온다
           2. getOne()은 Lazy 로딩이라 필요한 순간에만 EntityManager를 통해서 읽어온다
         */
        Optional<Account> byId = accountRepository.findById(account.getId()); // select * from account where id = ?
        System.out.println("===================================");
        byId.ifPresent(accountV -> accountV.getTags().add(tag)); // 있으면(null이 아니면) Tag를 Account > tags에 추가
        // select tags0_.account_id as account_1_1_0_, tags0_.tags_id as tags_id2_1_0_, tag1_.id as id1_3_1_, tag1_.title as title2_3_1_ from account_tags tags0_ inner join tag tag1_ on tags0_.tags_id=tag1_.id where tags0_.account_id=?
        // inner join으로 select 도 한번 하는거 같음
        System.out.println("===================================");
        // insert into account_tags (account_id, tags_id) values (?, ?)

    }

    //== 관심 주제 태그 삭제하기 ==//
    public void removeTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(accountV -> accountV.getTags().remove(tag));
    }

    /**
     * 지역 정보 메서드
     */
    //== 지역 정보 조회하기 ==//
    public Set<Zone> getZones(Account account) {
        val byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getZones();
    }

    //== 지역 정보 추가하기 ==//
    public void addZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().add(zone));
    }

    //== 지역 정보 삭제하기 ==//
    public void removeZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().remove(zone));
    }


}