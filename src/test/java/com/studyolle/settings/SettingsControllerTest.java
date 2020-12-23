package com.studyolle.settings;

import com.studyolle.account.AccountRepository;
import com.studyolle.account.AccountService;
import com.studyolle.domain.Account;
import com.studyolle.settings.SettingsController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

//    @WithAccount()  @WithAccount("인증된사용자") 부분은 [프로필 수정 테스트] 강의 부분 다시 들어야함
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    void updateProfile() throws Exception {
        String bio = "짧은 소개를 수정 하는 경우.";
        mockMvc.perform(post(SettingsController.ROOT + SettingsController.SETTINGS + SettingsController.PROFILE)
                .param("bio", bio)
                .with(csrf())) // post로 form 데이터를 보낼때는 항상 csrf 토큰을 잊지 말것
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.ROOT + SettingsController.SETTINGS + SettingsController.PROFILE))
                .andExpect(flash().attributeExists("message"));

        Account tak = accountRepository.findByNickname("tak");
        assertEquals(bio, tak.getBio());

    }
}