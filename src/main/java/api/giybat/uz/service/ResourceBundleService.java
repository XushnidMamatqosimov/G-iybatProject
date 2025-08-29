package api.giybat.uz.service;

import api.giybat.uz.enums.AppLanguages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class ResourceBundleService {
    @Autowired
    private ResourceBundleMessageSource messageSource;

    public String getMessage(String code, AppLanguages lang) {
        return messageSource.getMessage(code, null, new Locale(lang.name()));

    }

}
