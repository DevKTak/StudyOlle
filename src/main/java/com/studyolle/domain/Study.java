package com.studyolle.domain;

import com.studyolle.account.UserAccount;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/*
    - 어차피 조회할 데이터라면 쿼리 개수를 줄이고 join을 해서 가져오자
    - Left Outer join으로 연관 데이터를 한번에 조회할 수도 있다
 */
@NamedEntityGraph(name = "Study.withAll", attributeNodes = {
        @NamedAttributeNode("tags"),
        @NamedAttributeNode("zones"),
        @NamedAttributeNode("managers"),
        @NamedAttributeNode("members")})
@Entity
@Getter @Setter @EqualsAndHashCode(of ="id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Study {

    @Id @GeneratedValue
    private Long id;

    @ManyToMany
    private Set<Account> managers = new HashSet<>();

    @ManyToMany
    private Set<Account> members = new HashSet<>();

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

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    private LocalDateTime publishedDateTime; // 스터디를 공개한 시간

    private LocalDateTime closedDateTime; // 스터디를 종료한 시간

    private LocalDateTime recruitingUpdateDateTime; // 스터디를 자주 열고 닫지 못하게 제한을 주기 위함

    private boolean recruiting; // 현재 인원 모집중인지 여부

    private boolean published; // 공개상태 여부

    private boolean closed; // 종료 여부

    private boolean useBanner; // 배너 사용 여부

    /** 매니저 추가 **/
    public void addManger(Account account) {
        this.managers.add(account);
    }

    /** view 에서 호출한 메서드 **/
    //== 스터디에 가입이 가능한지 ==//
    public boolean isJoinable(UserAccount userAccount) {
        Account account = userAccount.getAccount();

        // 공개가 됐고 인원 모집중이고 멤버가 아니고 매니저가 아니면 가입이 가능
        return this.isPublished() && this.isRecruiting()
                && !this.members.contains(account) && !this.managers.contains(account);
    }

    //== 멤버 인지 ==//
    public boolean isMember(UserAccount userAccount) {
        return this.members.contains(userAccount.getAccount());
    }

    //== 매니저 인지 ==//
    public boolean isManager(UserAccount userAccount) {
        return this.managers.contains(userAccount.getAccount());
    }
    /** End **/

    /** account가 해당 스터디의 매니저인지 **/
    public boolean isManagedBy(Account account) {
        return this.getManagers().contains(account);
    }

}
