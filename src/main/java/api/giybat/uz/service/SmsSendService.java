package api.giybat.uz.service;

import api.giybat.uz.dto.sms.SmsAuthDTO;
import api.giybat.uz.dto.sms.SmsAuthResponseDTO;
import api.giybat.uz.dto.sms.SmsRequestDTO;
import api.giybat.uz.dto.sms.SmsSendResponseDTO;
import api.giybat.uz.entity.SmsProviderTokenHolderEntity;
import api.giybat.uz.enums.AppLanguages;
import api.giybat.uz.enums.SmsType;
import api.giybat.uz.exp.AppBadException;
import api.giybat.uz.repository.SmsProviderTokenHolderRepository;
import api.giybat.uz.util.RandomUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class SmsSendService {
    @Autowired
    private RestTemplate restTemplate;
    @Value("${eskiz.url}")
    private String eskizURL;
    @Value("${eskiz.login}")
    private String smsEmail;
    @Value("${eskiz.password}")
    private String smsPassword;
    private Integer smsLimit = 3;

    @Autowired
    private SmsProviderTokenHolderRepository smsProviderTokenHolderRepository;
    @Autowired
    private SmsHistoryService smsHistoryService;
    @Autowired
    private ResourceBundleService resourceBundleService;

    public void sendRegistrationSms(String phoneNumber, AppLanguages lang) throws JsonProcessingException {
        String randomSMSCode = RandomUtil.getRandomSMSCode();
        String message = resourceBundleService.getMessage("sms.confirm.message", lang);
        //"Bu Eskiz dan test";
        message = String.format(message, randomSMSCode);
        SmsSendResponseDTO smsSendResponseDTO = sendSms(phoneNumber, message, randomSMSCode, SmsType.REGISTRATION);
        System.out.println(smsSendResponseDTO);
    }

    public void sendResetPasswordSms(String phoneNumber, AppLanguages lang) throws JsonProcessingException {
        String randomSMSCode = RandomUtil.getRandomSMSCode();
        String message = resourceBundleService.getMessage("sms.reset.password.confirm", lang);
        message = String.format(message, randomSMSCode);
        sendSms(phoneNumber, message, randomSMSCode, SmsType.RESET_PASSWORD);

    }

    public void sendUsernameChangeConfirmSms(String phoneNumber, AppLanguages lang) throws JsonProcessingException {
        String code = RandomUtil.getRandomSMSCode();
        String message = resourceBundleService.getMessage("reset.confirm.message", lang);
        message = String.format(message, phoneNumber, code);
        sendSms(phoneNumber, message, code, SmsType.CHANGE_USERNAME_CONFIRM);
    }

    public SmsSendResponseDTO sendSms(String phoneNumber, String message, String code, SmsType smsType) throws JsonProcessingException {
        // check
        Long count = smsHistoryService.smsCountByPhone(phoneNumber);
        if (count >= smsLimit) {
            System.out.println("Sms Limit reached: " + phoneNumber);
            log.warn("Sms Limit reached: phone:{}",  phoneNumber);
            throw new AppBadException("SmsLimit exceeded");
        }
        //send
        SmsSendResponseDTO result = sendSms(phoneNumber, message);
        // save to db
        smsHistoryService.saveSmsToDB(phoneNumber, message, code, smsType);
        return result;
    }

    private SmsSendResponseDTO sendSms(String phoneNumber, String message) throws JsonProcessingException {
        // getToken
        String token = getToken();
        // send sms
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + token);
        // body
        SmsRequestDTO smsRequestDTO = new SmsRequestDTO();
        smsRequestDTO.setMobile_phone(phoneNumber);
        smsRequestDTO.setMessage(message);
        smsRequestDTO.setFrom("4546");
        // send request
        HttpEntity<SmsRequestDTO> entity = new HttpEntity<>(smsRequestDTO, headers);
        try {
            ResponseEntity<SmsSendResponseDTO> response = restTemplate.exchange(
                    eskizURL + "/message/sms/send",
                    HttpMethod.POST,
                    entity,
                    SmsSendResponseDTO.class);
            return response.getBody();
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("There is some problem with sending SMS: message: {}, phone: {}", e.getMessage(), phoneNumber);
            throw new RuntimeException("Error sending SMS");
        }
    }

    private String getToken() throws JsonProcessingException {
        Optional<SmsProviderTokenHolderEntity> optional = smsProviderTokenHolderRepository.findTop1By();
        if (optional.isEmpty()) {  // if token not exists
            String tokenFromProvider = getTokenFromProvider();
            SmsProviderTokenHolderEntity entity = new SmsProviderTokenHolderEntity();
            entity.setToken(tokenFromProvider);
            entity.setCreatedDate(LocalDateTime.now());
            entity.setExpDate(LocalDateTime.now().plusMonths(1));
            smsProviderTokenHolderRepository.save(entity);
            return tokenFromProvider;
        }
        // if exists check usable
        SmsProviderTokenHolderEntity entity = optional.get();
        LocalDateTime expDate = entity.getCreatedDate().plusMonths(1);
        if (!expDate.isBefore(LocalDateTime.now())) {
            return entity.getToken();
        }
        // update token, get new token and update it
        String token = getTokenFromProvider();
        entity.setToken(token);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setExpDate(LocalDateTime.now().plusMonths(1));
        smsProviderTokenHolderRepository.save(entity);
        return token;


    }

    private String getTokenFromProvider() throws JsonProcessingException {
        SmsAuthDTO smsAuthDTO = new SmsAuthDTO();
        smsAuthDTO.setEmail(smsEmail);
        smsAuthDTO.setPassword(smsPassword);


        // JsonNode bilan implementatsiya qilingani
        /*String response = restTemplate.postForObject(eskizURL + "/auth/login", smsAuthDTO, String.class);
        try {
            JsonNode parent = new ObjectMapper().readTree(response);
            String token = parent.get("data").get("token").asText();
            System.out.println(token);
            return token;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }*/

        try {
            System.out.println("----SMSSender new token was takenm----");
            SmsAuthResponseDTO response = restTemplate.postForObject(eskizURL + "/auth/login", smsAuthDTO, SmsAuthResponseDTO.class);
            System.out.println(response.getData().getToken());
            return response.getData().getToken();
        } catch (RuntimeException e) {
            log.error("Get token from provider failed: error: {}, ", e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
