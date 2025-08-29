package api.giybat.uz;

import api.giybat.uz.enums.AppLanguages;
import api.giybat.uz.enums.SmsType;
import api.giybat.uz.service.SmsSendService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationTests {

    @Autowired
    private SmsSendService smsSendService;

    @Test
    void contextLoads() throws JsonProcessingException {
       smsSendService.sendRegistrationSms("998915930412", AppLanguages.EN);
    }

}
