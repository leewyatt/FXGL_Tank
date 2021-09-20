package com.itcodebox.tank.components;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.core.util.LazyValue;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityGroup;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.BoundingBoxComponent;

import java.util.List;

import static com.itcodebox.tank.GameType.*;


public class MoveComponent extends Component {

    private BoundingBoxComponent bbox;
    private Dir moveDir= Dir.UP;
    //为了防止出现斜向上,斜向下等角度的移动,
    private boolean movedThisFrame = false;
    private double speed = 0;
    private Vec2 velocity = new Vec2();

    private LazyValue<EntityGroup> blocks = new LazyValue<>(() -> entity.getWorld().getGroup(BRICK, FLAG, SEA, STONE, ENEMY));

    public void setMoveDir(Dir moveDir) {
        if (movedThisFrame) {
            return;
        }
        movedThisFrame = true;
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

    @Override
    public void onUpdate(double tpf) {
        speed = tpf * 60;
        movedThisFrame = false;
    }

    private void right() {
        getEntity().setRotation(90);
        move(3 * speed, 0);

    }

    private void left() {
        getEntity().setRotation(270);
        move(-3 * speed, 0);

    }

    private void down() {
        getEntity().setRotation(180);
        move(0, 3 * speed);

    }

    private void up() {
        getEntity().setRotation(0);
        move(0, -3 * speed);

    }

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
}
