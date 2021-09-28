package com.itcodebox.tank;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.ExpireCleanComponent;
import com.almasb.fxgl.dsl.components.HealthIntComponent;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.almasb.fxgl.ui.ProgressBar;
import com.itcodebox.tank.components.EnemyViewComponent;
import com.itcodebox.tank.components.FlagViewComponent;
import com.itcodebox.tank.components.PlayerViewComponent;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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
        hpView.setTranslateY(36);

        hpView.currentValueProperty().bind(hpComponent.valueProperty());
        hpComponent.valueProperty().addListener((ob, ov, nv) -> {
            int hpValue = nv.intValue();
            if (hpValue >= Config.PLAYER_HEALTH * .7) {
                hpView.setFill(Color.LIGHTGREEN);
            } else if (hpValue >= Config.PLAYER_HEALTH * .4) {
                hpView.setFill(Color.GOLD);
            } else {
                hpView.setFill(Color.RED);
            }
        });
        return FXGL.entityBuilder(data)
                .type(GameType.PLAYER)
                .bbox(BoundingShape.box(38, 38))
                .view("tank/H1U.png")
                .view(hpView)
                .with(hpComponent)
                .with(new PlayerViewComponent())
                //.with(new KeepOnScreenComponent())
                .collidable()
                .build();
    }

    @Spawns("enemy")
    public Entity newEnemy(SpawnData data) {
        String assentName = data.get("assentName");
        return FXGL.entityBuilder(data)
                .type(GameType.ENEMY)
                .with("assentName", assentName)
                .bbox(BoundingShape.box(36, 36))
                .with(new EnemyViewComponent())
                //.with(new KeepOnScreenComponent())
                .collidable()
                .build();
    }

    @Spawns("flag")
    public Entity newFlag(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(GameType.FLAG)
                .viewWithBBox("map/flag.png")
                .with(new FlagViewComponent())
                .collidable()
                .build();
    }

    @Spawns("brick")
    public Entity newBrick(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(GameType.BRICK)
                .viewWithBBox("map/brick.png")
                .collidable()
                .neverUpdated()
                .build();
    }

    @Spawns("greens")
    public Entity newGreens(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(GameType.GREENS)
                .viewWithBBox("map/greens.png")
                .zIndex(100)
                .neverUpdated()
                .collidable()
                .build();
    }

    private final AnimationChannel seaAnimChan = new AnimationChannel(FXGL.image("map/sea_anim.png"), Duration.seconds(1.5), 2);

    @Spawns("sea")
    public Entity newSea(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(GameType.SEA)
                .viewWithBBox(new AnimatedTexture(seaAnimChan).loop())
                .build();
    }

    @Spawns("snow")
    public Entity newSnow(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(GameType.SNOW)
                .viewWithBBox("map/snow.png")
                .neverUpdated()
                .build();
    }

    private static final List<Image> STONE_BRICK_FLASH_IMAGE_LIST = new ArrayList<>();
    private static final AnimationChannel STONE_BRICK_AC;

    static {
        STONE_BRICK_FLASH_IMAGE_LIST.add(FXGL.image("map/stone.png"));
        STONE_BRICK_FLASH_IMAGE_LIST.add(FXGL.image("map/brick.png"));
        STONE_BRICK_AC = new AnimationChannel(STONE_BRICK_FLASH_IMAGE_LIST, Duration.seconds(.5));
    }

    @Spawns("stone")
    public Entity newStone(SpawnData data) {
        AnimatedTexture animatedTexture = new AnimatedTexture(STONE_BRICK_AC);
        Entity entity = FXGL.entityBuilder(data)
                .type(GameType.STONE)
                .viewWithBBox(animatedTexture)
                .collidable()
                .build();
        if (data.getData().containsKey("rewardStone")) {
            FXGL.runOnce(animatedTexture::loop, Config.SPADE_NOTIFY_TIME);
        } else {
            entity.setEverUpdated(true);
        }
        return entity;
    }

    @Spawns("bullet")
    public Entity newBullet(SpawnData data) {
        double speed;
        String textureStr;
        Entity owner = data.get("owner");
        CollidableComponent collidableComponent = new CollidableComponent(true);
        //检测碰撞, 忽略同类;Detect collisions, ignore the same type;
        collidableComponent.addIgnoredType(owner.getType());
        if (GameType.PLAYER == owner.getType()) {
            int bulletLevel = FXGL.geti("playerBulletLevel");
            if (bulletLevel < Config.PLAYER_BULLET_MAX_LEVEL) {
                textureStr = "bullet/normal.png";
                FXGL.play("normalFire.wav");
            } else {
                textureStr = "bullet/plus.png";
                FXGL.play("rocketFire.wav");
            }
            speed = Config.PLAYER_BULLET_SPEED + bulletLevel * 100;
        } else {
            speed = Config.ENEMY_BULLET_SPEED;
            textureStr = "bullet/normal.png";
            FXGL.play("normalFire.wav");
        }
        return FXGL.entityBuilder(data)
                .type(GameType.BULLET)
                .viewWithBBox(textureStr)
                .with(collidableComponent)
                .with(new ProjectileComponent(data.get("direction"), speed))
                .build();
    }

    private static final List<Image> BOMB_IMG_List = new ArrayList<>();

    static {
        for (int i = 1; i < 10; i++) {
            BOMB_IMG_List.add(FXGL.image("tank/bomb_rocket_" + i + ".png"));
        }
    }

    private AnimationChannel bombAc = new AnimationChannel(BOMB_IMG_List, Config.BOMB_ANIME_TIME);

    @Spawns("bomb")
    public Entity newBomb(SpawnData data) {
        return FXGL.entityBuilder(data)
                .view(new AnimatedTexture(bombAc).play())
                .with(new ExpireCleanComponent(Config.BOMB_ANIME_TIME))
                .build();
    }

    private static BoundingShape box50 = BoundingShape.box(50, 50);

    @Spawns("empty")
    public Entity newEmpty(SpawnData data) {
        return FXGL.entityBuilder(data)
                .at(data.getX() - 5, data.getY() - 5)
                .bbox(box50)
                .collidable()
                .build();
    }

    @Spawns("reward")
    public Entity newReward(SpawnData data) {
        //1张图片. 产生闪烁的效果
        AnimationChannel ac = new AnimationChannel(FXGL.image("reward/" + data.<String>get("rewardName") + ".png"), 1, 30, 28, Duration.seconds(.5), 0, 1);
        AnimatedTexture animatedTexture = new AnimatedTexture(ac);
        Entity entity = FXGL.entityBuilder(data)
                .type(GameType.REWARD)
                .viewWithBBox(animatedTexture)
                .scale(1.2, 1.2)
                .with(new ExpireCleanComponent(Config.REWARD_SHOW_TIME))
                .collidable()
                .zIndex(200)
                .build();
        FXGL.runOnce(animatedTexture::loop, Config.REWARD_NOTIFY_TIME);
        return entity;
    }

    @Spawns("borderWall")
    public Entity newBorderWall(SpawnData data) {
        return FXGL.entityBuilder(data)
                .type(GameType.BORDER_WALL)
                .viewWithBBox(new Rectangle(data.<Integer>get("width"), data.<Integer>get("height"), Color.web("666666")))
                .with(new PhysicsComponent())
                .neverUpdated()
                .collidable()
                .build();
    }
}
