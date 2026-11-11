package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class GameVisualizer extends JPanel {
    private static final int WORLD_WIDTH = 800;
    private static final int WORLD_HEIGHT = 600;
    private static final int WALL_THICKNESS = 2;

    private static final double MOVE_SPEED = 2.8;
    private static final double TURN_SPEED = 0.055;

    private static final int ROBOT_COLLISION_RADIUS = 6;
    private static final int ROBOT_LENGTH = 30;
    private static final int ROBOT_WIDTH = 10;

    private static final int COIN_RADIUS = 8;
    private static final double COIN_PICKUP_DISTANCE =
            ROBOT_COLLISION_RADIUS + COIN_RADIUS;

    private volatile double m_robotPositionX = 100;
    private volatile double m_robotPositionY = 100;
    private volatile double m_robotDirection = 0;

    private volatile boolean forwardPressed;
    private volatile boolean backwardPressed;
    private volatile boolean leftPressed;
    private volatile boolean rightPressed;

    private final List<Rectangle> obstacles = new ArrayList<>();
    private final List<Point> coins = new ArrayList<>();

    private int score;

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
        obstacles.addAll(GameMap.createWalls());
        initializeCoins();

        updateTimer = new Timer(10, e -> onModelUpdateEvent());
        updateTimer.setCoalesce(true);
    }

    private void initializeCoins() {
        for (int i = 0; i < 7; i++) {
            spawnCoin();
        }
    }

    private void spawnCoin() {
        int minX = WALL_THICKNESS + COIN_RADIUS;
        int maxX = WORLD_WIDTH - WALL_THICKNESS - COIN_RADIUS;
        int minY = WALL_THICKNESS + COIN_RADIUS;
        int maxY = WORLD_HEIGHT - WALL_THICKNESS - COIN_RADIUS;

        for (int attempt = 0; attempt < 500; attempt++) {
            int x = ThreadLocalRandom.current().nextInt(minX, maxX + 1);
            int y = ThreadLocalRandom.current().nextInt(minY, maxY + 1);

            if (!isCoinPositionFree(x, y)) {
                continue;
            }

            coins.add(new Point(x, y));
            return;
        }
    }

    private boolean isCoinPositionFree(int x, int y) {
        if (distance(x, y, m_robotPositionX, m_robotPositionY)
                < ROBOT_COLLISION_RADIUS + COIN_RADIUS + 10) {
            return false;
        }

        for (Point coin : coins) {
            if (distance(x, y, coin.x, coin.y) < COIN_RADIUS * 3) {
                return false;
            }
        }

        for (Rectangle obstacle : obstacles) {
            Rectangle2D expanded = new Rectangle2D.Double(
                    obstacle.x - COIN_RADIUS,
                    obstacle.y - COIN_RADIUS,
                    obstacle.width + COIN_RADIUS * 2.0,
                    obstacle.height + COIN_RADIUS * 2.0
            );

            if (expanded.contains(x, y)) {
                return false;
            }
        }

        return true;
    }

    private static double distance(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;

        return Math.sqrt(dx * dx + dy * dy);
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

    public synchronized void addCoin(Point coinPosition) {
        if (coinPosition == null
                || coinPosition.x < WALL_THICKNESS + COIN_RADIUS
                || coinPosition.y < WALL_THICKNESS + COIN_RADIUS
                || coinPosition.x > WORLD_WIDTH - WALL_THICKNESS - COIN_RADIUS
                || coinPosition.y > WORLD_HEIGHT - WALL_THICKNESS - COIN_RADIUS) {
            return;
        }

        coins.add(new Point(coinPosition));
        repaint();
    }

    public synchronized List<Point> getCoins() {
        List<Point> copy = new ArrayList<>();

        for (Point coin : coins) {
            copy.add(new Point(coin));
        }

        return copy;
    }

    public synchronized int getScore() {
        return score;
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

        collectCoins();
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
        double min = WALL_THICKNESS + ROBOT_COLLISION_RADIUS;

        if (x < min || y < min
                || x > WORLD_WIDTH - min
                || y > WORLD_HEIGHT - min) {
            return false;
        }

        synchronized (this) {
            for (Rectangle obstacle : obstacles) {
                Rectangle2D expanded = new Rectangle2D.Double(
                        obstacle.x - ROBOT_COLLISION_RADIUS,
                        obstacle.y - ROBOT_COLLISION_RADIUS,
                        obstacle.width + ROBOT_COLLISION_RADIUS * 2.0,
                        obstacle.height + ROBOT_COLLISION_RADIUS * 2.0
                );

                if (expanded.contains(x, y)) {
                    return false;
                }
            }
        }

        return true;
    }


    private synchronized void collectCoins() {
        Iterator<Point> iterator = coins.iterator();
        int collected = 0;

        while (iterator.hasNext()) {
            Point coin = iterator.next();

            double dx = m_robotPositionX - coin.x;
            double dy = m_robotPositionY - coin.y;

            if (dx * dx + dy * dy
                    <= COIN_PICKUP_DISTANCE * COIN_PICKUP_DISTANCE) {
                iterator.remove();
                score++;
                collected++;
            }
        }

        for (int i = 0; i < collected; i++) {
            spawnCoin();
        }
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

        Graphics2D screenGraphics = (Graphics2D) g.create();

        try {
            screenGraphics.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            double scale = Math.min(
                    (double) getWidth() / WORLD_WIDTH,
                    (double) getHeight() / WORLD_HEIGHT
            );

            double offsetX = (getWidth() - WORLD_WIDTH * scale) / 2.0;
            double offsetY = (getHeight() - WORLD_HEIGHT * scale) / 2.0;

            Graphics2D worldGraphics = (Graphics2D) screenGraphics.create();

            try {
                worldGraphics.translate(offsetX, offsetY);
                worldGraphics.scale(scale, scale);

                drawWalls(worldGraphics);
                drawObstacles(worldGraphics);
                drawCoins(worldGraphics);

                drawRobot(
                        worldGraphics,
                        round(m_robotPositionX),
                        round(m_robotPositionY),
                        m_robotDirection
                );
            } finally {
                worldGraphics.dispose();
            }

            drawScore(screenGraphics);

        } finally {
            screenGraphics.dispose();
        }
    }

    private void drawWalls(Graphics2D g) {
        g.setColor(new Color(65, 65, 65));

        g.fillRect(0, 0, WORLD_WIDTH, WALL_THICKNESS);

        g.fillRect(
                0,
                WORLD_HEIGHT - WALL_THICKNESS,
                WORLD_WIDTH,
                WALL_THICKNESS
        );

        g.fillRect(0, 0, WALL_THICKNESS, WORLD_HEIGHT);

        g.fillRect(
                WORLD_WIDTH - WALL_THICKNESS,
                0,
                WALL_THICKNESS,
                WORLD_HEIGHT
        );

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        g.drawRect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
    }

    private void drawObstacles(Graphics2D g) {
        List<Rectangle> snapshot = getObstacles();

        g.setColor(new Color(90, 90, 90));

        for (Rectangle obstacle : snapshot) {
            g.fillRect(
                    obstacle.x,
                    obstacle.y,
                    obstacle.width,
                    obstacle.height
            );
        }

        g.setColor(Color.BLACK);

        for (Rectangle obstacle : snapshot) {
            g.drawRect(
                    obstacle.x,
                    obstacle.y,
                    obstacle.width,
                    obstacle.height
            );
        }
    }

    private void drawCoins(Graphics2D g) {
        List<Point> snapshot = getCoins();

        for (Point coin : snapshot) {
            int diameter = COIN_RADIUS * 2;

            g.setColor(new Color(255, 193, 7));
            g.fillOval(
                    coin.x - COIN_RADIUS,
                    coin.y - COIN_RADIUS,
                    diameter,
                    diameter
            );

            g.setColor(new Color(184, 134, 11));
            g.setStroke(new BasicStroke(2));
            g.drawOval(
                    coin.x - COIN_RADIUS,
                    coin.y - COIN_RADIUS,
                    diameter,
                    diameter
            );

            g.setColor(new Color(255, 235, 120));
            g.fillOval(coin.x - 2, coin.y - 2, 4, 4);
        }
    }

    private void drawScore(Graphics2D g) {
        String text = java.text.MessageFormat.format(I18n.get("game.score"), getScore());

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));

        int textWidth = g.getFontMetrics().stringWidth(text);
        int x = getWidth() - textWidth - 30;
        int y = 20;

        g.setColor(new Color(0, 0, 0, 175));
        g.fillRoundRect(x - 10, y - 17, textWidth + 20, 28, 10, 10);

        g.setColor(new Color(255, 215, 0));
        g.drawString(text, x, y + 2);
    }

    private static void fillOval(
            Graphics g,
            int centerX,
            int centerY,
            int diameterX,
            int diameterY
    ) {
        g.fillOval(
                centerX - diameterX / 2,
                centerY - diameterY / 2,
                diameterX,
                diameterY
        );
    }

    private static void drawOval(
            Graphics g,
            int centerX,
            int centerY,
            int diameterX,
            int diameterY
    ) {
        g.drawOval(
                centerX - diameterX / 2,
                centerY - diameterY / 2,
                diameterX,
                diameterY
        );
    }

    private void drawRobot(
            Graphics2D g,
            int x,
            int y,
            double direction
    ) {
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
