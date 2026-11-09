package gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class GameVisualizer extends JPanel {
    private static final int WORLD_WIDTH = 800;
    private static final int WORLD_HEIGHT = 600;

    private static final double MOVE_SPEED = 2.8;
    private static final double TURN_SPEED = 0.055;

    private static final int ROBOT_COLLISION_RADIUS = 15;
    private static final int ROBOT_LENGTH = 30;
    private static final int ROBOT_WIDTH = 10;

    private volatile double m_robotPositionX = 100;
    private volatile double m_robotPositionY = 100;
    private volatile double m_robotDirection = 0;

    private volatile boolean forwardPressed;
    private volatile boolean backwardPressed;
    private volatile boolean leftPressed;
    private volatile boolean rightPressed;

    private final List<Rectangle> obstacles = new ArrayList<>();
    private final Timer updateTimer;
    private Window focusWindow;

    private final WindowAdapter windowFocusListener = new WindowAdapter() {
        @Override
        public void windowLostFocus(WindowEvent e) {
            clearPressedKeys();
        }
    };

    public GameVisualizer() {
        setFocusable(true);
        setDoubleBuffered(true);
        installKeyBindings();

        updateTimer = new Timer(10, e -> onModelUpdateEvent());
        updateTimer.setCoalesce(true);
    }

    private void installKeyBindings() {
        bindKey("pressed W", "forward-pressed", () -> forwardPressed = true);
        bindKey("released W", "forward-released", () -> forwardPressed = false);
        bindKey("pressed UP", "up-pressed", () -> forwardPressed = true);
        bindKey("released UP", "up-released", () -> forwardPressed = false);

        bindKey("pressed S", "backward-pressed", () -> backwardPressed = true);
        bindKey("released S", "backward-released", () -> backwardPressed = false);
        bindKey("pressed DOWN", "down-pressed", () -> backwardPressed = true);
        bindKey("released DOWN", "down-released", () -> backwardPressed = false);

        bindKey("pressed A", "left-pressed", () -> leftPressed = true);
        bindKey("released A", "left-released", () -> leftPressed = false);
        bindKey("pressed LEFT", "left-arrow-pressed", () -> leftPressed = true);
        bindKey("released LEFT", "left-arrow-released", () -> leftPressed = false);

        bindKey("pressed D", "right-pressed", () -> rightPressed = true);
        bindKey("released D", "right-released", () -> rightPressed = false);
        bindKey("pressed RIGHT", "right-arrow-pressed", () -> rightPressed = true);
        bindKey("released RIGHT", "right-arrow-released", () -> rightPressed = false);
    }

    private void bindKey(String keyStroke, String actionName, Runnable action) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(keyStroke), actionName);
        getActionMap().put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        updateTimer.start();
        focusWindow = SwingUtilities.getWindowAncestor(this);
        if (focusWindow != null) {
            focusWindow.addWindowFocusListener(windowFocusListener);
        }
    }

    @Override
    public void removeNotify() {
        updateTimer.stop();
        clearPressedKeys();
        if (focusWindow != null) {
            focusWindow.removeWindowFocusListener(windowFocusListener);
            focusWindow = null;
        }
        super.removeNotify();
    }

    public synchronized void addObstacle(Rectangle obstacle) {
        if (obstacle == null || obstacle.width <= 0 || obstacle.height <= 0) {
            return;
        }
        obstacles.add(new Rectangle(obstacle));
        repaint();
    }

    public synchronized void clearObstacles() {
        obstacles.clear();
        repaint();
    }

    public synchronized List<Rectangle> getObstacles() {
        List<Rectangle> copy = new ArrayList<>();
        for (Rectangle obstacle : obstacles) {
            copy.add(new Rectangle(obstacle));
        }
        return copy;
    }

    double getRobotPositionX() {
        return m_robotPositionX;
    }

    double getRobotPositionY() {
        return m_robotPositionY;
    }

    double getRobotDirection() {
        return m_robotDirection;
    }

    protected void onRedrawEvent() {
        EventQueue.invokeLater(this::repaint);
    }

    protected void onModelUpdateEvent() {
        if (leftPressed != rightPressed) {
            m_robotDirection = normalizeAngle(
                    m_robotDirection + (leftPressed ? -TURN_SPEED : TURN_SPEED));
        }

        double movement = 0;
        if (forwardPressed) {
            movement += MOVE_SPEED;
        }
        if (backwardPressed) {
            movement -= MOVE_SPEED;
        }

        if (movement != 0) {
            double dx = Math.cos(m_robotDirection) * movement;
            double dy = Math.sin(m_robotDirection) * movement;
            moveWithCollision(dx, dy);
        }

        repaint();
    }

    private void moveWithCollision(double dx, double dy) {
        double nextX = m_robotPositionX + dx;
        if (isPositionFree(nextX, m_robotPositionY)) {
            m_robotPositionX = nextX;
        }

        double nextY = m_robotPositionY + dy;
        if (isPositionFree(m_robotPositionX, nextY)) {
            m_robotPositionY = nextY;
        }
    }

    private boolean isPositionFree(double x, double y) {
        if (x < ROBOT_COLLISION_RADIUS || y < ROBOT_COLLISION_RADIUS
                || x > WORLD_WIDTH - ROBOT_COLLISION_RADIUS
                || y > WORLD_HEIGHT - ROBOT_COLLISION_RADIUS) {
            return false;
        }

        synchronized (this) {
            for (Rectangle obstacle : obstacles) {
                Rectangle2D expanded = new Rectangle2D.Double(
                        obstacle.x - ROBOT_COLLISION_RADIUS,
                        obstacle.y - ROBOT_COLLISION_RADIUS,
                        obstacle.width + ROBOT_COLLISION_RADIUS * 2.0,
                        obstacle.height + ROBOT_COLLISION_RADIUS * 2.0);
                if (expanded.contains(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void clearPressedKeys() {
        forwardPressed = false;
        backwardPressed = false;
        leftPressed = false;
        rightPressed = false;
    }

    private static double normalizeAngle(double angle) {
        double fullTurn = Math.PI * 2;
        angle %= fullTurn;
        return angle < 0 ? angle + fullTurn : angle;
    }

    private static int round(double value) {
        return (int) Math.round(value);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        double scale = Math.min(
                (double) getWidth() / WORLD_WIDTH,
                (double) getHeight() / WORLD_HEIGHT);
        double offsetX = (getWidth() - WORLD_WIDTH * scale) / 2.0;
        double offsetY = (getHeight() - WORLD_HEIGHT * scale) / 2.0;

        Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.translate(offsetX, offsetY);
            g2d.scale(scale, scale);
            drawObstacles(g2d);
            drawRobot(g2d, round(m_robotPositionX),
                    round(m_robotPositionY), m_robotDirection);
        } finally {
            g2d.dispose();
        }
    }

    private void drawObstacles(Graphics2D g) {
        List<Rectangle> snapshot = getObstacles();
        g.setColor(new Color(90, 90, 90));
        for (Rectangle obstacle : snapshot) {
            g.fillRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
        }
        g.setColor(Color.BLACK);
        for (Rectangle obstacle : snapshot) {
            g.drawRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
        }
    }

    private static void fillOval(Graphics g, int centerX, int centerY,
                                 int diameterX, int diameterY) {
        g.fillOval(centerX - diameterX / 2, centerY - diameterY / 2,
                diameterX, diameterY);
    }

    private static void drawOval(Graphics g, int centerX, int centerY,
                                 int diameterX, int diameterY) {
        g.drawOval(centerX - diameterX / 2, centerY - diameterY / 2,
                diameterX, diameterY);
    }

    private void drawRobot(Graphics2D g, int x, int y, double direction) {
        Graphics2D robotGraphics = (Graphics2D) g.create();
        try {
            robotGraphics.rotate(direction, x, y);
            robotGraphics.setColor(Color.MAGENTA);
            fillOval(robotGraphics, x, y, ROBOT_LENGTH, ROBOT_WIDTH);
            robotGraphics.setColor(Color.BLACK);
            drawOval(robotGraphics, x, y, ROBOT_LENGTH, ROBOT_WIDTH);
            robotGraphics.setColor(Color.WHITE);
            fillOval(robotGraphics, x + 10, y, 5, 5);
            robotGraphics.setColor(Color.BLACK);
            drawOval(robotGraphics, x + 10, y, 5, 5);
        } finally {
            robotGraphics.dispose();
        }
    }
}
