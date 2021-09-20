package com.itcodebox.tank;

import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.LoadingScene;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.HealthIntComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.itcodebox.tank.components.PlayerViewComponent;
import com.itcodebox.tank.ui.GameLoadingScene;
import com.itcodebox.tank.ui.GameMenu;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;

/**
 * @author LeeWyatt
 */
public class TankApp extends GameApplication {

    private PlayerViewComponent playerView;

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
            @NotNull
            @Override
            public FXGLMenu newMainMenu() {
                return new GameMenu();
            }
            @NotNull
            @Override
            public LoadingScene newLoadingScene() {
                return new GameLoadingScene();
            }
        });
        //开发模式.这样可以输出较多的日志异常追踪
        settings.setApplicationMode(ApplicationMode.DEVELOPER);
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
        FXGL.setLevelFromMap("level2.tmx");
        for (int i = 0; i < 12; i++) {
            FXGL.spawn("enemy",
                    new SpawnData(24 + i * 40, 20).put("assentName", "tank/E" + FXGLMath.random(1, 12) + "U.png"));
        }
        Entity player = FXGL.spawn("player", 8 * 24, 24 * 24);
        playerView = player.getComponent(PlayerViewComponent.class);
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
            }
        };

        FXGL.getPhysicsWorld().addCollisionHandler(collisionHandler);

        FXGL.getPhysicsWorld().addCollisionHandler(collisionHandler.copyFor(GameType.BULLET, GameType.BRICK));

        //如果子弹同时击中两个砖块. 那么可以清除2个砖块;
        //If the bullet hits two bricks at the same time, then 2 bricks can be cleared;
        //FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameType.BULLET,GameType.BRICK) {
        //    @Override
        //    protected void onCollisionBegin(Entity bullet, Entity brick) {
        //        FXGL.play("normalBomb.wav");
        //        FXGL.spawn("bombNormal",bullet.getCenter().getX()-15,bullet.getCenter().getY()-15);
        //        List<Entity> brickList = bricksValue.get();
        //        for (Entity entity : brickList) {
        //            if (entity.isActive()) {
        //                if (bullet.getBoundingBoxComponent().isCollidingWith(entity.getBoundingBoxComponent())) {
        //                    if (entity.isActive()) {
        //                        entity.removeFromWorld();
        //                    }
        //                }
        //            }
        //        }
        //        bullet.removeFromWorld();
        //    }
        //});

        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameType.BULLET, GameType.STONE) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity stone) {
                FXGL.play("normalBomb.wav");
                FXGL.spawn("bombNormal", bullet.getCenter().getX() - 25, bullet.getCenter().getY() - 15);
                bullet.removeFromWorld();
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
    }

    public static void main(String[] args) {
        launch(args);
    }
}
