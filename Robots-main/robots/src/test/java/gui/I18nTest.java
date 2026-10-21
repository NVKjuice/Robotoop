package gui;

import org.junit.jupiter.api.Test;
import java.util.Locale;
import java.util.ResourceBundle;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class I18nTest {

    //Проверка, что ключи в ru и en файлах совпадают
    @Test
    void testKeysAreIdenticalInBothLocales() {
        ResourceBundle ru = ResourceBundle.getBundle("I18n.Messages", new Locale("ru"));
        ResourceBundle en = ResourceBundle.getBundle("I18n.Messages", Locale.ENGLISH);

        assertEquals(ru.keySet(), en.keySet(), 
            "Ошибка: набор ключей в русском и английском файлах не совпадает!");
    }

    //Проверка, что переключается язык в классе I18n
    @Test
    void testLocaleSwitching() {
        // Включаем русский
        I18n.setLocale(new Locale("ru"));
        assertEquals("Режим отображения", I18n.get("menu.view"), 
            "Русский перевод для menu.view не совпадает");

        // Включаем английский
        I18n.setLocale(Locale.ENGLISH);
        assertEquals("View Mode", I18n.get("menu.view"), 
            "Английский перевод для menu.view не совпадает");
    }
    
    // ТЕСТ 3: Проверка, что I18n не падает при запросе ключа
    @Test
    void testGetReturnsNonNull() {
        I18n.setLocale(Locale.ENGLISH);
        assertNotNull(I18n.get("window.game.title"), "Значение ключа не должно быть null");
    }
}