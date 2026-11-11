package gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameVisualizerTest {
    private TestableGameVisualizer visualizer;

    @BeforeEach
    void setUp() {
        visualizer = new TestableGameVisualizer();
        visualizer.setSize(800, 600);
    }

    @Test
    void startsAtExpectedPositionAndDirection() {
        assertEquals(100.0, visualizer.getRobotPositionX(), 1e-9);
        assertEquals(100.0, visualizer.getRobotPositionY(), 1e-9);
        assertEquals(0.0, visualizer.getRobotDirection(), 1e-9);
    }

    @Test
    void wMovesRobotForwardAndReleaseStopsIt() {
        press("pressed W");
        visualizer.updateModelOnce();
        assertTrue(visualizer.getRobotPositionX() > 100.0);

        release("released W");
        double stoppedX = visualizer.getRobotPositionX();
        visualizer.updateModelOnce();
        assertEquals(stoppedX, visualizer.getRobotPositionX(), 1e-9);
    }

    @Test
    void upArrowMovesRobotForward() {
        press("pressed UP");
        visualizer.updateModelOnce();
        assertTrue(visualizer.getRobotPositionX() > 100.0);
    }

    @Test
    void sMovesRobotBackward() {
        press("pressed S");
        visualizer.updateModelOnce();
        assertTrue(visualizer.getRobotPositionX() < 100.0);
    }

    @Test
    void aTurnsRobotLeft() {
        press("pressed A");
        visualizer.updateModelOnce();
        assertTrue(visualizer.getRobotDirection() > Math.PI);
    }

    @Test
    void dTurnsRobotRight() {
        press("pressed D");
        visualizer.updateModelOnce();
        assertEquals(0.055, visualizer.getRobotDirection(), 1e-9);
    }

    @Test
    void oppositeTurnKeysCancelEachOther() {
        press("pressed A");
        press("pressed D");
        visualizer.updateModelOnce();
        assertEquals(0.0, visualizer.getRobotDirection(), 1e-9);
    }

    @Test
    void oppositeMovementKeysCancelEachOther() {
        press("pressed W");
        press("pressed S");
        visualizer.updateModelOnce();
        assertEquals(100.0, visualizer.getRobotPositionX(), 1e-9);
        assertEquals(100.0, visualizer.getRobotPositionY(), 1e-9);
    }


    @Test
    void robotStopsAtObstacle() {
        visualizer.clearObstacles();
        visualizer.addObstacle(new Rectangle(180, 80, 20, 40));

        press("pressed W");
        updateMany(100);

        assertTrue(visualizer.getRobotPositionX() > 100.0);
        assertTrue(
                visualizer.getRobotPositionX() < 180.0,
                "Робот прошёл сквозь препятствие"
        );
    }

    @Test
    void robotCanSlideAlongObstacle() {
        visualizer.addObstacle(new Rectangle(130, 80, 20, 40));
        press("pressed W");
        updateMany(10);
        double xBeforeTurn = visualizer.getRobotPositionX();

        press("pressed D");
        updateMany(10);
        release("released D");
        press("pressed W");
        updateMany(10);

        assertTrue(visualizer.getRobotPositionX() <= xBeforeTurn + 2.8);
        assertTrue(visualizer.getRobotPositionY() > 100.0);
    }

    @Test
    void obstacleCopies() {
        Rectangle original = new Rectangle(200, 120, 30, 180);
        visualizer.addObstacle(original);
        original.setBounds(0, 0, 1, 1);

        List<Rectangle> returned = visualizer.getObstacles();
        Rectangle added = returned.get(returned.size() - 1);

        assertEquals(new Rectangle(200, 120, 30, 180), added);

        added.setBounds(0, 0, 1, 1);

        List<Rectangle> updated = visualizer.getObstacles();
        Rectangle stored = updated.get(updated.size() - 1);

        assertEquals(new Rectangle(200, 120, 30, 180), stored);
    }

    @Test
    void clearObstaclesRemovesAllObstacles() {
        visualizer.addObstacle(new Rectangle(200, 120, 30, 180));
        visualizer.clearObstacles();
        assertTrue(visualizer.getObstacles().isEmpty());
    }

    @Test
    void invalidObstacles() {
        int initialSize = visualizer.getObstacles().size();

        visualizer.addObstacle(null);
        visualizer.addObstacle(new Rectangle(0, 0, 0, 20));
        visualizer.addObstacle(new Rectangle(0, 0, 20, -1));

        assertEquals(initialSize, visualizer.getObstacles().size());
    }

    @Test
    void BindingsForWASDAndArrows() {
        String[] keys = {
                "pressed W", "released W", "pressed UP", "released UP",
                "pressed S", "released S", "pressed DOWN", "released DOWN",
                "pressed A", "released A", "pressed LEFT", "released LEFT",
                "pressed D", "released D", "pressed RIGHT", "released RIGHT"
        };
        for (String key : keys) {
            KeyStroke stroke = KeyStroke.getKeyStroke(key);
            Object actionKey = visualizer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(stroke);
            assertNotNull(actionKey, "Нет привязки для " + key);
            assertNotNull(visualizer.getActionMap().get(actionKey), "Нет действия для " + key);
        }
    }

    @Test
    void paintingWorksAtDifferentPanelSizes() {
        int[][] sizes = {{400, 300}, {1200, 900}, {1200, 600}, {600, 900}};
        visualizer.addObstacle(new Rectangle(200, 120, 30, 180));
        for (int[] size : sizes) {
            visualizer.setSize(size[0], size[1]);
            BufferedImage image = new BufferedImage(size[0], size[1], BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            try {
                visualizer.paint(graphics);
            } finally {
                graphics.dispose();
            }
            assertEquals(size[0], image.getWidth());
            assertEquals(size[1], image.getHeight());
        }
    }

    @Test
    void scaling() {
        visualizer.setBackground(Color.WHITE);
        visualizer.setSize(1200, 600);
        visualizer.addObstacle(new Rectangle(200, 120, 30, 180));
        BufferedImage image = new BufferedImage(1200, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            visualizer.paint(graphics);
        } finally {
            graphics.dispose();
        }

        assertEquals(new Color(90, 90, 90), new Color(image.getRGB(405, 125), true));
        assertEquals(Color.WHITE, new Color(image.getRGB(50, 300), true));
    }

    @Test
    void startsWithSevenCoins() {
        assertEquals(7, visualizer.getCoins().size());
    }

    @Test
    void coinsAvoidWalls() {
        List<Point> coins = visualizer.getCoins();
        List<Rectangle> walls = visualizer.getObstacles();

        for (Point coin : coins) {
            for (Rectangle wall : walls) {
                Rectangle2D expanded = new Rectangle2D.Double(
                        wall.x - 8,
                        wall.y - 8,
                        wall.width + 16,
                        wall.height + 16
                );

                assertFalse(
                        expanded.contains(coin),
                        "Монета находится слишком близко к стене: " + coin
                );
            }
        }
    }

    @Test
    void coinsAreSeparated() {
        List<Point> coins = visualizer.getCoins();

        for (int i = 0; i < coins.size(); i++) {
            for (int j = i + 1; j < coins.size(); j++) {
                Point first = coins.get(i);
                Point second = coins.get(j);

                assertTrue(
                        first.distance(second) >= 24,
                        "Монеты расположены слишком близко друг к другу"
                );
            }
        }
    }

    @Test
    void mapHasWalls() {
        assertFalse(GameMap.createWalls().isEmpty());
    }

    private void press(String keyStroke) {
        invokeKeyAction(keyStroke);
    }

    private void release(String keyStroke) {
        invokeKeyAction(keyStroke);
    }

    private void invokeKeyAction(String keyStroke) {
        KeyStroke stroke = KeyStroke.getKeyStroke(keyStroke);
        Object actionKey = visualizer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(stroke);
        Action action = visualizer.getActionMap().get(actionKey);
        action.actionPerformed(new ActionEvent(visualizer, ActionEvent.ACTION_PERFORMED, keyStroke));
    }

    private void updateMany(int count) {
        for (int i = 0; i < count; i++) {
            visualizer.updateModelOnce();
        }
    }

    private static class TestableGameVisualizer extends GameVisualizer {
        @Override
        protected void onModelUpdateEvent() {
        }

        void updateModelOnce() {
            super.onModelUpdateEvent();
        }
    }
}