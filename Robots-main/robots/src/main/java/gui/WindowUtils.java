package gui;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

public class WindowUtils {

    public static void addCloseConfirmation(JInternalFrame frame) {
        frame.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);

        frame.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                if (ConfirmExitDialog.show(frame)) {
                    frame.dispose();
                }
            }
        });
    }
    // тоже самое для главного окна
    public static void addMainWindowCloseConfirmation(javax.swing.JFrame frame) {
        frame.setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (ConfirmExitDialog.show(frame)) {
                    System.exit(0);
                }
            }
        });
    }
}