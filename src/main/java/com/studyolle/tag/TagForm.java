package com.studyolle.tag;

import lombok.Data;

// SettingController.java > addTag() 에 요청 본문으로 들어오는 데이터 (key: 'tagTitle')를 받아주는 폼
@Data
public class TagForm {

    private String tagTitle;
}
