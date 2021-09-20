package com.itcodebox.tank;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.*;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.action.ActionComponent;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.entity.state.StateComponent;
import com.almasb.fxgl.pathfinding.CellMoveComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.almasb.fxgl.ui.ProgressBar;
import com.itcodebox.tank.components.EnemyViewComponent;
import com.itcodebox.tank.components.MoveComponent;
import com.itcodebox.tank.components.PlayerViewComponent;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LeeWyatt
 */
public class GameEntityFactory implements EntityFactory {
    @Spawns("player")
    public Entity newPlayer(SpawnData data) {
        HealthIntComponent hpComponent = new HealthIntComponent(Config.PLAYER_HEALTH);
        ProgressBar hpView = new ProgressBar(false);
        hpView.setFill(Color.LIGHTGREEN);
        hpView.setMaxValue(Config.PLAYER_HEALTH);
        hpView.setWidth(35);
        hpView.setHeight(8);
        hpView.setTranslateY(38);
        hpView.currentValueProperty().bind(hpComponent.valueProperty());
        hpComponent.valueProperty().addListener((ob, ov, nv) -> {
            int hpValue = nv.intValue();
            if (hpValue >= 7) {
                hpView.setFill(Color.LIGHTGREEN);
            } else if (hpValue >= 4) {
                hpView.setFill(Color.GOLD);
            } else {
                hpView.setFill(Color.RED);
            }
        });
        return FXGL.entityBuilder(data)
                .type(GameType.PLAYER)
                .bbox(BoundingShape.box(35, 35))
                .view("tank/H1U.png")
                .view(hpView)
                .with(hpComponent)
                .with(new MoveComponent())
                .with(new PlayerViewComponent())
                .with(new KeepOnScreenComponent())
                .collidable()
                .build();
    }

    @Spawns("enemy")
    public Entity newEnemy(SpawnData data) {
        String assentName = data.get("assentName");
        return FXGL.entityBuilder(data)
                .type(GameType.ENEMY)
                .bbox(BoundingShape.box(35, 35))
                .view(assentName)
                .with(new MoveComponent())
                .with(new EnemyViewComponent())
                .with(new CellMoveComponent(Config.BLOCK_SIZE / 2, Config.BLOCK_SIZE / 2, 300).allowRotation(true))
                .with(new ActionComponent())
                .with(new StateComponent())
                .with(new KeepOnScreenComponent())
                .collidable()
                .build();
    }

    @Spawns("flag")
    public Entity newFlag(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(GameType.FLAG)
                .viewWithBBox("map/flag.png")
                .collidable()
                .build();
    }

    @Spawns("brick")
    public Entity newBrick(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(GameType.BRICK)
                .viewWithBBox("map/brick.png")
                .collidable()
                .build();
    }

    @Spawns("greens")
    public Entity newGreens(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(GameType.GREENS)
                .viewWithBBox("map/greens.png")
                .zIndex(100)
                .build();
    }

    @Spawns("sea")
    public Entity newSea(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(GameType.SEA)
                .viewWithBBox("map/sea.png")
                .collidable()
                .build();
    }

    @Spawns("snow")
    public Entity newSnow(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(GameType.SNOW)
                .viewWithBBox("map/snow.png")
                .build();
    }

    @Spawns("stone")
    public Entity newStone(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(GameType.STONE)
                .viewWithBBox("map/stone.png")
                .collidable()
                .build();
    }

    @Spawns("bullet")
    public Entity newBullet(SpawnData data) {
        boolean isRocket = data.get("isRocket");
        double speed;
        String texture;
        if (isRocket) {
            speed = Config.ROCKET_SPEED;
            texture = "bullet/rocketR.png";
            FXGL.play("rocketFire.wav");

        }else {
            speed = Config.BULLET_SPEED;
            texture ="bullet/normal.png";
            FXGL.play("normalFire.wav");
        }
        Entity owner = data.get("owner");
        CollidableComponent collidableComponent = new CollidableComponent(true);
        //检测碰撞, 忽略同类;Detect collisions, ignore the same type;
        collidableComponent.addIgnoredType(owner.getType());

        return FXGL.entityBuilder(data)
                .type(GameType.BULLET)
                .viewWithBBox(texture)
                .with(collidableComponent)
                .with(new OffscreenCleanComponent())
                .with(new ProjectileComponent(data.get("direction"),speed))
                .build();
    }



    static final List<Image> BOMB_IMG_List = new ArrayList<>();

    static {
        for (int i = 1; i < 10; i++) {
            BOMB_IMG_List.add(FXGL.image("tank/bomb_rocket_" + i + ".png"));
        }
    }

    static AnimationChannel animChan = new AnimationChannel(BOMB_IMG_List, Duration.seconds(.3));

    @Spawns("bombNormal")
    public Entity newBombNormal(SpawnData data) {
        return FXGL.entityBuilder(data)
                .view(new AnimatedTexture(animChan).play())
                .with(new ExpireCleanComponent(Duration.seconds(.3)))
                .build();
    }

}
