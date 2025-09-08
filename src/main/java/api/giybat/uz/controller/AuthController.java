package api.giybat.uz.controller;

import api.giybat.uz.dto.AppResponse;
import api.giybat.uz.dto.auth.AuthDTO;
import api.giybat.uz.dto.profile.ProfileDTO;
import api.giybat.uz.dto.auth.RegistrationDTO;
import api.giybat.uz.dto.auth.ResetPasswordConfirmDTO;
import api.giybat.uz.dto.auth.ResetPasswordDTO;
import api.giybat.uz.dto.sms.SmsResendDTO;
import api.giybat.uz.dto.sms.SmsVerificationDTO;
import api.giybat.uz.enums.AppLanguages;
import api.giybat.uz.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "AuthController", description = "Controller for Authorization and Authentication")
public class AuthController {
    @Autowired
    private AuthService authService;

    // Registration
    @PostMapping("/registration")
    @Operation(summary = "Profile registration", description = "Api used for registration")
    public ResponseEntity<AppResponse<String>> registration(@Valid @RequestBody RegistrationDTO dto,
                                                            @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguages lang) {
        return ResponseEntity.ok().body(authService.registration(dto, lang));
    }

    @GetMapping("/registration/email-verification/{token}")
    @Operation(summary = "Profile verification via Email", description = "api for email verification")
    public ResponseEntity<?> emailVerification(@PathVariable("token") String token,
                                               @RequestParam(value = "lang", defaultValue = "UZ") AppLanguages lang) {
        return ResponseEntity.ok().body(authService.registrationEmailVerification(token, lang));
    }

    @PostMapping("/registration/sms-verification")
    @Operation(summary = "sms verification", description = "api for sms verification")
    public ResponseEntity<?> smsVerification(@Valid @RequestBody SmsVerificationDTO dto,
                                             @RequestParam("lang") AppLanguages lang) {
        return ResponseEntity.ok().body(authService.registrationSmsVerification(dto, lang));
    }

    @PostMapping("/registration/sms-verification-resend")
    @Operation(summary = "sms verification Resend", description = "api for sms resend verification")
    public ResponseEntity<AppResponse<String>> smsVerificationResend(@Valid @RequestBody SmsResendDTO dto,
                                             @RequestParam("lang") AppLanguages lang) {
        return ResponseEntity.ok().body(authService.registrationSmsVerificationResend(dto, lang));
    }


    // Login
    @PostMapping("/login")
    @Operation(summary = "login", description = "login")
    public ResponseEntity<ProfileDTO> login(@Valid @RequestBody AuthDTO authDTO,
                                            @RequestHeader("Accept-Language") AppLanguages lang) {
        return ResponseEntity.ok().body(authService.login(authDTO, lang));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "reset Password", description = "Apis for reset password")
    public ResponseEntity<AppResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO,
                                            @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguages lang) {
        return ResponseEntity.ok().body(authService.resetPassword(resetPasswordDTO, lang));
    }

    @PostMapping("/reset-password-confirm")
    @Operation(summary = "resetPasswordConfirm", description = "reset Password confirm Apis")
    public ResponseEntity<AppResponse<String>> resetPasswordConfirm(@Valid @RequestBody ResetPasswordConfirmDTO dto,
                                                             @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguages lang) {
        return ResponseEntity.ok().body(authService.resetPasswordConfirm(dto, lang));
    }


}
