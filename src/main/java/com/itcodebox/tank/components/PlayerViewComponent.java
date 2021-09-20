package com.itcodebox.tank.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.ViewComponent;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxgl.time.LocalTimer;
import com.itcodebox.tank.Config;
import javafx.geometry.Point2D;

import static com.almasb.fxgl.dsl.FXGL.spawn;

public class PlayerViewComponent extends Component {
    private MoveComponent moveComponent;
    private ViewComponent view;
    private Texture texture;

    private double frameWidth;
    private double frameHeight;

    private LocalTimer shootTimer = FXGL.newLocalTimer();

    public void shoot() {
        if (!shootTimer.elapsed(Config.SHOOT_DELAY)) {
            return;
        }
        spawn("bullet", new SpawnData(getEntity().getCenter().getX() - 15, getEntity().getCenter().getY() - 5)
                .put("direction", angleToVector())
                .put("owner", entity)
                .put("isRocket", true)
        );
        shootTimer.capture();
    }

    private Point2D angleToVector() {
        double angle = getEntity().getRotation();
        if (angle == 0) {
            return new Point2D(0, -1);
        } else if (angle == 90) {
            return new Point2D(1, 0);
        } else if (angle == 180) {
            return new Point2D(0, 1);
        } else {
            return new Point2D(-1, 0);
        }
    }

    public void up() {
        moveComponent.setMoveDir(Dir.UP);
    }

    public void down() {
        moveComponent.setMoveDir(Dir.DOWN);
    }

    public void left() {
        moveComponent.setMoveDir(Dir.LEFT);
    }

    public void right() {
        moveComponent.setMoveDir(Dir.RIGHT);
    }

}
