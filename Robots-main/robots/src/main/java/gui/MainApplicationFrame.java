package gui;

import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.Properties;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import log.Logger;

/**
 * Что требуется сделать:
 * 1. Метод создания меню перегружен функционалом и трудно читается. 
 * Следует разделить его на серию более простых методов (или вообще выделить отдельный класс).
 *
 */
public class MainApplicationFrame extends JFrame
{
    private final JDesktopPane desktopPane = new JDesktopPane();
    private LogWindow logWindow;   
    private GameWindow gameWindow;
    private String activeProfile;
    private Rectangle normalBounds;
    
    public MainApplicationFrame(Properties profile, String profileName, boolean restoreProfile) {
        
        this.activeProfile = profileName;

        int inset = 50;        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
            screenSize.width  - inset*2,
            screenSize.height - inset*2);
        normalBounds = new Rectangle(getBounds());
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent event) {
                rememberNormalBounds();
            }

            @Override
            public void componentResized(ComponentEvent event) {
                rememberNormalBounds();
            }
        });

        setContentPane(desktopPane);
        
        
        logWindow = createLogWindow();
        addWindow(logWindow);

        gameWindow = new GameWindow();
        gameWindow.setSize(400,  400);
        addWindow(gameWindow);

        setJMenuBar(generateMenuBar());

        if (restoreProfile && profile != null) {
            ProfileManager.restore(profile, this);
        } else {
            updateWindowTitles();
        }
        Logger.debug(I18n.get("log.works"));

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override 
            public void windowClosing(java.awt.event.WindowEvent e) {
                exitApplication();
            }
        });
        
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }
    
    public LogWindow getLogWindow() {
        return logWindow;
    }

    public GameWindow getGameWindow() {
        return gameWindow;
    }

    /** Returns the last non-maximized bounds for reliable profile persistence. */
    public Rectangle getProfileBounds() {
        return new Rectangle(normalBounds == null ? getBounds() : normalBounds);
    }

    public void setProfileBounds(Rectangle bounds) {
        if (bounds == null) {
            return;
        }
        normalBounds = new Rectangle(bounds);
        setBounds(normalBounds);
    }

    private void rememberNormalBounds() {
        if (getExtendedState() == Frame.NORMAL
                && getWidth() > 0 && getHeight() > 0) {
            normalBounds = new Rectangle(getBounds());
        }
    }

    public void updateLocalization() {
        updateMenuBar();
        updateWindowTitles();

        revalidate();
        repaint();
    }

    protected LogWindow createLogWindow()
    {
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setLocation(10,10);
        logWindow.setSize(300, 800);
        setMinimumSize(logWindow.getSize());
        logWindow.pack();
        return logWindow;
    }
    
    protected void addWindow(JInternalFrame frame)
    {
        desktopPane.add(frame);
        frame.setVisible(true);
    }
    
