package com.studyolle.account;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * SecurityConfig.java > tokenRepository() > JdbcTokenRepositoryImpl > CREATE_TABLE_SQL 스키마에 해당하는 Entity
 * JPA 이기 때문에 Entity로 만들어줘야함 (Remember-me 기능을 위한)
 */
@Table(name = "persistent_logins")
@Entity
@Getter
@Setter
public class PersistentLogins {

    @Id
    @Column(length = 64)
    private String series;

    @Column(nullable = false, length = 64)
    private String username;

    @Column(nullable = false, length = 64)
    private String token;

    @Column(name = "last_used", nullable = false, length = 64)
    private LocalDateTime lastUsed;

}
