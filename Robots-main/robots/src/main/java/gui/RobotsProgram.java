package gui;

import java.util.List;
import java.util.Properties;
import java.util.Locale;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class RobotsProgram {

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {

            List<String> profiles = ProfileManager.getProfiles();

            Properties selectedProfile = null;
            String profileName = null;
            boolean restore = false;

            if (!profiles.isEmpty()) {

                JComboBox<String> profileSelector =
                        new JComboBox<>(profiles.toArray(new String[0]));

                int selection = JOptionPane.showConfirmDialog(
                        null,
                        profileSelector,
                        I18n.get("profile.select.title"),
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (selection == JOptionPane.OK_OPTION) {

                    profileName = (String) profileSelector.getSelectedItem();

                    if (profileName != null) {
                        selectedProfile = ProfileManager.load(profileName);
                        String language = selectedProfile.getProperty("locale", "ru");

                        I18n.setLocale(language.equals("ru")? new Locale("ru") : Locale.ENGLISH);
                        int answer = JOptionPane.showConfirmDialog(
                                null,
                                I18n.get("profile.restore.message"),
                                I18n.get("profile.restore.title"),
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE
                        );

                        restore = answer == JOptionPane.YES_OPTION;
                    }
                }
            }

            MainApplicationFrame frame = new MainApplicationFrame(selectedProfile, profileName, restore);
            frame.setVisible(true);
        });
    }
}
