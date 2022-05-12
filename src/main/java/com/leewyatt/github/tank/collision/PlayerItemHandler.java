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
 * 玩家获得道具
 *      道具消失,产生道具效果
 */
public class PlayerItemHandler extends CollisionHandler {

    public PlayerItemHandler() {
        super(GameType.PLAYER, GameType.ITEM);
    }

    protected void onCollisionBegin(Entity player, Entity item) {
        TankApp app = getAppCast();
        ItemType itemType = item.getObject("itemType");
        play("item.wav");
        item.removeFromWorld();
        switch (itemType) {
            //道具:炸弹;查找地图全部敌人坦克,销毁(子弹攻击敌人能产生道具,但是道具攻击敌人不能产生道具)
            case BOMB -> collisionBomb();
            //道具:坦克;玩家恢复1点生命值
            case TANK -> collisionTank(player);
            //道具:船;玩家可以在水面行驶(效果不会延续到下一关)
            case SHIP -> collisionShip(player);
            //道具:星;玩家子弹如果没有满级,那么会升级
            case STAR -> collisionStar();
            //道具:铁锨;基地周围的墙,升级到石头
            case SPADE -> app.spadeBackUpBase();
            //道具:爱心;玩家生命值恢复到最大值
            case HEART -> collisionHeart(player);
            //道具:定时器;敌人坦克,全部停止行动一段时间
            case TIME -> app.freezingEnemy();
            //道具:武器;玩家子弹升到满级(效果会延续到下一关)
            case GUN -> set("playerBulletLevel", GameConfig.PLAYER_BULLET_MAX_LEVEL);
            //道具:钢盔;玩家获得一段时间的无敌保护
            case HELMET -> player.getComponent(EffectComponent.class)
                    .startEffect(new HelmetEffect());
            default -> {
            }
        }
    }

    private void collisionHeart(Entity player) {
        HealthIntComponent hp = player.getComponent(HealthIntComponent.class);
        hp.setValue(hp.getMaxValue());
    }

    private void collisionStar() {
        if (geti("playerBulletLevel") < GameConfig.PLAYER_BULLET_MAX_LEVEL) {
            inc("playerBulletLevel", 1);
        }
    }

    private void collisionShip(Entity player) {
        if (!player.getComponent(EffectComponent.class).hasEffect(ShipEffect.class)) {
            player.getComponent(EffectComponent.class).startEffect(new ShipEffect());
        }
    }

    private void collisionTank(Entity player) {
        HealthIntComponent hp = player.getComponent(HealthIntComponent.class);
        if (hp.getValue() < hp.getMaxValue()) {
            hp.damage(-1);
        }
    }

    private void collisionBomb() {
        List<Entity> enemyList = getGameWorld().getEntitiesByType(GameType.ENEMY);
        play("rocketBomb.wav");
        for (Entity enemy : enemyList) {
            spawn("explode", enemy.getCenter().getX() - 25, enemy.getCenter().getY() - 20);
            enemy.removeFromWorld();
            inc("destroyedEnemy", 1);
        }
    }

}
