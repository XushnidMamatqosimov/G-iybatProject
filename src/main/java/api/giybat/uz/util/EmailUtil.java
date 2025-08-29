package api.giybat.uz.util;

import java.util.regex.Pattern;

public class EmailUtil {
    public static boolean isEmail(String email) {
        String emailRegex = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
        return Pattern.matches(emailRegex, email);
    }
}
