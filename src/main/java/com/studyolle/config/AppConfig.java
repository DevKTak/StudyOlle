package com.studyolle.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.NameTokenizers;
import org.modelmapper.spi.NameTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    @Bean // BCrypt 인코딩을 위해 빈으로 등록해뒀음
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean // ModelMapper는 매번 만들어서 사용 할 필요가 없기 때문에 빈으로 등록해둠
    /*
      Account.java 에서 email 같은 경우 비슷한 name이 많아서 구분이 애매하기때문에 설정을 해줌
      map(source, destination) 인자 둘 다 UNDERSCORE가 아닌 이상 구분하지 않게 설정
     */
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setDestinationNameTokenizer(NameTokenizers.UNDERSCORE)
                .setSourceNameTokenizer(NameTokenizers.UNDERSCORE);
        return modelMapper;
    }
}
