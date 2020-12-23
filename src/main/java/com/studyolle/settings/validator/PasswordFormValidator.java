package com.studyolle.settings.validator;

import com.studyolle.settings.form.PasswordForm;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/* SignUpFormValidator.java 처럼 다른 빈을 사용할 것이 없기 때문에 빈으로 등록 할 필요가 없이
   다른곳에서 (SettingsController.java) 그냥 new 해서 만들면 된다 */
public class PasswordFormValidator implements Validator {

    @Override
    // 어떤 타입의 폼 객체를 검증을 할 것인지
    public boolean supports(Class<?> clazz) {
        return PasswordForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object object, Errors errors) {
        PasswordForm passwordForm = (PasswordForm)object;

        if (!passwordForm.getNewPassword().equals(passwordForm.getNewPasswordConfirm())) {
            // 첫번째 인자 "newPassword" 는 PasswordForm 에 있는 필드명과 같아야 하는 것 같다
            errors.rejectValue("newPassword", "wrong.value", "입력한 새 패스워드가 일치하지 않습니다. (BackEnd)");
        }
    }
}
