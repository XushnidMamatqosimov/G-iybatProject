package api.giybat.uz.service;

import api.giybat.uz.entity.EmailHistoryEntity;
import api.giybat.uz.enums.AppLanguages;
import api.giybat.uz.enums.SmsType;
import api.giybat.uz.exp.AppBadException;
import api.giybat.uz.repository.EmailHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EmailHistoryService {
    @Autowired
    private EmailHistoryRepository emailHistoryRepository;
    @Autowired
    private ResourceBundleService resourceBundleService;
    private final Integer attemptsCount = 3;

    public void saveSmsToDB(String email, String code, SmsType emailType) {
        EmailHistoryEntity entity = new EmailHistoryEntity();
        entity.setEmail(email);
        entity.setAttempts(0);
        entity.setCode(code);
        entity.setSmsType(emailType);
        entity.setCreatedDate(LocalDateTime.now());
        emailHistoryRepository.save(entity);
    }

    public Long smsCountByEmail(String email) {
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusMinutes(1);
        return emailHistoryRepository.countSmsByEmailAndCreatedDateBetween(email, from, to);

    }

    public void checkEmailTimeIsValid(String email, String code, AppLanguages lang) throws AppBadException {
        //findLastSmsByPhone
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusMinutes(1);
        Optional<?> lastSmsByEmail = emailHistoryRepository.findTop1ByEmailOrderByCreatedDateDesc(email);
        if (lastSmsByEmail.isEmpty()) {
            throw new AppBadException(resourceBundleService.getMessage("reg.verification.failed", lang));
        }
        //attempt Count
        EmailHistoryEntity emailHistoryEntity = (EmailHistoryEntity) lastSmsByEmail.get();
        if (emailHistoryEntity.getAttempts() >= attemptsCount) {
            throw new AppBadException(resourceBundleService.getMessage("attempts", lang));
        }
        //check code
        if (!emailHistoryEntity.getCode().equals(code)) {
            emailHistoryRepository.updateAttemptCount(emailHistoryEntity.getId());
            throw new AppBadException(resourceBundleService.getMessage("reg.verification.failed", lang));
        }
        // check time
        LocalDateTime expDate = emailHistoryEntity.getCreatedDate().plusMinutes(2);
        if (LocalDateTime.now().isAfter(expDate)) {
            throw new AppBadException(resourceBundleService.getMessage("reg.verification.failed", lang));
        }
    }
}
