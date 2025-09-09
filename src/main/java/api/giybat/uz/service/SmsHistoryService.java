package api.giybat.uz.service;

import api.giybat.uz.entity.SmsHistoryEntity;
import api.giybat.uz.enums.AppLanguages;
import api.giybat.uz.enums.SmsType;
import api.giybat.uz.exp.AppBadException;
import api.giybat.uz.repository.SmsHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class SmsHistoryService {
    @Autowired
    private SmsHistoryRepository smsHistoryRepository;
    @Autowired
    private ResourceBundleService resourceBundleService;

    public void saveSmsToDB(String phone, String message, String code, SmsType smsType) {
        SmsHistoryEntity smsHistoryEntity = new SmsHistoryEntity();
        smsHistoryEntity.setPhone(phone);
        smsHistoryEntity.setMessage(message);
        smsHistoryEntity.setCode(code);
        smsHistoryEntity.setSmsType(smsType);
        smsHistoryEntity.setCreatedDate(LocalDateTime.now());
        smsHistoryRepository.save(smsHistoryEntity);
    }

    public Long smsCountByPhone(String phone) {
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusMinutes(1);
        return smsHistoryRepository.countSmsByPhoneAndCreatedDateBetween(phone, from, to);

    }

    public void checkSmsTimeIsValid(String phone, String code, AppLanguages lang) throws AppBadException {
        //findLastSmsByPhone
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusMinutes(1);
        Optional<?> lastSmsByPhone = smsHistoryRepository.findTop1ByPhoneOrderByCreatedDateDesc(phone);
        if (lastSmsByPhone.isEmpty()) {
            throw new AppBadException(resourceBundleService.getMessage("reg.verification.failed", lang));
        }
        //attempt Count
        SmsHistoryEntity smsHistoryEntity = (SmsHistoryEntity) lastSmsByPhone.get();
        if (smsHistoryEntity.getAttemptCount()>=3){
            log.warn("Attempt count limit reached: {}", phone);
            throw new AppBadException(resourceBundleService.getMessage("attempts", lang));
        }
        //check code
        if (!smsHistoryEntity.getCode().equals(code)) {
            smsHistoryRepository.updateAttemptCount(smsHistoryEntity.getId());
            throw new AppBadException(resourceBundleService.getMessage("reg.verification.failed", lang));
        }
        // check time
        LocalDateTime expDate = smsHistoryEntity.getCreatedDate().plusMinutes(2);
        if (LocalDateTime.now().isAfter(expDate)) {
            log.warn("Sms time is expired: {}", phone);
            throw new AppBadException(resourceBundleService.getMessage("reg.verification.failed", lang));
        }
    }
}
