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
 * 子弹和墙壁碰撞; 这个方法,同时可以用于 子弹和石头, 子弹和草地
 * 因为子弹可能击中两个物体交界的地方;
 * 所以要同时判断,子弹到底碰撞了多少个物体;根据物体不同,那么做不同的处理
 * 如果碰到了草地 ,那么子弹不会销毁;
 *              如果是顶级子弹,那么草地(森林)会被销毁,子弹依然不会被销毁
 * 如果碰到了砖墙 ,那么子弹和撞墙都消失
 * 如果碰到了石头 ,那么子弹会消失
 *              如果是顶级子弹, 那么石头也会消失
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
