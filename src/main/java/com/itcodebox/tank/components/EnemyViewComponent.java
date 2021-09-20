package com.itcodebox.tank.components;

import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.core.util.LazyValue;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityGroup;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.BoundingBoxComponent;
import com.almasb.fxgl.entity.components.ViewComponent;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxgl.time.LocalTimer;
import com.itcodebox.tank.Config;
import javafx.geometry.Point2D;

import java.util.List;
import java.util.Random;

import static com.almasb.fxgl.dsl.FXGL.spawn;
import static com.itcodebox.tank.GameType.*;

/**
 * @author LeeWyatt
 */
public class EnemyViewComponent extends Component {
    private BoundingBoxComponent bbox;
    private ViewComponent view;
    private Texture texture;

    private double frameWidth;
    private double frameHeight;

    private LocalTimer shootTimer = FXGL.newLocalTimer();
    private Random random = new Random();
    private double speed = 1;
    private Dir moveDir;
    private LazyValue<EntityGroup> blocks = new LazyValue<>(() -> entity.getWorld().getGroup(BRICK, FLAG, SEA, STONE,ENEMY,PLAYER));
    @Override
    public void onUpdate(double tpf) {

        if (random.nextInt(1000) >960) {
           setMoveDir(Dir.values()[random.nextInt(4)]);
        }else{
            setMoveDir(moveDir);
        }

        if (random.nextInt(1000) >900) {
            shoot();
        }
    }

    public void shoot() {
        if (!shootTimer.elapsed(Config.SHOOT_DELAY)) {
            return;
        }
        spawn("bullet", new SpawnData(getEntity().getCenter().getX()-7,getEntity().getCenter().getY()-5)
                        .put("direction", angleToVector())
                        .put("owner", entity).put("isRocket",false));

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
        } else {    // 270
            return new Point2D(-1, 0);
        }
    }
    public void setMoveDir(Dir moveDir) {
        this.moveDir = moveDir;
        switch (moveDir) {
            case UP:
                up();
                break;
            case DOWN:
                down();
                break;
            case LEFT:
                left();
                break;
            case RIGHT:
                right();
                break;
            default:
                break;
        }
    }



    private void right() {
        getEntity().setRotation(90);
        move(2.5 * speed, 0);

    }

    private void left() {
        getEntity().setRotation(270);
        move(-2.5 * speed, 0);

    }

    private void down() {
        getEntity().setRotation(180);
        move(0, 2.5 * speed);

    }

    private void up() {
        getEntity().setRotation(0);
        move(0, -2.5 * speed);

    }

    @Override
    public void onAdded() {
        moveDir = FXGLMath.random(Dir.values()).get();
    }

    private Vec2 velocity = new Vec2();

    private void move(double dx, double dy) {
        if (!getEntity().isActive()) {
            return;
        }
        velocity.set((float) dx, (float) dy);
        int length = Math.round(velocity.length());
        velocity.normalizeLocal();

        List<Entity> blockList = blocks.get().getEntitiesCopy();
        for (int i = 0; i < length; i++) {
            entity.translate(velocity.x, velocity.y);
            boolean collision = false;
            Entity entityTemp = null;
            for (int j = 0; j < blockList.size(); j++) {
                 entityTemp = blockList.get(j);
                if (entityTemp == entity) {
                    continue;
                }
                if(entityTemp.getBoundingBoxComponent().isCollidingWith(bbox)){
                    collision = true;
                    break;
                }
            }
            if (collision) {
                entity.translate(-velocity.x, -velocity.y);
                //碰撞后增加开火几率; Increase the chance of firing after a collision
                if (random.nextInt(10) > 5) {
                    shoot();
                }
                //碰撞后增加改变方向的几率;Increase the chance of changing direction after collision;
                if (random.nextInt(10) >4) {
                    setMoveDir(Dir.values()[random.nextInt(4)]);
                }

                break;
            }
        }
    }


}
