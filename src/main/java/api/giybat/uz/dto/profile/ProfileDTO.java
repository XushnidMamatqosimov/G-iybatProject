package api.giybat.uz.dto.profile;

import api.giybat.uz.dto.AttachDTO;
import api.giybat.uz.enums.ProfileRole;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class ProfileDTO {
    private String name;
    private String username;
    private AttachDTO photo;
    private List<ProfileRole> roleList;
    private String jwt;

}
