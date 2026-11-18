package gui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class ProfileManagerTest {

    @TempDir
    Path tempDir;

    private String previousDirectory;

    @BeforeEach
    void setUp() {
        previousDirectory = System.getProperty("robots.profiles.dir");
        System.setProperty("robots.profiles.dir", tempDir.toString());
    }

    @AfterEach
    void tearDown() {
        if (previousDirectory == null) {
            System.clearProperty("robots.profiles.dir");
        } else {
            System.setProperty("robots.profiles.dir", previousDirectory);
        }
    }

    //Проверяем сохранение профиля
    @Test
    void profileIsSavedToFile() {

        Properties properties = new Properties();

        properties.setProperty("locale", "ru");
        properties.setProperty("main.x", "100");
        properties.setProperty("main.y", "200");

        ProfileManager.saveProperties("work", properties);

        File file = tempDir.resolve("work.properties").toFile();
        assertTrue(file.exists(),"Файл профиля не был создан");
    }

    //Проверяем загрузку профиля
    @Test
    void savedProfileCanBeLoaded() {

        Properties original = new Properties();

        original.setProperty("locale", "en");
        original.setProperty("main.x", "150");
        original.setProperty("main.y", "250");

        ProfileManager.saveProperties("work", original);

        Properties loaded = ProfileManager.load("work");

        assertEquals("en", loaded.getProperty("locale"));
        assertEquals("150", loaded.getProperty("main.x"));
        assertEquals("250", loaded.getProperty("main.y"));
    }

    //Проверяем поддержку нескольких профилей
    @Test
    void multipleProfilesCanBeSaved() {

        Properties first = new Properties();
        first.setProperty("locale", "ru");

        Properties second = new Properties();
        second.setProperty("locale", "en");

        ProfileManager.saveProperties("work", first);
        ProfileManager.saveProperties("gaming", second);

        assertTrue(tempDir.resolve("work.properties").toFile().exists());
        assertTrue(tempDir.resolve("gaming.properties").toFile().exists());
        assertEquals("ru", ProfileManager.load("work").getProperty("locale"));
        assertEquals("en", ProfileManager.load("gaming").getProperty("locale"));
    }

    //Проверяем получение списка профилей
    @Test
    void getProfilesReturnsSavedProfiles() {

        Properties properties = new Properties();

        ProfileManager.saveProperties("work", properties);
        ProfileManager.saveProperties("gaming", properties);

        List<String> profiles = ProfileManager.getProfiles();

        assertEquals(2, profiles.size());
        assertTrue(profiles.contains("work"));
        assertTrue(profiles.contains("gaming"));
    }

    //Проверяем загрузку несуществующего профиля
    @Test
    void loadingNonExistingProfileReturnsEmptyProperties() {
        Properties properties = ProfileManager.load("unknown");
        assertNotNull(properties);
        assertTrue(properties.isEmpty());
    }

    //Проверяем отклонение некорректных имён
    @Test
    void invalidProfileNamesAreRejected() {

        assertThrows(IllegalArgumentException.class,
                () -> ProfileManager.saveProperties( "../work", new Properties())
        );

        assertThrows(IllegalArgumentException.class,
            () -> ProfileManager.saveProperties(null, new Properties())
        );
    }

    //Проверяем сохранение локализации
    @Test
    void profilePreservesSelectedLocale() {

        Properties properties = new Properties();
        properties.setProperty("locale", "ru");
        ProfileManager.saveProperties("russian", properties);

        Properties loaded = ProfileManager.load("russian");
        assertEquals("ru", loaded.getProperty("locale"));
    }

    //Проверяем перезапись существующего профиля
    @Test
    void existingProfileCanBeUpdated() {

        Properties first = new Properties();
        first.setProperty("locale", "ru");

        ProfileManager.saveProperties("work", first);

        Properties second = new Properties();
        second.setProperty("locale", "en");

        ProfileManager.saveProperties("work", second);

        Properties loaded = ProfileManager.load("work");

        assertEquals("en", loaded.getProperty("locale"));
        assertEquals(1, ProfileManager.getProfiles().size());
    }
}