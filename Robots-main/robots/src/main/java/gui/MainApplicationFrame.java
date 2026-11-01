package gui;

import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.RenderingHints.Key;
import java.awt.event.KeyEvent;
import java.util.Locale;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
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
    
    public MainApplicationFrame() {
        //Make the big window be indented 50 pixels from each edge
        //of the screen.
        int inset = 50;        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
            screenSize.width  - inset*2,
            screenSize.height - inset*2);

        setContentPane(desktopPane);
        
        
        logWindow = createLogWindow();
        addWindow(logWindow);

        gameWindow = new GameWindow();
        gameWindow.setSize(400,  400);
        addWindow(gameWindow);

        setJMenuBar(generateMenuBar());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (ConfirmExitDialog.show(MainApplicationFrame.this)) {
                    System.exit(0);
                }
            }
        });
    }
    
    protected LogWindow createLogWindow()
    {
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setLocation(10,10);
        logWindow.setSize(300, 800);
        setMinimumSize(logWindow.getSize());
        logWindow.pack();
        Logger.debug(I18n.get("log.works"));
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
        
        return createMenuItem("menu.exit", KeyEvent.VK_X,
            event -> { if(ConfirmExitDialog.show(this)) System.exit(0); }
        );
    }

    private JMenu createLookAndFeelMenu()
    {
        JMenu menu = createMenu("menu.view", KeyEvent.VK_V, "menu.view.desc");
        JMenuItem systemItem = new JMenuItem(I18n.get("menu.view.system"), KeyEvent.VK_S);
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

        updateMenuBar();
        updateWindowTitles();

        revalidate();
        repaint();
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