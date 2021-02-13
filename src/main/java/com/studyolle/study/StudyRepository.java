package com.studyolle.study;

import com.studyolle.domain.Study;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study, Long> {

    boolean existsByPath(String path);

    /* type = EntityGraph.EntityGraphType.LOAD :::
                EntityGraph에 명시한 연관관계는 EAGER 모드로 가져오고
                나머지 Attributes는 XToOne으로 끝나는 것은 EAGER, XToMany로 끝나는 것은 LAZY
                기본전략에 따른다
       type = EntityGraph.EntityGraphType.FETCH :::
                선언한것 빼고는 다 LAZY로 작동
    */

    @EntityGraph(value = "Study.withAll", type = EntityGraph.EntityGraphType.LOAD)
    Study findByPath(String path);

    // Spring Data JPA 관점에선 결국 findByPath와 같은 쿼리가 작동하지만
    // 다른 @EntityGraph를 쓰기위한 메서드명을 지었음
    // StudyWithTags 부분은 JPA한텐 무의미한 이름
    @EntityGraph(value = "Study.withTagsAndManagers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithTagsByPath(String path);

    @EntityGraph(value = "Study.withZonesAndManagers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithZonesByPath(String path);

    @EntityGraph(value = "Study.withManagers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithManagersByPath(String path);

    @EntityGraph(value = "Study.withMembers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithMembersByPath(String path);
}
