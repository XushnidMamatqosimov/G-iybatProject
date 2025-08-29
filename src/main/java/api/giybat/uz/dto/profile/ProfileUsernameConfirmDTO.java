package api.giybat.uz.dto.profile;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUsernameConfirmDTO {
    @NotBlank(message = "Code is required")
    private String code;
}
