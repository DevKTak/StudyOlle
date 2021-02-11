package com.studyolle.study;

import com.studyolle.domain.Account;
import com.studyolle.domain.Study;
import com.studyolle.domain.Zone;
import com.studyolle.study.form.StudyDescriptionForm;
import com.studyolle.tag.Tag;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository repository;
    private final ModelMapper modelMapper;

    /** 스터디 생성 **/
    public Study createNewStudy(Study study, Account account) {
        Study newStudy = repository.save(study);
        newStudy.addManger(account);
        return newStudy;

    }

    /** 스터디 수정이 가능한지 **/
    public Study getStudyToUpdate(Account account, String path) {
        Study study = this.getStudy(path);
        checkIfManager(account, study);

        return study;
    }

    /** 스터디가 존재하는지 null 체크 메서드 호출 **/
    public Study getStudy(String path) {
        Study study = this.repository.findByPath(path);
        checkIfExistingStudy(path, study);

        return study;
    }

    /** 스터디 정보 업데이트 **/
    public void updateStudyDescription(Study study, StudyDescriptionForm studyDescriptionForm) {
        modelMapper.map(studyDescriptionForm, study); //
    }

    /** 배너 이미지 업데이트 **/
    public void updateStudyImage(Study study, String image) {
        study.setImage(image);
    }

    /** 배너 사용 O **/
    public void enableStudyBanner(Study study) {
        study.setUseBanner(true);
    }

    /** 배너 사용 X **/
    public void disableStudyBanner(Study study) {
        study.setUseBanner(false);
    }

    public void addTag(Study study, Tag tag) {
        study.getTags().add(tag);
    }

    public void removeTag(Study study, Tag tag) {
        study.getTags().remove(tag);
    }

    public void addZone(Study study, Zone zone) {
        study.getZones().add(zone);
    }

    public void removeZone(Study study, Zone zone) {
        study.getZones().remove(zone);
    }

    public Study getStudyToUpdateTag(Account account, String path) {
        Study study = repository.findStudyWithTagsByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    }

    public Study getStudyToUpdateZone(Account account, String path) {
        Study study = repository.findStudyWithZonesByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    }

    public Study getStudyToUpdateStatus(Account account, String path) {
        Study study = repository.findStudyWithManagersByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    }

    /** account가 해당 스터디의 매니저인지 **/
    private void checkIfManager(Account account, Study study) {
        if (!study.isManagedBy(account)) {
            // SpringSecurity에 있는 Exception
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
    }

    /** 스터디 존재 예외처리 **/
    private void checkIfExistingStudy(String path, Study study) {
        if (study == null) {
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
    }

    public void publish(Study study) {
        study.publish();
    }

    public void close(Study study) {
        study.close();
    }

    public void startRecruit(Study study) {
        study.startRecruit();
    }

    public void stopRecruit(Study study) {
        study.stopRecruit();
    }

}
