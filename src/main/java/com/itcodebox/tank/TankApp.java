package com.itcodebox.tank;

import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.LoadingScene;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.core.util.LazyValue;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.HealthIntComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxgl.time.LocalTimer;
import com.itcodebox.tank.components.PlayerViewComponent;
import com.itcodebox.tank.ui.GameLoadingScene;
import com.itcodebox.tank.ui.GameMenu;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author LeeWyatt
 */
public class TankApp extends GameApplication {

    private PlayerViewComponent playerView;
    public static final int PLAYER_BULLET_MAX_LEVEL = 3;
    private Random random = new Random();
    //private String[] rewardSkin = {"bomb","gun","helmet","spade","star","tank","time","ship"};
    private String[] rewardSkin = {"gun", "star", "tank", "time", "ship", "bomb"};
    private LazyValue<List<Entity>> bricksValue = new LazyValue<List<Entity>>(() -> FXGL.getGameWorld().getEntitiesByType(GameType.BRICK));
    private LazyValue<List<Entity>> stonesValue = new LazyValue<List<Entity>>(() -> FXGL.getGameWorld().getEntitiesByType(GameType.STONE));
    private Texture shipTexture;

    private LocalTimer timer;

    @Override
    protected void onUpdate(double tpf) {
        if (timer != null) {
            if (timer.elapsed(Duration.seconds(10))) {
                FXGL.set("stopTime", false);
                timer = null;
            }
        }
    }

    @Override
    protected void initSettings(GameSettings settings) {
        //26cell * 24px
        settings.setWidth(26 * 24);
        settings.setHeight(26 * 24);
        settings.setFontUI("airstrikeacad.ttf");
        settings.setTitle("90 Tank");
        settings.setAppIcon("icon.png");
        settings.setVersion("Version 0.1");
        settings.setMainMenuEnabled(true);
        settings.setGameMenuEnabled(true);
        settings.setSceneFactory(new SceneFactory() {
            @Override
            public FXGLMenu newMainMenu() {
                return new GameMenu();
            }

            @Override
            public LoadingScene newLoadingScene() {
                return new GameLoadingScene();
            }
        });
        //开发模式.这样可以输出较多的日志异常追踪
        settings.setApplicationMode(ApplicationMode.DEVELOPER);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("playerBulletLevel", 0);
        vars.put("hasShip", false);
        vars.put("stopTime", false);
    }

    @Override
    protected void initInput() {
        Input input = FXGL.getInput();
        input.addAction(new UserAction("Move Up") {
            @Override
            protected void onAction() {
                playerView.up();
            }
        }, KeyCode.UP);
        input.addAction(new UserAction("Move Down") {
            @Override
            protected void onAction() {
                playerView.down();
            }
        }, KeyCode.DOWN);
        input.addAction(new UserAction("Move Left") {
            @Override
            protected void onAction() {
                playerView.left();
            }
        }, KeyCode.LEFT);
        input.addAction(new UserAction("Move Right") {
            @Override
            protected void onAction() {
                playerView.right();
            }
        }, KeyCode.RIGHT);

        input.addAction(new UserAction("Shoot") {
            @Override
            protected void onAction() {
                playerView.shoot();
            }
        }, KeyCode.SPACE);

    }

    @Override
    protected void initGame() {
        FXGL.getGameScene().setBackgroundColor(Color.BLACK);
        FXGL.getGameWorld().addEntityFactory(new GameEntityFactory());
        FXGL.setLevelFromMap("level1.tmx");
        for (int i = 0; i < 15; i++) {
            FXGL.spawn("enemy",
                    new SpawnData(24 + i * 40, 20).put("assentName", "tank/E" + FXGLMath.random(1, 12) + "U.png"));
        }
        Entity player = FXGL.spawn("player", 8 * 24, 24 * 24);
        playerView = player.getComponent(PlayerViewComponent.class);

        shipTexture = FXGL.texture("H1_ship.png");
        shipTexture.setScaleX(1.1);
        shipTexture.setScaleY(1.1);
        shipTexture.setTranslateY(0);
        shipTexture.setTranslateX(-3);

    }

