package com.leewyatt.github.tank.collision;

import com.almasb.fxgl.dsl.components.EffectComponent;
import com.almasb.fxgl.dsl.components.HealthIntComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;
import com.leewyatt.github.tank.GameType;
import com.leewyatt.github.tank.TankApp;
import com.leewyatt.github.tank.effects.HelmetEffect;

import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * @author LeeWyatt
 */
public class BulletPlayerHandler extends CollisionHandler {

    public BulletPlayerHandler() {
        super(GameType.BULLET, GameType.PLAYER);
    }

    protected void onCollisionBegin(Entity bullet, Entity player) {
        play("normalBomb.wav");
        if (player.getComponent(EffectComponent.class).hasEffect(HelmetEffect.class)) {
            bullet.removeFromWorld();
            return;
        }
        spawn("explode", bullet.getCenter().getX() - 25, bullet.getCenter().getY() - 20);
        bullet.removeFromWorld();
        HealthIntComponent hp = player.getComponent(HealthIntComponent.class);
        hp.damage(1);
        TankApp tankApp = getAppCast();
        if (hp.isZero()) {
            if (!getb("gameOver")) {
                player.removeFromWorld();
                set("gameOver", true);
                getSceneService().pushSubScene(tankApp.failedSceneLazyValue.get());
            }
        }
    }
}
