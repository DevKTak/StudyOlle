package com.studyolle.settings.form;

import com.studyolle.domain.Zone;
import lombok.Data;

@Data
public class ZoneForm {

    //             zoneName =>>> %s(%s)/%s 이런형태
    private String zoneName; // Zone.java 에서 toString()으로 오버라이딩 해논 문자열이 그대로 오니까 각 메소드로 substring 해서 나눔

    public String getCityName() {
        return zoneName.substring(0, zoneName.indexOf("("));
    }

    public String getProvinceName() {
        return zoneName.substring(zoneName.indexOf("/") + 1);
    }

    public String getLocalNameOfCity() {
        return zoneName.substring(zoneName.indexOf("(") + 1, zoneName.indexOf(")"));
    }

    public Zone getZone() { // 생성 메서드 만들어논것 같은데 사용하지는 않는것 같음
        return Zone.builder().city(this.getCityName())
                .localNameOfCity(this.getLocalNameOfCity())
                .province(this.getProvinceName()).build();
    }
}
