package com.studyolle.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter @EqualsAndHashCode(of ="id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Study {

    @Id @GeneratedValue
    private Long id;

    @ManyToMany
    private Set<Account> managers = new HashSet<>();

    @ManyToMany
    private Set<Account> member = new HashSet<>();

    @Column(unique = true)
    private String path; // 스터디의 URL

    private String title;

    private String shortDescription;

    // varchar255자 안에 못담기 때문에 Lob, @Lob의 기본값이 EAGER인데 그냥 명시함, 스터디정보를 조회할 때 무조건 가져와라
    @Lob @Basic(fetch = FetchType.EAGER)
    private String fullDescription; // 전체 본문

    @Lob @Basic(fetch = FetchType.EAGER)
    private String image;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    private LocalDateTime publishedDateTime; // 스터디를 공개한 시간

    private LocalDateTime closeDateTime; // 스터디를 종료한 시간

    private LocalDateTime recruitingUpdateDateTime; // 스터디를 자주 열고 닫지 못하게 제한을 주기 위함

    private boolean recruiting; // 현재 인원 모집중인지 여부

    private boolean published; // 공개상태 여부

    private boolean close; // 종료 여부

    private boolean useBanner; // 배너 사용 여부

    /** 매니저 추가 **/
    public void addManger(Account account) {
        this.managers.add(account);
    }
}
