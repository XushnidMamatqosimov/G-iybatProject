package api.giybat.uz.service;

import api.giybat.uz.entity.ProfileRoleEntity;
import api.giybat.uz.enums.ProfileRole;
import api.giybat.uz.repository.ProfileRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ProfileRoleService {
    @Autowired
    private ProfileRoleRepository profileRoleRepository;

    public void create(Integer profileId, ProfileRole role) {
        ProfileRoleEntity roles = new ProfileRoleEntity();
        roles.setProfileId(profileId);
        roles.setRoles(role);
        roles.setCreatedDate(LocalDateTime.now());
        profileRoleRepository.save(roles);
    }

    public void deleteRoles(Integer profileId) {
        profileRoleRepository.deleteByProfileId(profileId);
    }
}
