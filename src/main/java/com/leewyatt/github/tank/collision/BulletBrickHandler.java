package com.leewyatt.github.tank.collision;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;
import com.leewyatt.github.tank.GameConfig;
import com.leewyatt.github.tank.GameType;

import java.io.Serializable;
import java.util.List;

import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * @author LeeWyatt
 */
public class BulletBrickHandler extends CollisionHandler {

    public BulletBrickHandler() {
        super(GameType.BULLET, GameType.BRICK);
    }

    /**
     * 注意这里不是onCollisionBegin,因为要检查有较多的碰撞检测
     */
    @Override
    protected void onCollision(Entity bullet, Entity brick) {
        Entity tank = bullet.getObject("owner");
        Serializable tankType = tank.getType();
        //找到碰撞的方块(石头,土墙,草地)
        List<Entity> list = getGameWorld().getEntitiesFiltered(tempE ->
                tempE.getBoundingBoxComponent().isCollidingWith(bullet.getBoundingBoxComponent())
                        && (tempE.isType(GameType.STONE)
                        || tempE.isType(GameType.BRICK)
                        || tempE.isType(GameType.GREENS))
        );
        boolean removeBullet = false;
        for (Entity entity : list) {
            Serializable entityType = entity.getType();
            if (entityType == GameType.BRICK) {
                removeBullet = true;
                if (entity.isActive()) {
                    entity.removeFromWorld();
                }
            } else if (entityType == GameType.GREENS) {
                if (tankType == GameType.PLAYER
                        && entity.isActive()
                        && geti("playerBulletLevel") == GameConfig.PLAYER_BULLET_MAX_LEVEL) {
                    entity.removeFromWorld();
                }
            } else { //STONE
                removeBullet = true;
                if (tankType == GameType.PLAYER
                        && entity.isActive()
                        && geti("playerBulletLevel") == GameConfig.PLAYER_BULLET_MAX_LEVEL) {
                    entity.removeFromWorld();
                }
            }
        }
        if (removeBullet) {
            bullet.removeFromWorld();
            play("normalBomb.wav");
            spawn("explode", bullet.getCenter().getX() - 25, bullet.getCenter().getY() - 20);
        }
    }
}
