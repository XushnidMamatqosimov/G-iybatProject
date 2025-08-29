package api.giybat.uz.controller;

import api.giybat.uz.dto.AppResponse;
import api.giybat.uz.dto.PasswordUpdateDTO;
import api.giybat.uz.dto.profile.ProfileDetailUpdateDTO;
import api.giybat.uz.dto.profile.ProfileUpdateUsernameDTO;
import api.giybat.uz.dto.profile.ProfileUsernameConfirmDTO;
import api.giybat.uz.enums.AppLanguages;
import api.giybat.uz.service.ProfileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
public class ProfileController {
    @Autowired
    private ProfileService profileService;

    @PutMapping("/detail")
    public ResponseEntity<AppResponse<String>> create(@Valid @RequestBody ProfileDetailUpdateDTO dto,
                                                      @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguages language) {
        AppResponse<String> stringAppResponse = profileService.updateDetail(dto, language);
        return ResponseEntity.ok(stringAppResponse);
    }

    @PutMapping("/pswdUpdate")
    public ResponseEntity<AppResponse<String>> updatePswd(@Valid @RequestBody PasswordUpdateDTO dto,
                                                          @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguages language) {
        AppResponse<String> stringAppResponse = profileService.updatePswd(dto, language);
        return ResponseEntity.ok(stringAppResponse);

    }

    @PutMapping("/username")
    public ResponseEntity<AppResponse<String>> updateUsername(@Valid @RequestBody ProfileUpdateUsernameDTO dto,
                                                          @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguages language) throws JsonProcessingException {
        AppResponse<String> stringAppResponse = profileService.updateUsername(dto, language);
        return ResponseEntity.ok(stringAppResponse);

    }

    @PutMapping("/username/confirm")
    public ResponseEntity<AppResponse<String>> updateUsernameConfirm(@Valid @RequestBody ProfileUsernameConfirmDTO dto,
                                                              @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguages language) throws JsonProcessingException {
        AppResponse<String> stringAppResponse = profileService.updateUsernameConfirm(dto, language);
        return ResponseEntity.ok(stringAppResponse);

    }
}
