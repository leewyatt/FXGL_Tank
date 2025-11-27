package com.leewyatt.github.tank.components;

import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.core.util.LazyValue;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityGroup;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.BoundingBoxComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxgl.time.LocalTimer;
import com.leewyatt.github.tank.GameConfig;
import javafx.util.Duration;

import java.util.List;
import java.util.Random;

import static com.leewyatt.github.tank.GameType.*;

/**
 * @author LeeWyatt
 * 敌人坦克的行为,随机移动, 遇见障碍物, 1. 提高射击的几率;(帮助敌人打开更多的土墙)
 *                               2. 提高转弯的几率;(帮助敌人避开石头,水面等)
 */
public class EnemyComponent extends Component {
    private BoundingBoxComponent bbox;
    private LocalTimer shootTimer = FXGL.newLocalTimer();
    private Random random = new Random();
    private double speed = 0;
    private Dir moveDir;
    private LazyValue<EntityGroup> blocks = new LazyValue<>(() -> entity.getWorld().getGroup(BRICK, FLAG, SEA, STONE, ENEMY, PLAYER, BORDER_WALL));

    /**
     * 移动累加器，用于平滑移动速度
     * 累积每帧的小数部分，避免因 tpf 波动导致速度不稳定
     */
    private double moveAccumulator = 0;

    /**
     * 变成坦克前3秒不能动
     */
    private boolean canMove;
    private static AnimationChannel ac = new AnimationChannel(FXGL.image("tank/spawnTank.png"), Duration.seconds(0.4), 4);

    private static Dir[] dirs = Dir.values();

    @Override
    public void onUpdate(double tpf) {
        speed = tpf * GameConfig.ENEMY_SPEED;
        if (FXGL.getb("freezingEnemy") || !canMove) {
            return;
        }

        if (moveDir == Dir.UP && random.nextInt(1000) > 880) {
            moveDir = dirs[random.nextInt(4)];
        } else if (random.nextInt(1000) > 980) {
            moveDir = dirs[random.nextInt(4)];
        }
        setMoveDir(moveDir);
        if (random.nextInt(1000) > 980) {
            shoot();
        }
    }

    public void shoot() {
        if (!shootTimer.elapsed(GameConfig.ENEMY_SHOOT_DELAY)) {
            return;
        }
        FXGL.spawn("bullet", new SpawnData(getEntity().getCenter().add(-4, -4))
                .put("direction", moveDir.getVector())
                .put("owner", entity)
        );
        shootTimer.capture();
    }

    public void setMoveDir(Dir moveDir) {
        // 如果方向改变，重置累加器
        if (this.moveDir != moveDir) {
            this.moveAccumulator = 0;
        }
        this.moveDir = moveDir;
        switch (moveDir) {
            case UP -> up();
            case DOWN -> down();
            case LEFT -> left();
            case RIGHT -> right();
            default -> {
            }
        }
    }

    private void right() {
        getEntity().setRotation(90);
        move();
    }

    private void left() {
        getEntity().setRotation(270);
        move();
    }

    private void down() {
        getEntity().setRotation(180);
        move();
    }

    private void up() {
        getEntity().setRotation(0);
        move();
    }

    @Override
    public void onAdded() {
        moveDir = Dir.DOWN;
        Texture texture = FXGL.texture(entity.getString("assentName"));
        AnimatedTexture animatedTexture = new AnimatedTexture(ac).loop();
        entity.getViewComponent().addChild(animatedTexture);
        FXGL.runOnce(() -> {
            if (entity != null && entity.isActive()) {
                entity.getViewComponent().addChild(texture);
                entity.getViewComponent().removeChild(animatedTexture);
                canMove = true;
            }
        }, Duration.seconds(1));
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

        List<Entity> blockList = blocks.get().getEntitiesCopy();

        for (int i = 0; i < pixelsToMove; i++) {
            entity.translate(dirX, dirY);
            boolean collision = false;
            Entity entityTemp;
            for (int j = 0; j < blockList.size(); j++) {
                entityTemp = blockList.get(j);
                if (entityTemp == entity) {
                    continue;
                }
                if (entityTemp.getBoundingBoxComponent().isCollidingWith(bbox)) {
                    collision = true;
                    break;
                }
            }
            if (collision) {
                entity.translate(-dirX, -dirY);
                // 碰撞后重置累加器
                moveAccumulator = 0;
                // 碰撞后增加开火几率; Increase the chance of firing after a collision
                if (FXGLMath.randomBoolean(0.6)) {
                    shoot();
                }
                // 碰撞后增加改变方向的几率; Increase the chance of changing direction after collision
                if (FXGLMath.randomBoolean(0.3)) {
                    setMoveDir(Dir.values()[random.nextInt(4)]);
                }

                break;
            }
        }
    }

}
