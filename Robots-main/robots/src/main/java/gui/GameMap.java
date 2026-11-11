
package gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public final class GameMap {

    private GameMap() {
    }

    public static List<Rectangle> createWalls() {
        List<Rectangle> walls = new ArrayList<>();

        walls.add(new Rectangle(220, 150, 170, 7));

        walls.add(new Rectangle(410, 90, 7, 170));

        walls.add(new Rectangle(540, 300, 160, 7));
        walls.add(new Rectangle(540, 300, 7, 110));

        walls.add(new Rectangle(180, 380, 7, 100));
        walls.add(new Rectangle(180, 440, 140, 7));

        return walls;
    }
}

