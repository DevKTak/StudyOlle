package com.studyolle.config;

import com.studyolle.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity // SpringSecurity 설정을 직접 다 하겠다
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter { // WebSecurity 설정을 좀 더 손쉽게 하기 위한 상속

    private final AccountService accountService;
    private final DataSource dataSource;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .mvcMatchers("/", "/login", "/sign-up", "check-email-token",
                    "/email-login", "/login-by-email", "/search/study").permitAll() // 권한 확인없이 접근 해야 할 요청들
            .mvcMatchers(HttpMethod.GET, "/profile/*").permitAll() // 프로필 요청은 GET만 허용
            .anyRequest().authenticated(); // 나머지 요청은 로그인 (인증받은 사용자) 해야만 사용 가능

        http.formLogin()
                .loginPage("/login").permitAll();

        http.logout()
                .logoutSuccessUrl("/");

        http.rememberMe() // 기본 세션 타임아웃 30분 이후에도 로그인 기억하기
                .userDetailsService(accountService)
                .tokenRepository(tokenRepository()); // username, 토큰(랜덤, 매번 바뀜), 시리즈(랜덤, 고정) 3가지 조합해서 만든 토큰
    }

    /**
     * DB 에서 토큰값을 가져와서 비교해야 하기 때문에 이 메소드 필요 (remember-me)
     */
    @Bean
    public PersistentTokenRepository tokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        return jdbcTokenRepository;
    }

    /**
     * 스태틱한 리소스들은 인증을 하지 않게(시큐리티 필터를 적용하지 말아라) 하는 로직
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .mvcMatchers("/node_modules/**") // 이 경로에 있는것도 시큐리티 필터 적용 X
//                    CSS(new String[]{"/css/**"}),
//                    JAVA_SCRIPT(new String[]{"/js/**"}),
//                    IMAGES(new String[]{"/images/**"}),
//                    WEB_JARS(new String[]{"/webjars/**"}),
//                    FAVICON(new String[]{"/**/favicon.ico"});
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }
}
