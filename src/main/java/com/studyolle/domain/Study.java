package com.studyolle.domain;

import com.studyolle.account.UserAccount;
import com.studyolle.tag.Tag;
import lombok.*;

import javax.persistence.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

/*
    - 조인을 하든 각자 가져오든 연관된 데이터를 다 조회하면 낭비
    - 필요한 엔티티만 조회하자
 */
@NamedEntityGraph(name = "Study.withTagsAndManagers", attributeNodes = {
        @NamedAttributeNode("tags"),
        @NamedAttributeNode("managers")})
@NamedEntityGraph(name = "Study.withZonesAndManagers", attributeNodes = {
        @NamedAttributeNode("zones"),
        @NamedAttributeNode("managers")})
@NamedEntityGraph(name = "Study.withManagers", attributeNodes = {
        @NamedAttributeNode("managers")})
@NamedEntityGraph(name = "Study.withMembers", attributeNodes = {
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

    private LocalDateTime recruitingUpdatedDateTime; // 스터디를 자주 열고 닫지 못하게 제한을 주기 위함

    private boolean recruiting; // 현재 인원 모집중인지 여부

    private boolean published; // 공개상태 여부

    private boolean closed; // 종료 여부

    private boolean useBanner; // 배너 사용 여부

    private int memberCount;

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

    public boolean isRemovable() {
        return !this.published; // TODO 모임을 했던 스터디는 삭제할 수 없다.
    }
    /** End **/

    /** 스터디 공개 **/
    public void publish() {
        if (!this.closed && !this.published) { // 스터디가 종료되지 않았고 공개되지 않았을 때 공개 가능
            this.published = true;
            this.publishedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("스터디를 공개할 수 없는 상태입니다. 스터디를 이미 공개했거나 종료했습니다.");
        }
    }

    /** 스터디 종료 **/
    public void close() {
        if (this.published && !this.closed) { // 스터디가 종료되지 않았고 공개된 상태일 때 종료 가능
            this.closed = true;
            this.closedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("스터디를 종료할 수 없습니다. 스터디를 공개하지 않았거나 이미 종료한 스터디입니다.");
        }
    }

    public void startRecruit() {
        if (canUpdateRecruiting()) {
            this.recruiting = true;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("인원 모집을 시작할 수 없습니다. 스터디를 공개하거나 한 시간 뒤 다시 시도하세요.");
        }
    }

    public void stopRecruit() {
        if (canUpdateRecruiting()) {
            this.recruiting = false;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("인원 모집을 멈출 수 없습니다. 스터디를 공개하거나 한 시간 뒤 다시 시도하세요.");
        }
    }

    /** 팀원 모집 시작/중단 자주 못하게 방지 **/
    public boolean canUpdateRecruiting() {
        return this.published && this.recruitingUpdatedDateTime == null || this.recruitingUpdatedDateTime.isBefore(LocalDateTime.now().minusHours(1));
    }

    public void addMember(Account account) {
        this.getMembers().add(account);
        this.memberCount++;
    }

    public void removeMember(Account account) {
        this.getMembers().remove(account);
        this.memberCount--;
    }

    /** account가 해당 스터디의 매니저인지 **/
    public boolean isManagedBy(Account account) {
        return this.getManagers().contains(account);
    }

    /** 스터디 URL 인코딩 **/
    public String getEncodedPath() {
        return URLEncoder.encode(this.path, StandardCharsets.UTF_8);
    }
}