    @Override
    protected void initPhysics() {

        CollisionHandler collisionHandler = new CollisionHandler(GameType.BULLET, GameType.ENEMY) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity enemy) {
                FXGL.play("normalBomb.wav");
                FXGL.spawn("bombNormal", enemy.getCenter().getX() - 25, enemy.getCenter().getY() - 15);
                bullet.removeFromWorld();
                enemy.removeFromWorld();
                //20% get reward
                if (random.nextInt(10) > 2) {
                    FXGL.spawn("reward",
                            new SpawnData(FXGLMath.random(50, FXGL.getAppWidth() - 50), FXGLMath.random(50, FXGL.getAppHeight() - 50))
                                    .put("rewardName", FXGL.getGameWorld().getEntitiesByType(GameType.ENEMY).size() > 5 ? rewardSkin[random.nextInt(rewardSkin.length - 1)] : rewardSkin[random.nextInt(rewardSkin.length)])
                    );
                }
            }
        };

        FXGL.getPhysicsWorld().addCollisionHandler(collisionHandler);

        //如果子弹同时击中两个砖块. 那么可以清除2个砖块;
        //If the bullet hits two bricks at the same time, then 2 bricks can be cleared;
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameType.BULLET, GameType.BRICK) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity brick) {
                FXGL.play("normalBomb.wav");
                FXGL.spawn("bombNormal", bullet.getCenter().getX() - 15, bullet.getCenter().getY() - 15);
                //移除其中一个
                brick.removeFromWorld();
                //判断,如果还有砖块被击中. 那么也移除
                List<Entity> brickList = bricksValue.get();
                for (Entity entity : brickList) {
                    if (entity.isActive()) {
                        if (bullet.getBoundingBoxComponent().isCollidingWith(entity.getBoundingBoxComponent())) {
                            if (entity.isActive()) {
                                entity.removeFromWorld();
                                break;
                            }
                        }
                    }
                }
                bullet.removeFromWorld();
            }
        });

        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameType.BULLET, GameType.STONE) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity stone) {
                FXGL.play("normalBomb.wav");
                FXGL.spawn("bombNormal", bullet.getCenter().getX() - 25, bullet.getCenter().getY() - 15);
                Entity owner = bullet.getObject("owner");
                Serializable ownerType = owner.getType();
                if (ownerType == GameType.PLAYER && FXGL.geti("playerBulletLevel") == PLAYER_BULLET_MAX_LEVEL) {
                    stone.removeFromWorld();
                    List<Entity> stonesList = stonesValue.get();
                    for (Entity otherStone : stonesList) {
                        if (otherStone.isActive()) {
                            if (bullet.getBoundingBoxComponent().isCollidingWith(otherStone.getBoundingBoxComponent())) {
                                if (otherStone.isActive()) {
                                    otherStone.removeFromWorld();
                                    break;
                                }
                            }
                        }
                    }
                }
                bullet.removeFromWorld();
            }
        });

        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameType.BULLET, GameType.GREENS) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity greens) {
                Entity owner = bullet.getObject("owner");
                Serializable ownerType = owner.getType();
                if (ownerType == GameType.PLAYER && FXGL.geti("playerBulletLevel") == PLAYER_BULLET_MAX_LEVEL) {
                    greens.removeFromWorld();
                }
            }
        });

        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameType.BULLET, GameType.PLAYER) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity player) {
                FXGL.play("normalBomb.wav");
                FXGL.spawn("bombNormal", bullet.getCenter().getX() - 25, bullet.getCenter().getY() - 15);
                bullet.removeFromWorld();
                HealthIntComponent hp = player.getComponent(HealthIntComponent.class);
                hp.damage(1);
                if (hp.isZero()) {
                    //TODO
                    System.out.println("Game Over...");
                }
            }
        });

        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameType.BULLET, GameType.FLAG) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity stone) {
                FXGL.play("normalBomb.wav");
                FXGL.spawn("bombNormal", bullet.getCenter().getX() - 25, bullet.getCenter().getY() - 15);
                bullet.removeFromWorld();
                //TODO
                System.out.println("Game Over...");
            }
        });

        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameType.PLAYER, GameType.REWARD) {
            @Override
            protected void onCollisionBegin(Entity player, Entity reward) {
                String skinName = reward.getString("rewardName");
                if ("tank".equals(skinName)) {
                    FXGL.play("reward.wav");
                    HealthIntComponent hp = player.getComponent(HealthIntComponent.class);
                    if (hp.getValue() < hp.getMaxValue()) {
                        //血量+1
                        hp.damage(-1);
                    }
                }
                if ("ship".equals(skinName)) {
                    FXGL.play("reward.wav");
                    FXGL.set("hasShip", true);
                    if (!player.getViewComponent().getChildren().contains(shipTexture)) {
                        player.getViewComponent().addChild(shipTexture);
                    }

                }
                if ("star".equals(skinName)) {
                    FXGL.play("reward.wav");
                    if (FXGL.geti("playerBulletLevel") <= PLAYER_BULLET_MAX_LEVEL) {
                        FXGL.inc("playerBulletLevel", 1);
                    }
                }
                if ("gun".equals(skinName)) {
                    FXGL.play("reward.wav");
                    FXGL.set("playerBulletLevel", PLAYER_BULLET_MAX_LEVEL);
                }
                if ("time".equals(skinName)) {
                    FXGL.play("reward.wav");
                    FXGL.set("stopTime", true);
                    if (timer == null) {
                        timer = FXGL.newLocalTimer();
                    }
                    timer.capture();

                }
                if ("bomb".equals(skinName)) {
                    List<Entity> enemyList = FXGL.getGameWorld().getEntitiesByType(GameType.ENEMY);
                    FXGL.play("rocketBomb.wav");
                    for (Entity enemy : enemyList) {
                        FXGL.spawn("bombNormal", enemy.getCenter().getX() - 25, enemy.getCenter().getY() - 15);
                        enemy.removeFromWorld();
                    }
                }

                reward.removeFromWorld();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
