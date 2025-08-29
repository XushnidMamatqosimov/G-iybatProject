package api.giybat.uz.dto.sms;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsVerificationDTO {
    @NotBlank(message = "phone is required")
    private String phone;
    @NotBlank(message = "code is required")
    private String code;
}
