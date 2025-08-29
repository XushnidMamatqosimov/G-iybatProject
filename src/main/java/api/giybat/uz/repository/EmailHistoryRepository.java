package api.giybat.uz.repository;

import api.giybat.uz.entity.EmailHistoryEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailHistoryRepository extends CrudRepository<EmailHistoryEntity, String> {
    // select count(*) from sms_history where email = ? and created_date between ? and ?
    Long countSmsByEmailAndCreatedDateBetween(String email, LocalDateTime from, LocalDateTime to);

    //Optional<?> findLastSmsByPhoneAndCreatedDateBetween(String phone, LocalDateTime from, LocalDateTime to);

    Optional<?> findTop1ByEmailOrderByCreatedDateDesc(String email);

    @Modifying
    @Transactional
    @Query("update EmailHistoryEntity set attempts = coalesce( attempts, 0) + 1 where id = ?1")
    void updateAttemptCount(String id);
}
