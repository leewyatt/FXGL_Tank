package com.leewyatt.github.tank.collision;

import com.almasb.fxgl.dsl.components.EffectComponent;
import com.almasb.fxgl.dsl.components.HealthIntComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;
import com.leewyatt.github.tank.GameConfig;
import com.leewyatt.github.tank.GameType;
import com.leewyatt.github.tank.ItemType;
import com.leewyatt.github.tank.TankApp;
import com.leewyatt.github.tank.effects.HelmetEffect;
import com.leewyatt.github.tank.effects.ShipEffect;

import java.util.List;

import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * @author LeeWyatt
 */
public class PlayerItemHandler extends CollisionHandler {

    public PlayerItemHandler() {
        super(GameType.PLAYER, GameType.ITEM);
    }

    protected void onCollisionBegin(Entity player, Entity item) {
        TankApp app = getAppCast();
        ItemType itemType = item.getObject("itemType");
        if (ItemType.BOMB == itemType) {
            List<Entity> enemyList = getGameWorld().getEntitiesByType(GameType.ENEMY);
            play("rocketBomb.wav");
            for (Entity enemy : enemyList) {
                spawn("explode", enemy.getCenter().getX() - 25, enemy.getCenter().getY() - 20);
                enemy.removeFromWorld();
                inc("destroyedEnemy", 1);
            }
            item.removeFromWorld();
            return;
        }
        play("item.wav");
        if (ItemType.TANK == itemType) {
            HealthIntComponent hp = player.getComponent(HealthIntComponent.class);
            if (hp.getValue() < hp.getMaxValue()) {
                hp.damage(-1);
            }
        }
        if (ItemType.SHIP == itemType) {
            if (!player.getComponent(EffectComponent.class).hasEffect(ShipEffect.class)) {
                player.getComponent(EffectComponent.class).startEffect(new ShipEffect());
            }
        }
        if (ItemType.STAR == itemType) {
            if (geti("playerBulletLevel") < GameConfig.PLAYER_BULLET_MAX_LEVEL) {
                inc("playerBulletLevel", 1);
            }
        }
        if (ItemType.GUN == itemType) {
            set("playerBulletLevel", GameConfig.PLAYER_BULLET_MAX_LEVEL);
        }
        if (ItemType.TIME == itemType) {
            app.freezingEnemy();
        }
        if (ItemType.HELMET == itemType) {
            player.getComponent(EffectComponent.class)
                    .startEffect(new HelmetEffect());
        }
        if (ItemType.SPADE == itemType) {
            app.spadeBackUpBase();
        }
        item.removeFromWorld();
    }

}
