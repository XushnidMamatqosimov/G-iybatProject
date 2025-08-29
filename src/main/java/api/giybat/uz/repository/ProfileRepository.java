package api.giybat.uz.repository;

import api.giybat.uz.entity.ProfileEntity;
import api.giybat.uz.enums.GeneralStatus;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ProfileRepository extends CrudRepository<ProfileEntity, Integer> {
    Optional<ProfileEntity> findByUsernameAndVisibleIsTrue(String username);
    Optional<ProfileEntity> findByIdAndVisibleIsTrue(Integer id);

    @Modifying
    @Transactional
    @Query("update ProfileEntity set status = ?2 where id = ?1")
    void changeStatus(Integer profileId, GeneralStatus status);

    @Modifying
    @Transactional
    @Query("update ProfileEntity set password = ?2 where username = ?1")
    void updatePassword(String username, String newPassword);

    @Modifying
    @Transactional
    @Query("update ProfileEntity set name =?1 where id = ?2")
    void changeName(@NotBlank(message = "Name required") String name, Integer profileId);

    @Modifying
    @Transactional
    @Query("update ProfileEntity set password = ?2 where id = ?1")
    void changePassword(@NotBlank(message = "Password required") String id, String newPassword);

    @Modifying
    @Transactional
    @Query("update ProfileEntity set tempUsername = ?2 where id = ?1")
    void updateTempUsername(Integer id, String tempUsername);

    @Modifying
    @Transactional
    @Query("update ProfileEntity set username = ?2 where id = ?1")
    void updateUsername(Integer id, String username);
}