//    protected JMenuBar createMenuBar() {
//        JMenuBar menuBar = new JMenuBar();
// 
//        //Set up the lone menu.
//        JMenu menu = new JMenu("Document");
//        menu.setMnemonic(KeyEvent.VK_D);
//        menuBar.add(menu);
// 
//        //Set up the first menu item.
//        JMenuItem menuItem = new JMenuItem("New");
//        menuItem.setMnemonic(KeyEvent.VK_N);
//        menuItem.setAccelerator(KeyStroke.getKeyStroke(
//                KeyEvent.VK_N, ActionEvent.ALT_MASK));
//        menuItem.setActionCommand("new");
////        menuItem.addActionListener(this);
//        menu.add(menuItem);
// 
//        //Set up the second menu item.
//        menuItem = new JMenuItem("Quit");
//        menuItem.setMnemonic(KeyEvent.VK_Q);
//        menuItem.setAccelerator(KeyStroke.getKeyStroke(
//                KeyEvent.VK_Q, ActionEvent.ALT_MASK));
//        menuItem.setActionCommand("quit");
////        menuItem.addActionListener(this);
//        menu.add(menuItem);
// 
//        return menuBar;
//    }
    
    private JMenu createMenu(String titleKey, int mnemonic, String descriptionKey) {
        JMenu menu = new JMenu(I18n.get(titleKey));
        menu.setMnemonic(mnemonic);
        if(descriptionKey != null) {
            menu.getAccessibleContext().setAccessibleDescription(I18n.get(descriptionKey));
        }

        return menu;
    }

    private JMenuItem createMenuItem(String titleKey, int mnemonic, ActionListener action) {
        JMenuItem item = new JMenuItem(I18n.get(titleKey));
        item.setMnemonic(mnemonic);
        item.addActionListener(action);

        return item;
    }

    private JMenuBar generateMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();

        menuBar.add(createLookAndFeelMenu());
        menuBar.add(createTestMenu());
        menuBar.add(createLanguageMenu());
        menuBar.add(createExitMenuItem());

        return menuBar;
    }

    private JMenuItem createExitMenuItem() {
        return createMenuItem("menu.exit", KeyEvent.VK_X, event -> exitApplication());
    }

    private JMenu createLookAndFeelMenu()
    {
        JMenu menu = createMenu("menu.view", KeyEvent.VK_V, "menu.view.desc");
        menu.add(createMenuItem("menu.view.desc", KeyEvent.VK_S,
            event -> setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName()
            )
        ));
        menu.add(createMenuItem("menu.view.crossplatform", KeyEvent.VK_C,
            event -> setLookAndFeel(
                UIManager.getCrossPlatformLookAndFeelClassName()
            )
        ));

        return menu;
    }

    private JMenu createTestMenu()
    {
        JMenu menu = createMenu("menu.tests", KeyEvent.VK_T, "menu.tests.desc");
        menu.add(createMenuItem("menu.tests.log", KeyEvent.VK_S,
            event -> Logger.debug(
                I18n.get("log.new.line")
            )
        ));

        return menu;
    }

    private JMenu createLanguageMenu() {
        JMenu menu = createMenu("menu.language", KeyEvent.VK_L, null);
        menu.add(createMenuItem("menu.language.ru", KeyEvent.VK_R, event -> changeLanguage(new Locale("ru"))));
        menu.add(createMenuItem("menu.language.en", KeyEvent.VK_E, event -> changeLanguage(Locale.ENGLISH)));

        return menu;
    }

    private void changeLanguage(Locale locale) {
        I18n.setLocale(locale);
        updateLocalization();
    }

    private void updateMenuBar() {
        setJMenuBar(generateMenuBar());
    }

    private void updateWindowTitles() {
        if (logWindow != null) {
            logWindow.setTitle(I18n.get("window.log.title"));
        }

        if (gameWindow != null) {
            gameWindow.setTitle(I18n.get("window.game.title"));
        }
    }

    private void exitApplication() {
        if (!ConfirmExitDialog.show(this)) {
            return;
        }

        String profileName = (String) JOptionPane.showInputDialog(
            this,
            I18n.get("profile.save.message"),
            I18n.get("profile.save.title"),
            JOptionPane.QUESTION_MESSAGE,
            null,
            null,
            activeProfile == null ? "default" : activeProfile
        );

        if (profileName == null) {
            return;
        }

        profileName = profileName.trim();

        if (!profileName.matches("[a-zA-Z0-9_-]+")) {
            JOptionPane.showMessageDialog(
                    this,
                    I18n.get("profile.invalid"),
                    I18n.get("profile.save.title"),
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try {
            ProfileManager.save(profileName, this);
            System.exit(0);
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    I18n.get("profile.save.title"),
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void setLookAndFeel(String className)
    {
        try
        {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        }
        catch (ClassNotFoundException | InstantiationException
            | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
            // just ignore
        }
    }
}