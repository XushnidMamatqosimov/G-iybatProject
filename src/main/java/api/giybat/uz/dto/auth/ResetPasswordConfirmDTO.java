package api.giybat.uz.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordConfirmDTO {
    @NotBlank(message = "Username required")
    private String username;
    @NotBlank(message = "Confirm Code required")
    private String confirmCode;
    @NotBlank(message = "Password required")
    private String password;
}
