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
import com.almasb.fxgl.entity.components.BoundingBoxComponent;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxgl.time.LocalTimer;
import com.almasb.fxgl.time.TimerAction;
import com.itcodebox.tank.components.FlagViewComponent;
import com.itcodebox.tank.components.PlayerViewComponent;
import com.itcodebox.tank.ui.GameLoadingScene;
import com.itcodebox.tank.ui.GameMenu;
import javafx.animation.*;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author LeeWyatt
 */
public class TankApp extends GameApplication {

    /**
     * 目前地图就制作了两关
     */
    private int MAX_LEVEL = 2;
    private int hasGeneratedEnemy;
    private Entity player;
    private PlayerViewComponent playerView;
    private Random random = new Random();
    private String[] rewardNames = {"gun", "star", "tank", "time", "ship", "bomb", "helmet", "spade"};
    //private String[] rewardNames = {"gun", "gun"};
    private int[] spawnX = {30, 295 + 30, 589 + 20, 130 + 30, 424 + 30};
    /**
     * 可能同时击中的元素
     */
    private LazyValue<List<Entity>> blocksValue = new LazyValue<List<Entity>>(() -> FXGL.getGameWorld().getEntitiesByType(GameType.STONE, GameType.BRICK, GameType.GREENS));
    /**
     * tank无敌计时器
     */
    private LocalTimer helmetTimer;
    /**
     * home加固计时器
     */
    private LocalTimer spadeTimer;
    /**
     * 敌军冻结计时器
     */
    private LocalTimer freezingTimer;
    /**
     * 定时刷新敌军坦克
     */
    private TimerAction spawnEnemyTimerAction;

    @Override
    protected void onUpdate(double tpf) {
        if (freezingTimer != null) {
            if (freezingTimer.elapsed(Config.STOP_MOVE_TIME)) {
                FXGL.set("freezingEnemy", false);
                freezingTimer = null;
            }
        }

        if (helmetTimer != null) {
            if (helmetTimer.elapsed(Config.HELMET_TIME)) {
                FXGL.set("armedHelmet", false);
                helmetTimer = null;
            }
        }

        if (spadeTimer != null) {
            if (spadeTimer.elapsed(Config.SPADE_TIME)) {
                resetWall();
                spadeTimer = null;
            }
        }

    }

    private void resetWall() {
        buildWall("brick");
    }

    private void updateWall() {
        buildWall("stone");
    }

