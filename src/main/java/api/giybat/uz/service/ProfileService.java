package api.giybat.uz.service;

import api.giybat.uz.dto.AppResponse;
import api.giybat.uz.dto.PasswordUpdateDTO;
import api.giybat.uz.dto.profile.ProfileDetailUpdateDTO;
import api.giybat.uz.dto.profile.ProfileUpdateUsernameDTO;
import api.giybat.uz.dto.profile.ProfileUsernameConfirmDTO;
import api.giybat.uz.entity.ProfileEntity;
import api.giybat.uz.enums.AppLanguages;
import api.giybat.uz.enums.ProfileRole;
import api.giybat.uz.exp.AppBadException;
import api.giybat.uz.repository.ProfileRepository;
import api.giybat.uz.repository.ProfileRoleRepository;
import api.giybat.uz.util.EmailUtil;
import api.giybat.uz.util.JwtUtil;
import api.giybat.uz.util.PhoneUtil;
import api.giybat.uz.util.SpringSecurityUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@Service
public class ProfileService {
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private ResourceBundleService resourceBundleService;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private SmsSendService smsSendService;
    @Autowired
    private SendingEmailService sendingEmailService;
    @Autowired
    private SmsHistoryService smsHistoryService;
    @Autowired
    private EmailHistoryService emailHistoryService;
    @Autowired
    private ProfileRoleRepository profileRoleRepository;
    @Autowired
    private AttachService attachService;


    public AppResponse<String> updateDetail(ProfileDetailUpdateDTO dto, AppLanguages language) {
        Integer currentUserId = SpringSecurityUtil.getCurrentUserId();
        profileRepository.changeName(dto.getName(), currentUserId);

       /* // 2 - usul
        ProfileEntity profile = findById(currentUserId);
        profile.setName(dto.getName());
        profileRepository.save(profile);*/

        return new AppResponse<>(resourceBundleService.getMessage("profile.detail.update.success", language));
    }

    public AppResponse<String> updatePswd(@Valid @RequestBody PasswordUpdateDTO dto, AppLanguages language) {
        Integer currentUserId = SpringSecurityUtil.getCurrentUserId();
        ProfileEntity currentProfile = findById(currentUserId);

        if (!bCryptPasswordEncoder.matches(dto.getOldPassword(), currentProfile.getPassword())) {
            return new AppResponse<>(resourceBundleService.getMessage("username.password.incorrect", language));
        }
        profileRepository.changePassword(String.valueOf(currentUserId), bCryptPasswordEncoder.encode(dto.getNewPassword()));
        return new AppResponse<>(resourceBundleService.getMessage("reset.password.success", language));

    }

    public ProfileEntity findById(Integer profileId) {
        return profileRepository.findById(profileId).orElseThrow(() -> new AppBadException("Profile not found"));
    }


    public AppResponse<String> updateUsername(@Valid ProfileUpdateUsernameDTO dto, AppLanguages language) throws JsonProcessingException {
        //check
        Optional<ProfileEntity> optional = profileRepository.findByUsernameAndVisibleIsTrue(dto.getUsername());
        if (optional.isPresent()) {
            throw new AppBadException(resourceBundleService.getMessage("username.exists", language));
        }
        //send
        if (PhoneUtil.isPhone(dto.getUsername())) {
            smsSendService.sendUsernameChangeConfirmSms(dto.getUsername(), language);
        } else if (EmailUtil.isEmail(dto.getUsername())) {
            sendingEmailService.sendChangeUsernameEmail(dto.getUsername(), language);
        }
        //save
        Integer currentUserId = SpringSecurityUtil.getCurrentUserId();
        ProfileEntity currentProfile = findById(currentUserId);
        currentProfile.setTempUsername(dto.getUsername());
        profileRepository.updateTempUsername(currentUserId, dto.getUsername());

        String responseText = resourceBundleService.getMessage("reset.confirm.message", language);
        return new AppResponse<>(String.format(responseText, dto.getUsername()));
    }

    public AppResponse<String> updateUsernameConfirm(@Valid ProfileUsernameConfirmDTO dto, AppLanguages language) {
        // check
        Integer currentUserId = SpringSecurityUtil.getCurrentUserId();
        ProfileEntity currentProfile = findById(currentUserId);
        String tempUsername = currentProfile.getTempUsername();
        if (PhoneUtil.isPhone(tempUsername)) {
            smsHistoryService.checkSmsTimeIsValid(tempUsername, dto.getCode(), language);
        } else if (EmailUtil.isEmail(tempUsername)) {
            emailHistoryService.checkEmailTimeIsValid(tempUsername, dto.getCode(), language);
        }
        // update username
        profileRepository.updateUsername(currentUserId, tempUsername);
        //response

        List<ProfileRole> roleList = profileRoleRepository.getAllRolesListByProfileId(currentUserId);
        String jwt = JwtUtil.encode(tempUsername, currentProfile.getId(), roleList);
        return new AppResponse<>(jwt, resourceBundleService.getMessage("change.username.success", language));
    }

    public AppResponse<String> updatePhoto( @Valid String photoId, AppLanguages language) {
        Integer currentUserId = SpringSecurityUtil.getCurrentUserId();
        ProfileEntity currentProfile = findById(currentUserId);
        profileRepository.updatePhoto(currentProfile.getId(), photoId);
        if (currentProfile.getPhotoId() != null && !currentProfile.getPhotoId().equals(photoId)) {
            attachService.delete(currentProfile.getPhotoId());
        }
        return new AppResponse<>(resourceBundleService.getMessage("photo.update.success", language));
    }
}
