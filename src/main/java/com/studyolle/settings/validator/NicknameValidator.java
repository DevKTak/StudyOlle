package com.studyolle.settings.validator;

import com.studyolle.account.AccountRepository;
import com.studyolle.domain.Account;
import com.studyolle.settings.form.NicknameForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class NicknameValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return NicknameForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        NicknameForm nicknameForm = (NicknameForm) target; /* NicknameForm.java 에 바인딩이 먼저 되고 NicknameValidator.java 로 온다.
                                                              NicknameForm.java 에서 @NotBlank 로 걸러졌을 거기때문에 또 null 체크 할 필요 없다 */
        Account byNickname = accountRepository.findByNickname(nicknameForm.getNickname());
        if (byNickname != null) {
            errors.rejectValue("nickname", "wrong.value", "입력하신 닉네임을 사용할 수 없습니다. (BackEnd)");
        }
    }
}
