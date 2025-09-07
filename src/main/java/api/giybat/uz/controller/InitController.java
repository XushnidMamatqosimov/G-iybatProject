package api.giybat.uz.controller;

import api.giybat.uz.entity.ProfileEntity;
import api.giybat.uz.enums.GeneralStatus;
import api.giybat.uz.enums.ProfileRole;
import api.giybat.uz.repository.ProfileRepository;
import api.giybat.uz.service.ProfileRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/init")
public class InitController {
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private ProfileRoleService profileRoleService;

    @GetMapping("/all")
    public String updateDetail() {
        Optional<ProfileEntity> exists = profileRepository.findByUsernameAndVisibleIsTrue("adminjon@gmail.com");
        if (exists.isPresent()) {
            return "Present";
        }
        ProfileEntity profileEntity = new ProfileEntity();
        profileEntity.setName("Adminjon");
        profileEntity.setUsername("adminjon@gmail.com");
        profileEntity.setVisible(true);
        profileEntity.setPassword(bCryptPasswordEncoder.encode("1234"));
        profileEntity.setStatus(GeneralStatus.ACTIVE);
        profileEntity.setCreatedDate(LocalDateTime.now());
        profileRepository.save(profileEntity);
        profileRoleService.create(profileEntity.getId(), ProfileRole.ROLE_ADMIN);
        profileRoleService.create(profileEntity.getId(), ProfileRole.ROLE_USER);
        return "Success";

    }
}
