package com.itcodebox.tank.components;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.core.util.LazyValue;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityGroup;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.BoundingBoxComponent;
import com.almasb.fxgl.entity.components.ViewComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxgl.time.LocalTimer;
import com.itcodebox.tank.Config;
import javafx.geometry.Point2D;
import javafx.util.Duration;

import java.util.List;

import static com.almasb.fxgl.dsl.FXGL.spawn;
import static com.itcodebox.tank.GameType.*;

public class PlayerViewComponent extends Component {
    /**
     * 为了防止出现斜向上,斜向下等角度的移动,
     */
    private boolean movedThisFrame = false;
    private double speed = 0;
    private Vec2 velocity = new Vec2();
    private BoundingBoxComponent bbox;

    private ViewComponent view;
    private Texture texture;
    private LazyValue<EntityGroup> blocksAll = new LazyValue<>(() -> entity.getWorld().getGroup(BRICK, FLAG, SEA, STONE, ENEMY, BORDER_WALL));
    private LazyValue<EntityGroup> blocks = new LazyValue<>(() -> entity.getWorld().getGroup(BRICK, FLAG, STONE, ENEMY, BORDER_WALL));

    private double frameWidth;
    private double frameHeight;

    private LocalTimer shootTimer = FXGL.newLocalTimer();
    private static double speedFactor = 2.0;
    private AnimatedTexture helmetAnimTexture;
    private boolean armedShip;
    private  Texture  shipTexture ;

    public PlayerViewComponent() {
        initHelmetAnim();
        initShipTexture();
    }

    @Override
    public void onUpdate(double tpf) {
        speed = tpf * 60;
        movedThisFrame = false;
        if (FXGL.getb("armedHelmet")) {
            if (!entity.getViewComponent().getChildren().contains(helmetAnimTexture)) {
                entity.getViewComponent().addChild(helmetAnimTexture);
            }
        } else {
            if (entity.getViewComponent().getChildren().contains(helmetAnimTexture)) {
                entity.getViewComponent().removeChild(helmetAnimTexture);
            }
        }
    }

    public void setArmedShip(boolean armedShip) {
        if (this.armedShip ==armedShip) {
            return;
        }
        this.armedShip = armedShip;
        if(armedShip){
           if(!entity.getViewComponent().getChildren().contains(shipTexture)){
               entity.getViewComponent().addChild(shipTexture);
           }
        }else{
            if(entity.getViewComponent().getChildren().contains(shipTexture)){
                entity.getViewComponent().removeChild(shipTexture);
            }
        }
    }

    private void initShipTexture() {
        shipTexture = FXGL.texture("H1_ship.png");
        shipTexture.setScaleX(1.1);
        shipTexture.setScaleY(1.1);
        shipTexture.setTranslateY(0);
        shipTexture.setTranslateX(-3);
    }

    private void initHelmetAnim() {
        helmetAnimTexture = new AnimatedTexture(new AnimationChannel(FXGL.image("helmet_used.png"), Duration.seconds(.3), 4)).loop();
        helmetAnimTexture.setScaleX(1.5);
        helmetAnimTexture.setScaleY(1.5);
        helmetAnimTexture.setTranslateX(1);
        helmetAnimTexture.setTranslateY(6);
    }

    public void right() {
        if (movedThisFrame) {
            return;
        }
        movedThisFrame = true;
        getEntity().setRotation(90);
        move(speedFactor * speed, 0);

    }

    public void left() {
        if (movedThisFrame) {
            return;
        }
        movedThisFrame = true;
        getEntity().setRotation(270);
        move(-speedFactor * speed, 0);

    }

    public void down() {
        if (movedThisFrame) {
            return;
        }
        movedThisFrame = true;
        getEntity().setRotation(180);
        move(0, speedFactor * speed);

    }

    public void up() {
        if (movedThisFrame) {
            return;
        }
        movedThisFrame = true;
        getEntity().setRotation(0);
        move(0, -speedFactor * speed);
    }

    private void move(double dx, double dy) {

        if (!getEntity().isActive()) {
            return;
        }
        velocity.set((float) dx, (float) dy);
        int length = Math.round(velocity.length());
        velocity.normalizeLocal();
        List<Entity> blockList;
        if (armedShip) {
            blockList = blocks.get().getEntitiesCopy();
        } else {
            blockList = blocksAll.get().getEntitiesCopy();
        }
        for (int i = 0; i < length; i++) {
            entity.translate(velocity.x, velocity.y);
            boolean collision = false;
            for (int j = 0; j < blockList.size(); j++) {
                if (blockList.get(j).getBoundingBoxComponent().isCollidingWith(bbox)) {
                    collision = true;
                    break;
                }
            }
            if (collision) {
                entity.translate(-velocity.x, -velocity.y);
                break;
            }
        }
    }

    public void shoot() {
        if (!shootTimer.elapsed(Config.PLAYER_SHOOT_DELAY)) {
            return;
        }

        spawn("bullet", new SpawnData(getEntity().getCenter().getX() - 3, getEntity().getCenter().getY() - 4)
                .put("direction", angleToVector())
                .put("owner", entity));

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

}
