package gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameVisualizerTest {

    private TestableGameVisualizer visualizer;

    @BeforeEach
    void setUp() {
        visualizer = new TestableGameVisualizer();
        visualizer.setSize(200, 150);
    }

    @Test
    void InsidePanel() {
        visualizer.setTargetPosition(new Point(0, 75));

        checkBoundsDuringMovement(1000);
    }

    @Test
    void InsidePanelRight() {
        visualizer.setTargetPosition(new Point(250, 75));

        checkBoundsDuringMovement(1000);
    }

    @Test
    void InsidePanelTop() {
        visualizer.setTargetPosition(new Point(100, 0));

        checkBoundsDuringMovement(1000);
    }

    @Test
    void InsidePanelBottom() {
        visualizer.setTargetPosition(new Point(100, 200));

        checkBoundsDuringMovement(1000);
    }

    @Test
    void robotTurnsInPlaceWhenTargetIsBehindIt() {
        visualizer.setTargetPosition(new Point(50, 100));

        double startX = visualizer.getRobotPositionX();
        double startY = visualizer.getRobotPositionY();

        visualizer.updateModelOnce();

        assertEquals(startX, visualizer.getRobotPositionX(), 1e-9);
        assertEquals(startY, visualizer.getRobotPositionY(), 1e-9);
        assertTrue(visualizer.getRobotDirection() > 0.0,
                "Робот должен повернуться, а потом ехать");
    }

    @Test
    void robotMovesWhenTargetIsInFront() {
        visualizer.setTargetPosition(new Point(150, 100));

        double startX = visualizer.getRobotPositionX();

        visualizer.updateModelOnce();

        assertTrue(visualizer.getRobotPositionX() > startX,
                "Робот должен ехать вперед");
    }

    private void checkBoundsDuringMovement(int numberOfSteps) {
        double minX = 15;
        double minY = 15;
        double maxX = visualizer.getWidth() - 15;
        double maxY = visualizer.getHeight() - 15;

        for (int i = 0; i < numberOfSteps; i++) {
            visualizer.updateModelOnce();

            assertTrue(visualizer.getRobotPositionX() >= minX,
                    "Пересек левую границу на шаге " + i);
            assertTrue(visualizer.getRobotPositionX() <= maxX,
                    "Пересек левую границу на шаге  " + i);
            assertTrue(visualizer.getRobotPositionY() >= minY,
                    "Пересек верхнюю границу на шаге  " + i);
            assertTrue(visualizer.getRobotPositionY() <= maxY,
                    "Пересек нижнюю границу на шаге  " + i);
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
