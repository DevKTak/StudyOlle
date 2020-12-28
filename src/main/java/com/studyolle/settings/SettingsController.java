package com.studyolle.settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolle.account.AccountService;
import com.studyolle.account.CurrentAccount;
import com.studyolle.domain.Account;
import com.studyolle.domain.Tag;
import com.studyolle.domain.Zone;
import com.studyolle.settings.form.*;
import com.studyolle.settings.validator.NicknameValidator;
import com.studyolle.settings.validator.PasswordFormValidator;
import com.studyolle.tag.TagRepository;
import com.studyolle.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.studyolle.settings.SettingsController.ROOT;
import static com.studyolle.settings.SettingsController.SETTINGS;

@Controller
@RequestMapping(ROOT + SETTINGS)
@RequiredArgsConstructor
public class SettingsController {
    static final String ROOT = "/";
    static final String SETTINGS = "settings";
    static final String PROFILE = "/profile";
    static final String PASSWORD = "/password";
    static final String NOTIFICATIONS = "/notifications";
    static final String ACCOUNT = "/account";
    static final String TAGS = "/tags";
    static final String ZONES = "/zones";


    private final AccountService accountService;
    private final ModelMapper modelMapper;
    private final NicknameValidator nicknameValidator;
    private final TagRepository tagRepository;
    private final ZoneRepository zoneRepository;
    private final ObjectMapper objectMapper; // List를 Json 문자열로 변환할때 사용

    /** 백엔드 커스텀 유효성 검사 (PasswordFormValidator.java) 사용**/
    @InitBinder("passwordForm") // signUpSubmit() 메소드 파라미터 같은 상황에서 SignUpForm을 받을 때 호출!, SignUpForm의 카멜케이스로 들어감
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @InitBinder("nicknameForm")
    public void nicknameFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(nicknameValidator);
    }

    @GetMapping(PROFILE)
    public String updateProfileForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
//        model.addAttribute(new Profile(account)); // th:field에서 사용함으로써 Profile 객체와 연결 가능, account 객체의 정보를 가지고 인자있는 생성자를 통하여 Profile 객체를 만듬
        model.addAttribute(modelMapper.map(account, Profile.class)); // Profile 타입의 인스턴스가 만들어지고 account에 들어있는 데이터로 채워짐
        return SETTINGS + PROFILE; // return 타입을 void로 하면 알아서 URL명으로 리턴한다
    }

    @PostMapping(PROFILE)
    public String updateProfile(@CurrentAccount Account account, @Valid @ModelAttribute Profile profile, Errors errors,
                                Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account); /* 폼을 채웠던 Profile 데이터와 error에 대한 정보는
                                            Model에 자동으로 들어가기 때문에 GET 요청 때 처럼 다시 account 정보만 넣어주면 된다 */
            return SETTINGS + PROFILE;
        }
        accountService.updateProfile(account, profile); // account 정보를 profile에 받아온 값으로 변경 하기 위한 메서드
        attributes.addFlashAttribute("message", "프로필을 수정했습니다."); /* 리다이렉트 시키고 한번쓰고 말 데이터를 보내는 용도
                                                                                     @GetMapping(SETTINGS_PROFILE_URL) 여기로 리다이렉트 시켜주고
                                                                                     Model 객체로 자동으로 들어간다 */
        return "redirect:/" +  SETTINGS + PROFILE; // 사용자가 refesh 했을시 폼서브밋이 다시 일어나지 않도록 리다이렉트 시켜줌
    }

    @GetMapping(PASSWORD)
    public String updatePasswordForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());

        return SETTINGS + PASSWORD;
    }

    @PostMapping(PASSWORD)
    public String updatePassword(@CurrentAccount Account account, @Valid @ModelAttribute PasswordForm passwordForm, Errors errors,
                                 Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);

            return SETTINGS + PASSWORD;
        }
        accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "패스워드를 변경했습니다.");

        return "redirect:/" + SETTINGS + PASSWORD;
    }

    @GetMapping(NOTIFICATIONS)
    public String updateNotificationsForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
