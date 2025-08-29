package api.giybat.uz.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordUpdateDTO {
    @NotBlank(message = "Old Password is required")
    private String oldPassword;
    @NotBlank(message = "New Password is required")
    private String newPassword;
}
