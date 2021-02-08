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
                기본전략에 따른다 */
    @EntityGraph(value ="Study.withAll", type = EntityGraph.EntityGraphType.LOAD)
    Study findByPath(String path);
}
