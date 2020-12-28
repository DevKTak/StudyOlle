package com.studyolle.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
// application.properties에 있는 app.host를 받아옴
@ConfigurationProperties("app") //  app에 해당하는 것을 바인딩을 받겠다
public class AppProperties {

    private String host; // host를 바인딩 받겠다
}
