package gui;

import javax.swing.JOptionPane;
import java.awt.Component;

public class ConfirmExitDialog {

    public static boolean show(Component parent) {
        int result = JOptionPane.showConfirmDialog(
                parent,
                I18n.get("dialog.exit.message"),
                I18n.get("dialog.exit.title"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return isConfirmed(result);
    }

    public static boolean isConfirmed(int result) {
        return result == JOptionPane.YES_OPTION;
    }
}