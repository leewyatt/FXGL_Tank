package com.leewyatt.github.tank.collision;

import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.physics.CollisionHandler;
import com.leewyatt.github.tank.GameConfig;
import com.leewyatt.github.tank.GameType;
import com.leewyatt.github.tank.ItemType;

import static com.almasb.fxgl.dsl.FXGL.*;
import static com.almasb.fxgl.dsl.FXGL.getAppHeight;

/**
 * @author LeeWyatt
 * 子弹和敌人的碰撞(需要忽略敌人阵营的子弹;这个忽略的设置写到了产生子弹的实体方法里了)
 *      销毁敌人 也 销毁子弹
 */
public class BulletEnemyHandler extends CollisionHandler {

    public BulletEnemyHandler() {
        super(GameType.BULLET, GameType.ENEMY);
    }

    protected void onCollisionBegin(Entity bullet, Entity enemy) {
        play("normalBomb.wav");
        spawn("explode", enemy.getCenter().getX() - 25, enemy.getCenter().getY() - 20);
        bullet.removeFromWorld();
        enemy.removeFromWorld();
        inc("destroyedEnemy", 1);
        // get item 一定几率得到道具
        if (FXGLMath.randomBoolean(GameConfig.SPAWN_ITEM_PRO)) {
            spawn("item",
                    new SpawnData(FXGLMath.random(50, getAppWidth() - 50 - 6 * 24)
                            , FXGLMath.random(50, getAppHeight() - 50))
                            .put("itemType", FXGLMath.random(ItemType.values()).get()));
        }
    }
}
