package com.studyolle.main;

import com.studyolle.account.AccountRepository;
import com.studyolle.account.AccountService;
import com.studyolle.account.form.SignUpForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// junit5 를 쓸대는 RunWith 이런거 안써도됨
@SpringBootTest // @SpringBootTest에 이미 @ExtendWith이 달려 있기 때문
@AutoConfigureMockMvc // MVC 테스트
class MainControllerTest {

    // junit에서는 생성자 주입으로 하면 junit이 다른 인스턴스를 주입하려고 하기 때문에 사용 X
    @Autowired MockMvc mockMvc;
    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;


    @BeforeEach // 한개의 테스트 시작 전
    void beforeEach() {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("kyungtak");
        signUpForm.setEmail("test01@naver.com");
        signUpForm.setPassword("12341234");
        accountService.processNewAccount(signUpForm);
    }

    @AfterEach // 한개의 테스트 시작 후
    void afterEach() {
        accountRepository.deleteAll();
    }

    @DisplayName("이메일로 로그인 성공")
    @Test
    void login_with_email() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "test01@naver.com")
                .param("password", "12341234")
                .with(csrf())) // form을 전달할 때 csrf 토큰도 같이 전송
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/")) // 로그인에 성공할 경우 "/" 루트
                .andExpect(authenticated().withUsername("kyungtak")); /* test01 이라는 username으로 인증이 될 것이다
                                                                      UserAccount.java > super() 에서 첫번째 아규먼트인 username으로
                                                                      nickname을 줬기 때문에 email이 아닌 nickname으로 인증한다 */
    }

    @DisplayName("닉네임으로 로그인 성공")
    @Test
    void login_with_nickname() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "kyungtak")
                .param("password", "12341234")
                .with(csrf())) // form을 전달할 때 csrf 토큰도 같이 전송
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/")) // 로그인에 성공할 경우 "/" 루트
                .andExpect(authenticated().withUsername("kyungtak")); /* test01 이라는 username으로 인증이 될 것이다
                                                                      UserAccount.java > super() 에서 첫번째 아규먼트인 username으로
                                                                      nickname을 줬기 때문에 email이 아닌 nickname으로 인증한다 */
    }

    @DisplayName("로그인 실패")
    @Test
    void login_fail() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "1111111")
                .param("password", "22222222")
                .with(csrf())) // form을 전달할 때 csrf 토큰도 같이 전송
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error")) // 로그인에 실패할 경우 "/login?error"
                .andExpect(unauthenticated());
    }

    @DisplayName("로그아웃")
    @Test
    void logout() throws Exception {
        mockMvc.perform(post("/logout")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());
    }
}