//        model.addAttribute(new Notifications(account));
        model.addAttribute(modelMapper.map(account, Notifications.class));

        return SETTINGS + NOTIFICATIONS;
    }

    @PostMapping(NOTIFICATIONS)
    public String updateNotifications(@CurrentAccount Account account, Model model,
                                      @Valid @ModelAttribute Notifications notifications, Errors errors,
                                      RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);

            return SETTINGS + NOTIFICATIONS;
        }
        accountService.updateNotifications(account, notifications);
        attributes.addFlashAttribute("message", "알림 설정을 변경했습니다.");

        return "redirect:/" + NOTIFICATIONS;
    }

    @GetMapping(TAGS)
    public String updateTags(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);

        Set<Tag> tags = accountService.getTags(account);
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));

        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags)); // List<String> =>>> JSON String 변환

        System.out.println("allTags =>>> " + allTags); // allTags = [tesateas, tete]
        System.out.println("whitelist  =>>> " + objectMapper.writeValueAsString(allTags)); // objectMapper ===> ["tesateas","tete"]

        return SETTINGS + TAGS;
    }

    @PostMapping(TAGS + "/add")
    @ResponseBody // ajax 요청으로 들어온것이기 때문에 응답도 @ResponseBody
    public ResponseEntity addTag(@CurrentAccount Account account, @RequestBody TagForm tagForm) { // 요청 본문으로 들어오기 때문에 @RequestBody
        String title = tagForm.getTagTitle();
        // Optional로 안받으면 null이 들어감, Optional 쓰는 경우의 로직
        // title로 Tag를 찾아보고 데이터가 없으면 그 title에 해당하는것을 저장해서 받아오기
//        Tag tag = tagRepository.findByTitle(title).orElseGet(() -> tagRepository.save(Tag.builder()
//                            .title(tagForm.getTagTitle())
//                            .build()));

        // Optional 을 사용 안한 경우의 로직
        Tag tag = tagRepository.findByTitle(title); // select id, title from tag where title = ?

        if (tag == null) {
            tag = tagRepository.save(Tag.builder().title(title).build()); // insert into tag (title, id) values (?, ?)
        }
        accountService.addTag(account, tag);

        return ResponseEntity.ok().build(); // 성공 응답 보내기
    }

    @PostMapping(TAGS + "/remove")
    @ResponseBody // ajax 요청으로 들어온것이기 때문에 응답도 @ResponseBody
    public ResponseEntity removeTag(@CurrentAccount Account account, @RequestBody TagForm tagForm) { // 요청 본문으로 들어오기 때문에 @RequestBody
        Tag tag = tagRepository.findByTitle(tagForm.getTagTitle()); // select id, title from tag where title = ?

        if (tag == null) {
            return ResponseEntity.badRequest().build(); // 실패 응답 보내기
        }
        accountService.removeTag(account, tag);

        return ResponseEntity.ok().build();
    }

    @GetMapping(ZONES)
    public String updateZonesForm(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);

        Set<Zone> zones = accountService.getZones(account);
        model.addAttribute("zones", zones.stream().map(Zone::toString).collect(Collectors.toList()));

        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));

        return SETTINGS + ZONES;
    }

    @PostMapping(ZONES + "/add")
    @ResponseBody
    public ResponseEntity addZone(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm) {
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        accountService.addZone(account, zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping(ZONES + "/remove")
    @ResponseBody
    public ResponseEntity removeZone(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm) {
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        accountService.removeZone(account, zone);
        return ResponseEntity.ok().build();
    }

    @GetMapping(ACCOUNT)
    public String updateAccountForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NicknameForm.class));

        return SETTINGS + ACCOUNT;
    }

    @PostMapping(ACCOUNT)
    public String updateAccount(@CurrentAccount Account account, @Valid NicknameForm nicknameForm, Errors errors,
                                Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) { // NicknameForm.java 에 있는 것도 검증하고 NicknameValidator.java 까지 검증한 후 에러를 넣어줌
            model.addAttribute(account);

            return SETTINGS + ACCOUNT;
        }
        accountService.updateNickname(account, nicknameForm.getNickname());
        attributes.addFlashAttribute("message", "닉네임을 수정했습니다.");
        return "redirect:/" + SETTINGS + ACCOUNT;
    }

}
