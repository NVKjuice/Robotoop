package gui;

import java.awt.Frame;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JInternalFrame;

public class ProfileManager {
    
    private static File getProfileDirectory() {
        String testDirectory = System.getProperty("robots.profiles.dir");
        if (testDirectory != null) {
            return new File(testDirectory);
        }
        return new File(System.getProperty("user.home"), ".robots/profiles");
    }
    

    public static List<String> getProfiles() {
        List<String> profiles = new ArrayList<>();
        File directory = getProfileDirectory();

        if (!directory.exists()) {
            return profiles;
        }

        File[] files = directory.listFiles((dir, name) -> name.endsWith(".properties"));

        if (files != null) {
            for (File file : files) {
                profiles.add(file.getName().replace(".properties", ""));
            }
        }

        return profiles;
    }

    private static File getProfileFile(String name) {
        if (name == null || !name.matches("[a-zA-Z0-9_-]+")) {
            throw new IllegalArgumentException("Некорректное имя профиля");
        }

        return new File(getProfileDirectory(), name + ".properties");
    }

    public static Properties load(String name) {
        Properties properties = new Properties();

        File file = getProfileFile(name);

        if (!file.exists()) {
            return properties;
        }

        try (FileInputStream input =
                     new FileInputStream(file)) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties;
    }

    public static void save(String name, MainApplicationFrame frame) {
        File directory = getProfileDirectory();

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalStateException("Не удалось создать каталог профилей");
        }
        Properties p = new Properties();

        p.setProperty("locale", I18n.getLocale().getLanguage());

        Rectangle bounds = frame.getProfileBounds();

        p.setProperty("main.x", String.valueOf(bounds.x));
        p.setProperty("main.y", String.valueOf(bounds.y));
        p.setProperty("main.width", String.valueOf(bounds.width));
        p.setProperty("main.height", String.valueOf(bounds.height));

        p.setProperty("main.maximized", String.valueOf((frame.getExtendedState() & Frame.MAXIMIZED_BOTH) != 0));

        saveInternalWindow(p, "log", frame.getLogWindow());
        saveInternalWindow(p, "game", frame.getGameWindow());

        saveProperties(name, p);
    }

    private static void saveInternalWindow(Properties p, String prefix, JInternalFrame window) {

        Rectangle bounds = (window.isMaximum() || window.isIcon())
                ? window.getNormalBounds() : window.getBounds();
        if (bounds == null || bounds.width <= 0 || bounds.height <= 0) {
            bounds = window.getBounds();
        }

        p.setProperty(prefix + ".visible", String.valueOf(window.isVisible()));
        p.setProperty(prefix + ".x", String.valueOf(bounds.x));
        p.setProperty(prefix + ".y", String.valueOf(bounds.y));
        p.setProperty(prefix + ".width", String.valueOf(bounds.width));
        p.setProperty(prefix + ".height", String.valueOf(bounds.height));
        p.setProperty(prefix + ".icon", String.valueOf(window.isIcon()));
        p.setProperty(prefix + ".maximized", String.valueOf(window.isMaximum()));
    }

    public static void restore(Properties p, MainApplicationFrame frame) {
        String language = p.getProperty("locale");
        if (language != null) {
            I18n.setLocale("ru".equals(language)
                    ? new java.util.Locale("ru") : java.util.Locale.ENGLISH);
        }

        int x = getInt(p, "main.x", 50);
        int y = getInt(p, "main.y", 50);
        int width = getInt(p, "main.width", 1000);
        int height = getInt(p, "main.height", 700);

        frame.setExtendedState(Frame.NORMAL);
        frame.setProfileBounds(new Rectangle(x, y, Math.max(1, width), Math.max(1, height)));

        if (Boolean.parseBoolean(p.getProperty("main.maximized", "false"))) {
            frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        }

        restoreInternalWindow(p, "log", frame.getLogWindow());
        restoreInternalWindow(p, "game", frame.getGameWindow());

        frame.updateLocalization();
    }

    private static void restoreInternalWindow(Properties p, String prefix, JInternalFrame window) {
        int x = getInt(p, prefix + ".x", 10);
        int y = getInt(p, prefix + ".y", 10);
        int width = getInt(p, prefix + ".width", 300);
        int height = getInt(p, prefix + ".height", 400);
        boolean maximized = Boolean.parseBoolean(p.getProperty(prefix + ".maximized", "false"));
        boolean iconified = Boolean.parseBoolean(p.getProperty(prefix + ".icon", "false"));
        boolean visible = Boolean.parseBoolean(p.getProperty(prefix + ".visible", "true"));

        try {
            // Clear transient states before restoring the saved normal bounds.
            if (window.isIcon()) {
                window.setIcon(false);
            }
            if (window.isMaximum()) {
                window.setMaximum(false);
            }
            window.setBounds(x, y, Math.max(1, width), Math.max(1, height));
            window.setVisible(visible);

            // Apply states only after normal bounds have been restored.
            if (maximized) {
                window.setMaximum(true);
            }
            if (iconified) {
                window.setIcon(true);
            }
        } catch (java.beans.PropertyVetoException e) {
            throw new IllegalStateException("Не удалось восстановить состояние окна: " + prefix, e);
        }
    }

    static void saveProperties(String name, Properties properties) {
        File directory = getProfileDirectory();

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalStateException("Не удалось создать каталог профилей");
        }

        try (FileOutputStream output = new FileOutputStream(getProfileFile(name))) {
            properties.store(output, "Robots application profile");
        } catch (IOException e) {
            throw new RuntimeException("Ошибка сохранения профиля", e);
        }
    }

    private static int getInt(Properties p, String key, int defaultValue) {

        try {
            return Integer.parseInt(p.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}