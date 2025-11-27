package com.leewyatt.github.tank.components;

import com.almasb.fxgl.core.util.LazyValue;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.EffectComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityGroup;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.BoundingBoxComponent;
import com.almasb.fxgl.time.LocalTimer;
import com.leewyatt.github.tank.GameConfig;
import com.leewyatt.github.tank.effects.ShipEffect;

import java.util.List;

import static com.almasb.fxgl.dsl.FXGL.spawn;
import static com.leewyatt.github.tank.GameType.BORDER_WALL;
import static com.leewyatt.github.tank.GameType.BRICK;
import static com.leewyatt.github.tank.GameType.ENEMY;
import static com.leewyatt.github.tank.GameType.FLAG;
import static com.leewyatt.github.tank.GameType.SEA;
import static com.leewyatt.github.tank.GameType.STONE;

/**
 * @author LeeWyatt
 * 玩家的行为,移动和射击
 */
public class PlayerComponent extends Component {
    /**
     * 为了防止出现斜向上,斜向下等角度的移动,
     */
    private boolean movedThisFrame = false;
    private double speed = 0;
    private BoundingBoxComponent bbox;

    /**
     * 移动累加器，用于平滑移动速度
     * 累积每帧的小数部分，避免因 tpf 波动导致速度不稳定
     */
    private double moveAccumulator = 0;

    private LazyValue<EntityGroup> blocksAll = new LazyValue<>(() -> entity.getWorld().getGroup(BRICK, FLAG, SEA, STONE, ENEMY, BORDER_WALL));
    private LazyValue<EntityGroup> blocks = new LazyValue<>(() -> entity.getWorld().getGroup(BRICK, FLAG, STONE, ENEMY, BORDER_WALL));
    private LocalTimer shootTimer = FXGL.newLocalTimer();
    private Dir moveDir = Dir.UP;

    @Override
    public void onUpdate(double tpf) {
        speed = tpf * GameConfig.PLAYER_SPEED;
        movedThisFrame = false;
    }

    public void right() {
        if (movedThisFrame) {
            return;
        }
        movedThisFrame = true;
        getEntity().setRotation(90);
        moveDir = Dir.RIGHT;
        move();
    }

    public void left() {
        if (movedThisFrame) {
            return;
        }
        movedThisFrame = true;
        getEntity().setRotation(270);
        moveDir = Dir.LEFT;
        move();
    }

    public void down() {
        if (movedThisFrame) {
            return;
        }
        movedThisFrame = true;
        getEntity().setRotation(180);
        moveDir = Dir.DOWN;
        move();
    }

    public void up() {
        if (movedThisFrame) {
            return;
        }
        movedThisFrame = true;
        getEntity().setRotation(0);
        moveDir = Dir.UP;
        move();
    }

    private void move() {
        if (!getEntity().isActive()) {
            return;
        }

        // 累加本帧应该移动的距离
        moveAccumulator += speed;

        // 只移动整数像素部分
        int pixelsToMove = (int) moveAccumulator;
        if (pixelsToMove == 0) {
            return;
        }
        moveAccumulator -= pixelsToMove;  // 保留小数部分到下一帧

        // 获取移动方向的单位向量
        float dirX = (float) moveDir.getVector().getX();
        float dirY = (float) moveDir.getVector().getY();

        List<Entity> blockList;
        if (entity.getComponent(EffectComponent.class).hasEffect(ShipEffect.class)) {
            blockList = blocks.get().getEntitiesCopy();
        } else {
            blockList = blocksAll.get().getEntitiesCopy();
        }

        for (int i = 0; i < pixelsToMove; i++) {
            entity.translate(dirX, dirY);
            boolean collision = false;
            for (int j = 0; j < blockList.size(); j++) {
                if (blockList.get(j).getBoundingBoxComponent().isCollidingWith(bbox)) {
                    collision = true;
                    break;
                }
            }
            // 运动, 遇到障碍物回退
            if (collision) {
                entity.translate(-dirX, -dirY);
                moveAccumulator = 0;  // 碰撞后重置累加器
                break;
            }
        }
    }

    public void shoot() {
        if (!shootTimer.elapsed(GameConfig.PLAYER_SHOOT_DELAY)) {
            return;
        }
        spawn("bullet", new SpawnData(getEntity().getCenter().add(-4, -4.5))
                .put("direction", moveDir.getVector())
                .put("owner", entity));
        shootTimer.capture();
    }
}
