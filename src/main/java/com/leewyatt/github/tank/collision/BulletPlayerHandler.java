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
 * 子弹和玩家碰撞(为了扩展多个玩家,所以这里忽略了来自同盟的子弹,友军不能误伤;同样忽略的代码在产生子弹实体的方法里)
 * 子弹消失,玩家减少生命值
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