    private void buildWall(String entityName) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                if (row != 0 && (col == 1 || col == 2)) {
                    continue;
                }
                //remove old wall
                List<Entity> entityTempList = FXGL.getGameWorld().getEntitiesAt(new Point2D(288 + col * 24, 576 + row * 24));
                for (Entity entityTemp : entityTempList) {
                    Serializable type = entityTemp.getType();
                    //如果是玩家自建的地图, 那么需要判断是不是水面草地雪地等
                    if (type == GameType.STONE || type == GameType.BRICK || type == GameType.SNOW || type == GameType.SEA || type == GameType.GREENS) {
                        if (entityTemp.isActive()) {
                            entityTemp.removeFromWorld();
                        }
                    }
                }
                //create new wall
                if ("stone".equals(entityName)) {
                    FXGL.spawn(entityName, new SpawnData(288 + col * 24, 576 + row * 24).put("rewardStone", true));
                } else {
                    FXGL.spawn(entityName, new SpawnData(288 + col * 24, 576 + row * 24));
                }

            }
        }
    }

    @Override
    protected void initSettings(GameSettings settings) {
        //26cell * 24px
        settings.setWidth(28 * 24);
        settings.setHeight(28 * 24);
        settings.setFontUI("airstrikeacad.ttf");
        settings.setTitle("90 Tank");
        settings.setAppIcon("icon.png");
        settings.setVersion("Version 0.2");
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
        vars.put("level", 1);
        vars.put("playerBulletLevel", 0);
        vars.put("armedHelmet", true);
        vars.put("hasSpade", false);
        vars.put("freezingEnemy", false);
        vars.put("destroyedEnemy", 0);
        vars.put("gameOver", false);
    }

    @Override
    protected void initInput() {
        Input input = FXGL.getInput();
        input.addAction(new UserAction("Move Up") {
            @Override
            protected void onAction() {
                if (player != null && !FXGL.getb("gameOver") && player.isActive()) {
                    playerView.up();
                }
            }
        }, KeyCode.UP);
        input.addAction(new UserAction("Move Down") {
            @Override
            protected void onAction() {
                if (player != null && !FXGL.getb("gameOver") && player.isActive()) {
                    playerView.down();
                }
            }
        }, KeyCode.DOWN);
        input.addAction(new UserAction("Move Left") {
            @Override
            protected void onAction() {
                if (player != null && !FXGL.getb("gameOver") && player.isActive()) {
                    playerView.left();
                }
            }
        }, KeyCode.LEFT);
        input.addAction(new UserAction("Move Right") {
            @Override
            protected void onAction() {
                if (player != null && !FXGL.getb("gameOver") && player.isActive()) {
                    playerView.right();
                }
            }
        }, KeyCode.RIGHT);

        input.addAction(new UserAction("Shoot") {
            @Override
            protected void onAction() {
                if (player != null && !FXGL.getb("gameOver") && player.isActive()) {
                    playerView.shoot();
                }
            }
        }, KeyCode.SPACE);

    }

    @Override
    protected void initGame() {

        FXGL.getGameScene().setBackgroundColor(Color.BLACK);
        FXGL.getGameWorld().addEntityFactory(new GameEntityFactory());
        buildAndStartLevel();
        FXGL.getip("destroyedEnemy").addListener((ob, ov, nv) -> {
            if (nv.intValue() == Config.MAX_ENEMY_NUM) {
                FXGL.set("gameOver",true);
                buildWinAnim();
            }
        });

    }

    private void buildAndStartLevel() {
        //1. 清理上一个关卡的残留(这里主要是清理声音残留)
        clearEntitiesByTpe(GameType.BULLET, GameType.ENEMY, GameType.PLAYER);

        //2. 开场动画
        Rectangle rect1 = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight() / 2.0, Config.BG_GARY);
        Rectangle rect2 = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight() / 2.0, Config.BG_GARY);
        rect2.setLayoutY(FXGL.getAppHeight() / 2.0);
        Text text = new Text("STAGE " + FXGL.geti("level"));
        text.setFont(new Font(35));
        text.setLayoutX(FXGL.getAppWidth() / 2.0 - 80);
        text.setLayoutY(FXGL.getAppHeight() / 2.0 - 5);
        Pane p1 = new Pane(rect1, rect2, text);

        FXGL.addUINode(p1);

        Timeline tl = new Timeline(
                new KeyFrame(Duration.seconds(1.2),
                        new KeyValue(rect1.translateYProperty(), -FXGL.getAppHeight() / 2.0),
                        new KeyValue(rect2.translateYProperty(), FXGL.getAppHeight() / 2.0)
                ));
        tl.setOnFinished(e -> FXGL.removeUINode(p1));

        PauseTransition pt = new PauseTransition(Duration.seconds(1.5));
        pt.setOnFinished(e -> {
            text.setVisible(false);
            tl.play();
            //3. 开始新关卡
            startLevel();
        });
        pt.play();
    }

    private void buildFailedAnim() {
        Texture gameOverTexture = FXGL.texture("GameOver.png");
        gameOverTexture.setTranslateY(FXGL.getAppHeight() - gameOverTexture.getHeight() + 24);
        gameOverTexture.setTranslateX(FXGL.getAppWidth() / 2.0 - gameOverTexture.getWidth() / 2.0);
        FXGL.addUINode(gameOverTexture);
        TranslateTransition tt = new TranslateTransition(Duration.seconds(5), gameOverTexture);
        tt.setToY(FXGL.getAppHeight() / 2.0 - gameOverTexture.getHeight() / 2);
        tt.setOnFinished(e -> {
            FXGL.removeUINode(gameOverTexture);
            //清理关卡的残留(这里主要是清理声音残留)
            clearEntitiesByTpe(GameType.BULLET, GameType.ENEMY, GameType.PLAYER);
            FXGL.getGameController().gotoMainMenu();
        });
        tt.play();
    }

    private void buildWinAnim() {
        clearEntitiesByTpe(GameType.BULLET, GameType.ENEMY, GameType.PLAYER);
        Rectangle rect = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight(), Config.BG_GARY);

        Text hiText = new Text("HI-SCORE");
        hiText.setFont(Font.font(30));
        hiText.setFill(Color.web("#B53021"));
        hiText.setLayoutY(260);
        hiText.setLayoutX(150);
        Text scoreText = new Text("20000");
        scoreText.setFont(Font.font(30));
        scoreText.setFill(Color.web("#EAA024"));
        scoreText.setLayoutY(260);
        scoreText.setLayoutX(400);
        Text levelText = new Text("STAGE " + FXGL.geti("level"));
        levelText.setFont(Font.font(25));
        levelText.setFill(Color.web("#EAA024"));
        levelText.setLayoutY(360);
        levelText.setLayoutX(275);

        FXGL.getGameScene().addUINode(rect);
        FXGL.getGameScene().addUINode(hiText);
        FXGL.getGameScene().addUINode(scoreText);
        FXGL.getGameScene().addUINode(levelText);

        FXGL.runOnce(() -> {
            if (FXGL.geti("level")==MAX_LEVEL) {
                FXGL.getGameScene().clearUINodes();
                FXGL.getGameController().gotoMainMenu();
            }else {
                FXGL.getGameScene().clearUINodes();
                FXGL.inc("level", 1);
                buildAndStartLevel();
            }

        }, Duration.seconds(1.2));
    }

    private void clearEntitiesByTpe(GameType... types) {
        List<Entity> entities = FXGL.getGameWorld().getEntitiesByType(types);
        for (Entity entity : entities) {
            entity.removeFromWorld();
        }
    }

    private void startLevel() {
        if (spawnEnemyTimerAction != null) {
            spawnEnemyTimerAction.expire();
            spawnEnemyTimerAction = null;
        }
        FXGL.set("gameOver", false);
        //每局开始都有无敌保护时间
        FXGL.set("armedHelmet", true);
        //清除上一关残留的道具影响
        FXGL.set("freezingEnemy", false);
        //恢复消灭敌军数量
        FXGL.set("destroyedEnemy", 0);

        freezingTimer = null;
        spadeTimer = null;
        helmetTimer = FXGL.newLocalTimer();
        helmetTimer.capture();

        FXGL.setLevelFromMap("level" + FXGL.geti("level") + ".tmx");
        FXGL.play("start.wav");
        player = null;
        player = FXGL.spawn("player", 9 * 24, 25 * 24);
        playerView = player.getComponent(PlayerViewComponent.class);
        //首先产生几个
        for (int i = 0; i < spawnX.length; i++) {
            FXGL.spawn("enemy",
                    new SpawnData(spawnX[i], 30).put("assentName", "tank/E" + FXGLMath.random(1, 12) + "U.png"));
            hasGeneratedEnemy++;
        }
        spawnEnemy();
    }

    private void spawnEnemy() {
        if (spawnEnemyTimerAction != null) {
            spawnEnemyTimerAction.expire();
            spawnEnemyTimerAction = null;
        }
        Entity rectEntity = FXGL.spawn("empty", new SpawnData(-100, -100));
        spawnEnemyTimerAction = FXGL.run(() -> {
            //尝试次数
            int testTimes = random.nextInt(2) + 2;
            for (int i = 0; i < testTimes; i++) {
                if (hasGeneratedEnemy < Config.MAX_ENEMY_NUM) {
                    boolean canGenerate = true;
                    int x = spawnX[random.nextInt(3)];
                    int y = 30;
                    rectEntity.setPosition(x, y);
                    List<Entity> tankList = FXGL.getGameWorld().getEntitiesByType(GameType.ENEMY, GameType.PLAYER);
                    BoundingBoxComponent emptyBox = rectEntity.getBoundingBoxComponent();
                    for (Entity tank : tankList) {
                        if (tank.isActive() && emptyBox.isCollidingWith(tank.getBoundingBoxComponent())) {
                            canGenerate = false;
                            break;
                        }
                    }
                    if (canGenerate) {
                        hasGeneratedEnemy++;
                        FXGL.spawn("enemy",
                                new SpawnData(x, y).put("assentName", "tank/E" + FXGLMath.random(1, 12) + "U.png"));
                    }
                    rectEntity.setPosition(-100, -100);

                } else {
                    if (spawnEnemyTimerAction != null) {
                        spawnEnemyTimerAction.expire();
                    }
                }
            }
        }, Config.GENERATE_ENEMY_TIME);
    }

    @Override
    protected void initPhysics() {
        bulletHitEnemy();
        bulletHitPlayer();
        bulletHitBrick();
        bulletHitSton();
        bulletHitGreens();
        bulletHitFlag();
        bulletHitBorderWall();
        bulletHitBullet();
        playerGetReward();
    }

    private void bulletHitBullet() {
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameType.BULLET, GameType.BULLET) {
            @Override
            protected void onCollisionBegin(Entity b1, Entity b2) {
                Entity owner1 = b1.getObject("owner");
                Serializable type1 = owner1.getType();

                Entity owner2 = b2.getObject("owner");
                Serializable type2 = owner2.getType();
                if (type1 != type2) {
                    b1.removeFromWorld();
                    b2.removeFromWorld();
                }
            }
        });
    }

    private void bulletHitBorderWall() {
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameType.BULLET, GameType.BORDER_WALL) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity border) {
                bullet.removeFromWorld();
            }
        });
    }

    private void playerGetReward() {
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameType.PLAYER, GameType.REWARD) {
            @Override
            protected void onCollisionBegin(Entity player, Entity reward) {
                String rewardName = reward.getString("rewardName");
                if ("bomb".equals(rewardName)) {
                    List<Entity> enemyList = FXGL.getGameWorld().getEntitiesByType(GameType.ENEMY);
                    FXGL.play("rocketBomb.wav");
                    for (Entity enemy : enemyList) {
                        FXGL.spawn("bomb", enemy.getCenter().getX() - 25, enemy.getCenter().getY() - 20);
                        enemy.removeFromWorld();
                        FXGL.inc("destroyedEnemy", 1);
                    }
                    reward.removeFromWorld();
                    return;
                }

                FXGL.play("reward.wav");
                if ("tank".equals(rewardName)) {
                    HealthIntComponent hp = player.getComponent(HealthIntComponent.class);
                    if (hp.getValue() < hp.getMaxValue()) {
                        hp.damage(-1);
                    }
                }
                if ("ship".equals(rewardName)) {
                    playerView.setArmedShip(true);
                }

                if ("star".equals(rewardName)) {
                    if (FXGL.geti("playerBulletLevel") < Config.PLAYER_BULLET_MAX_LEVEL) {
                        FXGL.inc("playerBulletLevel", 1);
                    }
                }

                if ("gun".equals(rewardName)) {
                    FXGL.set("playerBulletLevel", Config.PLAYER_BULLET_MAX_LEVEL);
                }

                if ("time".equals(rewardName)) {
                    FXGL.set("freezingEnemy", true);
                    if (freezingTimer == null) {
                        freezingTimer = FXGL.newLocalTimer();
                    }
                    freezingTimer.capture();
                }

                if ("helmet".equals(rewardName)) {
                    FXGL.set("armedHelmet", true);
                    if (helmetTimer == null) {
                        helmetTimer = FXGL.newLocalTimer();
                    }
                    helmetTimer.capture();
                }

                if ("spade".equals(rewardName)) {
                    updateWall();
                    if (spadeTimer == null) {
                        spadeTimer = FXGL.newLocalTimer();
                    }
                    spadeTimer.capture();
                }
                reward.removeFromWorld();
            }
        });
    }

    private void bulletHitFlag() {
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameType.BULLET, GameType.FLAG) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity flag) {
                FlagViewComponent flagComponent = flag.getComponent(FlagViewComponent.class);
                if (!flagComponent.isFailed()) {
                    FXGL.play("normalBomb.wav");
                    FXGL.spawn("bomb", bullet.getCenter().getX() - 25, bullet.getCenter().getY() - 20);
                    bullet.removeFromWorld();
                    if (!FXGL.getb("gameOver")) {
                        FXGL.set("gameOver", true);
                        buildFailedAnim();
                    }
                }
                flagComponent.hitFlag();
            }
        });
    }

    private void bulletHitPlayer() {
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameType.BULLET, GameType.PLAYER) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity player) {
                FXGL.play("normalBomb.wav");
                if (FXGL.getb("armedHelmet")) {
                    bullet.removeFromWorld();
                    return;
                }

                FXGL.spawn("bomb", bullet.getCenter().getX() - 25, bullet.getCenter().getY() - 20);
                bullet.removeFromWorld();
                HealthIntComponent hp = player.getComponent(HealthIntComponent.class);
                hp.damage(1);
                if (hp.isZero()) {
                    if (!FXGL.getb("gameOver")) {
                        player.removeFromWorld();
                        FXGL.set("gameOver", true);
                        buildFailedAnim();
                    }
                }
            }
        });
    }

    private void bulletHitGreens() {
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameType.BULLET, GameType.GREENS) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity greens) {
                Entity owner = bullet.getObject("owner");
                Serializable ownerType = owner.getType();
                if (ownerType == GameType.PLAYER && FXGL.geti("playerBulletLevel") == Config.PLAYER_BULLET_MAX_LEVEL) {
                    greens.removeFromWorld();
                }
                bulletHit(bullet, false);
            }
        });
    }

    private void bulletHitSton() {
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameType.BULLET, GameType.STONE) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity stone) {
                Entity owner = bullet.getObject("owner");
                Serializable ownerType = owner.getType();
                if (ownerType == GameType.PLAYER && FXGL.geti("playerBulletLevel") == Config.PLAYER_BULLET_MAX_LEVEL) {
                    stone.removeFromWorld();
                }
                bulletHit(bullet, true);
            }
        });
    }

    private void bulletHitBrick() {
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameType.BULLET, GameType.BRICK) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity brick) {
                //移除其中一个
                brick.removeFromWorld();
                //判断,如果还有砖块被击中. 那么也要判断是否移除
                bulletHit(bullet, true);
            }
        });
    }

    private void bulletHitEnemy() {
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameType.BULLET, GameType.ENEMY) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity enemy) {
                FXGL.play("normalBomb.wav");
                FXGL.spawn("bomb", enemy.getCenter().getX() - 25, enemy.getCenter().getY() - 20);
                bullet.removeFromWorld();
                enemy.removeFromWorld();
                FXGL.inc("destroyedEnemy", 1);
                // get reward
                if (random.nextInt(10) > 2) {
                    FXGL.spawn("reward",
                            new SpawnData(FXGLMath.random(50, FXGL.getAppWidth() - 50), FXGLMath.random(50, FXGL.getAppHeight() - 50))
                                    .put("rewardName", rewardNames[random.nextInt(rewardNames.length)]));
                }
            }
        });
    }

    private void bulletHit(Entity bullet, boolean removeBullet) {
        Entity tank = bullet.getObject("owner");
        Serializable tankType = tank.getType();
        List<Entity> list = blocksValue.get();
        for (Entity entity : list) {
            Serializable entityType = entity.getType();
            if (entityType == GameType.BRICK) {
                if (entity.isActive()
                        && bullet.getBoundingBoxComponent().isCollidingWith(entity.getBoundingBoxComponent())) {
                    entity.removeFromWorld();
                    removeBullet = true;
                    break;
                }
            } else if (entityType == GameType.GREENS) {
                if (tankType == GameType.PLAYER
                        && entity.isActive()
                        && bullet.getBoundingBoxComponent().isCollidingWith(entity.getBoundingBoxComponent())
                        && FXGL.geti("playerBulletLevel") == Config.PLAYER_BULLET_MAX_LEVEL) {
                    entity.removeFromWorld();
                    break;
                }
            } else if (entityType == GameType.STONE) {
                if (tankType == GameType.PLAYER
                        && entity.isActive()
                        && bullet.getBoundingBoxComponent().isCollidingWith(entity.getBoundingBoxComponent())
                        && FXGL.geti("playerBulletLevel") == Config.PLAYER_BULLET_MAX_LEVEL) {
                    entity.removeFromWorld();
                    removeBullet = true;
                    break;
                }
            }
        }
        if (removeBullet) {
            bullet.removeFromWorld();
            FXGL.play("normalBomb.wav");
            FXGL.spawn("bomb", bullet.getCenter().getX() - 25, bullet.getCenter().getY() - 20);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
