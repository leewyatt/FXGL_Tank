package com.leewyatt.github.tank.collision;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;
import com.leewyatt.github.tank.GameType;

/**
 * @author LeeWyatt
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
