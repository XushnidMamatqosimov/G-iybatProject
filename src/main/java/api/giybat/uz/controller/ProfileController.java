package api.giybat.uz.controller;

import api.giybat.uz.dto.AppResponse;
import api.giybat.uz.dto.PasswordUpdateDTO;
import api.giybat.uz.dto.ProfilePhotoUpdateDTO;
import api.giybat.uz.dto.profile.ProfileDetailUpdateDTO;
import api.giybat.uz.dto.profile.ProfileUpdateUsernameDTO;
import api.giybat.uz.dto.profile.ProfileUsernameConfirmDTO;
import api.giybat.uz.enums.AppLanguages;
import api.giybat.uz.service.ProfileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@Tag(name = "ProfileController", description = "Api set for working with ProfileController")
public class ProfileController {
    @Autowired
    private ProfileService profileService;

    @PutMapping("/detail")
    @Operation(summary = "Create profile", description = "Apis for creating Profiles")
    public ResponseEntity<AppResponse<String>> create(@Valid @RequestBody ProfileDetailUpdateDTO dto,
                                                      @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguages language) {
        AppResponse<String> stringAppResponse = profileService.updateDetail(dto, language);
        return ResponseEntity.ok(stringAppResponse);
    }

    @PutMapping("/pswdUpdate")
    @Operation(summary = "Update profile Password", description = "Update Profile Password")
    public ResponseEntity<AppResponse<String>> updatePswd(@Valid @RequestBody PasswordUpdateDTO dto,
                                                          @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguages language) {
        AppResponse<String> stringAppResponse = profileService.updatePswd(dto, language);
        return ResponseEntity.ok(stringAppResponse);

    }

    @PutMapping("/username")
    @Operation(summary = "Update username", description = "apis for updating username")
    public ResponseEntity<AppResponse<String>> updateUsername(@Valid @RequestBody ProfileUpdateUsernameDTO dto,
                                                          @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguages language) throws JsonProcessingException {
        AppResponse<String> stringAppResponse = profileService.updateUsername(dto, language);
        return ResponseEntity.ok(stringAppResponse);

    }

    @PutMapping("/username/confirm")
    @Operation(summary = "update username confirm", description = "Update username confirm")
    public ResponseEntity<AppResponse<String>> updateUsernameConfirm(@Valid @RequestBody ProfileUsernameConfirmDTO dto,
                                                              @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguages language) throws JsonProcessingException {
        AppResponse<String> stringAppResponse = profileService.updateUsernameConfirm(dto, language);
        return ResponseEntity.ok(stringAppResponse);

    }

    @PutMapping("/photo")
    @Operation(summary = "Update photo", description = "api for updating profile photo")
    public ResponseEntity<AppResponse<String>> update(@Valid @RequestBody ProfilePhotoUpdateDTO dto,
                                                      @RequestHeader (value = "Accep-Language", defaultValue = "UZ") AppLanguages language) throws JsonProcessingException {
        AppResponse<String> response = profileService.updatePhoto(dto.getPhotoId(), language);
        return ResponseEntity.ok(response);

    }
}
