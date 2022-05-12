package com.leewyatt.github.tank.collision;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;
import com.leewyatt.github.tank.GameType;

/**
 * @author LeeWyatt
 * 碰撞检测: 子弹和边界碰撞,子弹消失,但是不产生爆炸的效果和声音,因为声音和爆炸效果太多,感觉不好.
 */
public class BulletBorderHandler extends CollisionHandler {

    public BulletBorderHandler() {
        super(GameType.BULLET, GameType.BORDER_WALL);
    }

    @Override
    protected void onCollisionBegin(Entity bullet, Entity border) {
        bullet.removeFromWorld();
    }
}
