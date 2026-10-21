package gui;

import org.junit.jupiter.api.Test;
import javax.swing.JOptionPane;
import static org.junit.jupiter.api.Assertions.*;

class ConfirmExitDialogTest {

    @Test
    void testIsConfirmedWhenYes() {
        assertTrue(ConfirmExitDialog.isConfirmed(JOptionPane.YES_OPTION));
    }

    @Test
    void testIsNotConfirmedWhenNo() {
        assertFalse(ConfirmExitDialog.isConfirmed(JOptionPane.NO_OPTION));
    }

    @Test
    void testIsNotConfirmedWhenClosed() {
        assertFalse(ConfirmExitDialog.isConfirmed(JOptionPane.CLOSED_OPTION));
    }
}