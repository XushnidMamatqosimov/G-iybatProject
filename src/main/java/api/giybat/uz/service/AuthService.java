package api.giybat.uz.service;

import api.giybat.uz.dto.AppResponse;
import api.giybat.uz.dto.auth.AuthDTO;
import api.giybat.uz.dto.profile.ProfileDTO;
import api.giybat.uz.dto.auth.RegistrationDTO;
import api.giybat.uz.dto.auth.ResetPasswordConfirmDTO;
import api.giybat.uz.dto.auth.ResetPasswordDTO;
import api.giybat.uz.dto.sms.SmsResendDTO;
import api.giybat.uz.dto.sms.SmsVerificationDTO;
import api.giybat.uz.entity.ProfileEntity;
import api.giybat.uz.enums.AppLanguages;
import api.giybat.uz.enums.GeneralStatus;
import api.giybat.uz.enums.ProfileRole;
import api.giybat.uz.exp.AppBadException;
import api.giybat.uz.repository.EmailHistoryRepository;
import api.giybat.uz.repository.ProfileRepository;
import api.giybat.uz.repository.ProfileRoleRepository;
import api.giybat.uz.util.EmailUtil;
import api.giybat.uz.util.JwtUtil;
import api.giybat.uz.util.PhoneUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private ProfileRoleRepository profileRoleRepository;
    @Autowired
    private ProfileRoleService profileRoleService;
    @Autowired
    private SendingEmailService sendingEmailService;
    @Autowired
    private ProfileService profileService;
    @Autowired
    private ResourceBundleService resourceBundleService;
    @Autowired
    private SmsSendService smsSendService;
    @Autowired
    private SmsHistoryService smsHistoryService;
    @Autowired
    private EmailHistoryService emailHistoryService;
    @Autowired
    private EmailHistoryRepository emailHistoryRepository;

    // 1. REGISTRATION
    public AppResponse<String> registration(RegistrationDTO dto, AppLanguages lang) {
        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleIsTrue(dto.getUsername());
        if (optional.isPresent()) {
            ProfileEntity profile = optional.get();
            if (profile.getStatus().equals(GeneralStatus.IN_REGISTRATION)) {
                profileRoleService.deleteRoles(profile.getId());
                profileRepository.delete(profile);
                /// send SMS/Email
            } else {
                throw new AppBadException(resourceBundleService.getMessage("email.phone.exists", lang));
            }
        }

        //create profile
        ProfileEntity profile = new ProfileEntity();
        profile.setName(dto.getName());
        profile.setUsername(dto.getUsername());
        profile.setPassword(bCryptPasswordEncoder.encode(dto.getPassword()));
        profile.setStatus(GeneralStatus.IN_REGISTRATION);
        profile.setCreatedDate(LocalDateTime.now());
        profile.setVisible(true);
        profile.setLang(lang);
        profileRepository.save(profile);

        //insert role
        profileRoleService.create(profile.getId(), ProfileRole.ROLE_USER);
        //send email or SMS
        try {
            if (PhoneUtil.isPhone(dto.getUsername())) {
                smsSendService.sendRegistrationSms(dto.getUsername(), lang);
            } else if (EmailUtil.isEmail(dto.getUsername())) {
                sendingEmailService.sendRegistrationEmail(dto.getUsername(), profile.getId(), lang);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("xatolik");
        }

        // send message
        return new AppResponse<>(resourceBundleService.getMessage("email.confirm.send", lang));
    }

    // 2. VERIFICATION
    public AppResponse<String> registrationEmailVerification(String token, AppLanguages lang) {
        // tokendan id-ni ajratib olish jarayoni boladi va uni keyin har doimgiday ishlov berib ketaveramiz
        try {
            Integer profileId = JwtUtil.decodeRegVer(token);
            ProfileEntity profile = profileService.findById(profileId);
            AppLanguages lang1 = profile.getLang();
            if (profile.getStatus().equals(GeneralStatus.IN_REGISTRATION)) {
                profileRepository.changeStatus(profileId, GeneralStatus.ACTIVE);
                return new AppResponse<>(resourceBundleService.getMessage("reg.verification.success", lang1));
                // return "Successfully registered";
            }
        } catch (JwtException e) {
            e.printStackTrace();
        }
        throw new AppBadException(resourceBundleService.getMessage("reg.verification.failed", lang));
        //throw new AppBadException("Verification failed");
    }

    public ProfileDTO login(AuthDTO authDTO, AppLanguages lang) {
        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleIsTrue(authDTO.getUsername());
        if (optional.isEmpty()) {
            throw new AppBadException(resourceBundleService.getMessage("username.password.incorrect", lang));
        }
        ProfileEntity profile = optional.get();
        if (!bCryptPasswordEncoder.matches(authDTO.getPassword(), profile.getPassword())) {
            throw new AppBadException(resourceBundleService.getMessage("username.password.incorrect", lang));
        }
        if (!profile.getStatus().equals(GeneralStatus.ACTIVE)) {
            throw new AppBadException(resourceBundleService.getMessage("status.error", lang));
        }

        ProfileDTO response = new ProfileDTO();
        response.setName(profile.getName());
        response.setUsername(profile.getUsername());
        response.setRoleList(profileRoleRepository.getAllRolesListByProfileId(profile.getId()));
        response.setJwt(JwtUtil.encode(profile.getUsername(), profile.getId(), response.getRoleList()));
        return response;
    }

    public ProfileDTO registrationSmsVerification(SmsVerificationDTO dto, AppLanguages lang) {
        //998915699980
        //12345
        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleIsTrue(dto.getPhone());
        if (optional.isEmpty()) {
            throw new AppBadException(resourceBundleService.getMessage("reg.verification.failed", lang));
        }
        ProfileEntity profile = optional.get();
        if (!profile.getStatus().equals(GeneralStatus.IN_REGISTRATION)) {
            throw new AppBadException(resourceBundleService.getMessage("reg.verification.failed", lang));
        }
        //code check
        smsHistoryService.checkSmsTimeIsValid(dto.getPhone(), dto.getCode(), lang);
        //ACTIVE
        profileRepository.changeStatus(profile.getId(), GeneralStatus.ACTIVE);
        return getLogInResponse(profile);
    }

    public AppResponse<String> registrationSmsVerificationResend(@Valid SmsResendDTO dto, AppLanguages lang) {
        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleIsTrue(dto.getPhone());
        if (optional.isEmpty()) {
            throw new AppBadException(resourceBundleService.getMessage("reg.verification.failed", lang));
        }
        ProfileEntity profile = optional.get();
        if (!profile.getStatus().equals(GeneralStatus.IN_REGISTRATION)) {
            throw new AppBadException(resourceBundleService.getMessage("reg.verification.failed", lang));
        }
        //resend sms
        try {
            smsSendService.sendRegistrationSms(dto.getPhone(), lang);
            return new AppResponse<>(resourceBundleService.getMessage("sms.resend", lang));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new AppBadException(resourceBundleService.getMessage("sms.resend.failed", lang));
        }

    }

    public AppResponse<String> resetPassword(@Valid ResetPasswordDTO dto, AppLanguages lang) {
        // checking
        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleIsTrue(dto.getUsername());
        if (optional.isEmpty()) {
            throw new AppBadException(resourceBundleService.getMessage("profile.not.found", lang));
        }
        ProfileEntity profile = optional.get();
        if (!profile.getStatus().equals(GeneralStatus.ACTIVE)) {
            throw new AppBadException(resourceBundleService.getMessage("reg.verification.failed", lang));
        }
        // send
        if (PhoneUtil.isPhone(dto.getUsername())) {
            try {
                smsSendService.sendResetPasswordSms(dto.getUsername(), lang);
            }catch (JsonProcessingException e) {
                e.printStackTrace();
                System.out.println("sms sending failed");
            }
        }
        if (EmailUtil.isEmail(dto.getUsername())) {
            sendingEmailService.sendResetPasswordEmail(dto.getUsername(), lang);
        }
        String responseMessage = resourceBundleService.getMessage("reset.confirm.message", lang);
        return new AppResponse<>(String.format(responseMessage, dto.getUsername()));
    }

    public AppResponse<String> resetPasswordConfirm(@Valid ResetPasswordConfirmDTO dto, AppLanguages lang) {
        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleIsTrue(dto.getUsername());
        if (optional.isEmpty()) {
            throw new AppBadException(resourceBundleService.getMessage("reg.verification.failed", lang));
        }
        ProfileEntity profile = optional.get();
        if (!profile.getStatus().equals(GeneralStatus.ACTIVE)) {
            throw new AppBadException(resourceBundleService.getMessage("status.error", lang));
        }
        if(PhoneUtil.isPhone(dto.getUsername())) {
            smsHistoryService.checkSmsTimeIsValid(dto.getUsername(), dto.getConfirmCode(), lang);
        }if (EmailUtil.isEmail(dto.getUsername())) {
            emailHistoryService.checkEmailTimeIsValid(dto.getUsername(), dto.getConfirmCode(), lang);
        }
        // passwordni update qilish
        String username = dto.getUsername();
        profileRepository.updatePassword(username, bCryptPasswordEncoder.encode(dto.getPassword()));

        return new AppResponse<>(resourceBundleService.getMessage("reset.password.success", lang));
    }


    public ProfileDTO getLogInResponse(ProfileEntity profile) {
        ProfileDTO response = new ProfileDTO();
        response.setName(profile.getName());
        response.setUsername(profile.getUsername());
        response.setRoleList(profileRoleRepository.getAllRolesListByProfileId(profile.getId()));
        response.setJwt(JwtUtil.encode(profile.getUsername(), profile.getId(), response.getRoleList()));
        return response;
    }
}
