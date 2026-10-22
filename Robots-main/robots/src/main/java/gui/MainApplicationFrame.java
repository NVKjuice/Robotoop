package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
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

        WindowUtils.addMainWindowCloseConfirmation(this);
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
    
    private JMenuBar generateMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();

        menuBar.add(createLookAndFeelMenu());
        menuBar.add(createTestMenu());
        menuBar.add(createLanguageMenu());
        menuBar.add(createExitMenuItem());

        return menuBar;
    }
    // пункт меню
    private JMenuItem createExitMenuItem() {
        JMenuItem exitButton = new JMenuItem(I18n.get("menu.exit"));
        exitButton.setMnemonic(KeyEvent.VK_X);
        exitButton.addActionListener((event) -> {
            if (ConfirmExitDialog.show(this)) {
                System.exit(0);
            }
        });
        return exitButton;
    }

    private JMenu createLookAndFeelMenu()
    {
        JMenu menu = new JMenu(I18n.get("menu.view"));
        menu.setMnemonic(KeyEvent.VK_V);
        menu.getAccessibleContext().setAccessibleDescription(I18n.get(
            "menu.view.desc"));

        JMenuItem systemItem = new JMenuItem(I18n.get("menu.view.system"), KeyEvent.VK_S);
        systemItem.addActionListener((event) -> {
            setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            this.invalidate();
        });
        menu.add(systemItem);

        JMenuItem crossplatformItem =
            new JMenuItem(I18n.get("menu.view.crossplatform"), KeyEvent.VK_S);
        crossplatformItem.addActionListener((event) -> {
            setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            this.invalidate();
        });
        menu.add(crossplatformItem);

        return menu;
    }

    private JMenu createTestMenu()
    {
        JMenu menu = new JMenu(I18n.get("menu.tests"));
        menu.setMnemonic(KeyEvent.VK_T);
        menu.getAccessibleContext().setAccessibleDescription(I18n.get(
            "menu.tests.desc"));

        JMenuItem addLogMessageItem =
            new JMenuItem(I18n.get("menu.tests.log"), KeyEvent.VK_S);
        addLogMessageItem.addActionListener((event) -> {
            Logger.debug(I18n.get("log.new.line"));
        });
        menu.add(addLogMessageItem);

        return menu;
    }

    private JMenu createLanguageMenu() {
        JMenu menu = new JMenu(I18n.get("menu.language"));
        menu.setMnemonic(KeyEvent.VK_L);

        JMenuItem ruItem = new JMenuItem(I18n.get("menu.language.ru"));
        ruItem.addActionListener(e -> changeLanguage(new Locale("ru")));
        menu.add(ruItem);

        JMenuItem enItem = new JMenuItem(I18n.get("menu.language.en"));
        enItem.addActionListener(e -> changeLanguage(Locale.ENGLISH));
        menu.add(enItem);

        return menu;
    }

    private void changeLanguage(Locale locale) 
    {
        I18n.setLocale(locale);
        setJMenuBar(generateMenuBar());

        if (logWindow != null) 
        {
            logWindow.setTitle(I18n.get("window.log.title"));
        }
        if (gameWindow != null) 
            {
            gameWindow.setTitle(I18n.get("window.game.title"));
        }

        SwingUtilities.updateComponentTreeUI(this);
        revalidate();
        repaint();
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