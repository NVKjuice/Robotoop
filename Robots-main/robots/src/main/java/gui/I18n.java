package gui;

import java.util.Locale;
import java.util.ResourceBundle;

public final class I18n {

    private static final String BUNDLE_NAME = "i18n.Messages";

    private static Locale currentLocale = getSystemLocale();
    private static ResourceBundle bundle =
            ResourceBundle.getBundle(BUNDLE_NAME, currentLocale);

    private I18n() {
    }

    private static Locale getSystemLocale() {
        Locale systemLocale = Locale.getDefault();

        if ("ru".equals(systemLocale.getLanguage())) {
            return new Locale("ru");
        }

        return Locale.ENGLISH;
    }

    public static String get(String key) {
        return bundle.getString(key);
    }

    public static Locale getLocale() {
        return currentLocale;
    }

    public static void setLocale(Locale locale) {
        if (locale == null) {
            return;
        }

        if ("ru".equals(locale.getLanguage())) {
            currentLocale = new Locale("ru");
        } else {
            currentLocale = Locale.ENGLISH;
        }

        bundle = ResourceBundle.getBundle(
                BUNDLE_NAME,
                currentLocale,
                ResourceBundle.Control.getNoFallbackControl(
                        ResourceBundle.Control.FORMAT_DEFAULT
                )
        );
    }
}

