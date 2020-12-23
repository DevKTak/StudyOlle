package com.studyolle.account;

import com.studyolle.domain.Account;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

/**
 * SpringSecurity가 다루는 User정보와
 * 우리 도메인이 다루는 유저정보의 사이를 매꾸는 일종의 어댑터 역할이라고 생각
 */
@Getter
public class UserAccount extends User {

    private Account account; // 이 Account는 우리가 가지고 있는 유저 정보

    public UserAccount(Account account) {
        // SpringSecurity가 가지고 있는 유저정보를 우리가 가지고 있는 유저 정보랑 연동
        super(account.getNickname(), account.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.account = account;
    }
}
