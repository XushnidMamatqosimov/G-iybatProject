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
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    // Registration
    @PostMapping("/registration")
    public ResponseEntity<AppResponse<String>> registration(@Valid @RequestBody RegistrationDTO dto,
                                                            @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguages lang) {
        return ResponseEntity.ok().body(authService.registration(dto, lang));
    }

    @GetMapping("/registration/email-verification/{token}")
    public ResponseEntity<?> emailVerification(@PathVariable("token") String token,
                                               @RequestParam(value = "lang", defaultValue = "UZ") AppLanguages lang) {
        return ResponseEntity.ok().body(authService.registrationEmailVerification(token, lang));
    }

    @PostMapping("/registration/sms-verification")
    public ResponseEntity<?> smsVerification(@Valid @RequestBody SmsVerificationDTO dto,
                                             @RequestParam("lang") AppLanguages lang) {
        return ResponseEntity.ok().body(authService.registrationSmsVerification(dto, lang));
    }

    @PostMapping("/registration/sms-verification-resend")
    public ResponseEntity<AppResponse<String>> smsVerificationResend(@Valid @RequestBody SmsResendDTO dto,
                                             @RequestParam("lang") AppLanguages lang) {
        return ResponseEntity.ok().body(authService.registrationSmsVerificationResend(dto, lang));
    }


    // Login
    @PostMapping("/login")
    public ResponseEntity<ProfileDTO> login(@Valid @RequestBody AuthDTO authDTO,
                                            @RequestHeader("Accept-Language") AppLanguages lang) {
        return ResponseEntity.ok().body(authService.login(authDTO, lang));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AppResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO,
                                            @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguages lang) {
        return ResponseEntity.ok().body(authService.resetPassword(resetPasswordDTO, lang));
    }

    @PostMapping("/reset-password-confirm")
    public ResponseEntity<AppResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordConfirmDTO dto,
                                                             @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguages lang) {
        return ResponseEntity.ok().body(authService.resetPasswordConfirm(dto, lang));
    }


}
