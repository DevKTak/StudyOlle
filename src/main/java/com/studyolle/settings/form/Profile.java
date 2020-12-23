package com.studyolle.settings.form;

import com.studyolle.domain.Account;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Getter @Setter
public class Profile {

    @Length(max = 35)
    private String bio; // 자기소개

    @Length(max = 50)
    private String url;

    @Length(max = 50)
    private String occupation; // 직업

    @Length(max = 50)
    private String location;

    private String profileImage;

//    public Profile(Account account) {
    /** ModelMapper를 활용하려했으나 Profile은 bean이 아니기 때문에 ModelMapper를 주입받지 못하고
        ModelMapper modelMapper = new ModelMapper(); 해서 사용해도 되지만 컨트롤러에서 처리!
        Notifications.java 도 같음
     */
//        this.bio = account.getBio();
//        this.url = account.getUrl();
//        this.occupation = account.getOccupation();
//        this.location = account.getLocation();
//        this.profileImage = account.getProfileImage();
//    }
}
