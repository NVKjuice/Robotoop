package gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
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
    void robotCannotCrossRightWorldBoundary() {
        press("pressed W");
        updateMany(1000);
        assertTrue(visualizer.getRobotPositionX() <= 785.0);
        assertTrue(visualizer.getRobotPositionX() >= 780.0);
    }

    @Test
    void robotCannotCrossLeftWorldBoundary() {
        press("pressed S");
        updateMany(1000);
        assertTrue(visualizer.getRobotPositionX() >= 15.0);
        assertTrue(visualizer.getRobotPositionX() <= 20.0);
    }

    @Test
    void robotCannotCrossBottomWorldBoundary() {
        press("pressed D");
        updateMany(29);
        release("released D");
        press("pressed W");
        updateMany(1000);
        assertTrue(visualizer.getRobotPositionY() <= 585.0);
        assertTrue(visualizer.getRobotPositionY() >= 580.0);
    }

    @Test
    void robotCannotCrossTopWorldBoundary() {
        press("pressed A");
        updateMany(29);
        release("released A");
        press("pressed W");
        updateMany(1000);
        assertTrue(visualizer.getRobotPositionY() >= 15.0);
        assertTrue(visualizer.getRobotPositionY() <= 20.0);
    }

    @Test
    void robotCannotPassThroughObstacle() {
        visualizer.addObstacle(new Rectangle(130, 80, 20, 40));
        press("pressed W");
        updateMany(100);
        assertTrue(visualizer.getRobotPositionX() < 115.0);
        assertTrue(visualizer.getRobotPositionX() > 110.0);
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
    void obstacleListAndElementsAreDefensiveCopies() {
        Rectangle original = new Rectangle(200, 120, 30, 180);
        visualizer.addObstacle(original);
        original.setBounds(0, 0, 1, 1);

        List<Rectangle> returned = visualizer.getObstacles();
        assertEquals(new Rectangle(200, 120, 30, 180), returned.get(0));
        returned.get(0).setBounds(0, 0, 1, 1);
        assertEquals(new Rectangle(200, 120, 30, 180), visualizer.getObstacles().get(0));
    }

    @Test
    void clearObstaclesRemovesAllObstacles() {
        visualizer.addObstacle(new Rectangle(200, 120, 30, 180));
        visualizer.clearObstacles();
        assertTrue(visualizer.getObstacles().isEmpty());
    }

    @Test
    void invalidObstaclesAreIgnored() {
        visualizer.addObstacle(null);
        visualizer.addObstacle(new Rectangle(0, 0, 0, 20));
        visualizer.addObstacle(new Rectangle(0, 0, 20, -1));
        assertTrue(visualizer.getObstacles().isEmpty());
    }

    @Test
    void keyboardBindingsExistForWASDAndArrows() {
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
    void scalingKeepsWorldCenteredWhenPanelIsWiderThanWorld() {
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