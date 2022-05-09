package com.leewyatt.github.tank.ui;

import com.almasb.fxgl.texture.Texture;
import com.leewyatt.github.tank.Config;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * @author LeeWyatt
 */
public class InfoPane extends Pane {
    public InfoPane() {
        TilePane tilePane = new TilePane(10, 10);
        tilePane.setAlignment(Pos.TOP_LEFT);
        tilePane.setPrefSize(65, 390);
        for (int i = 0; i < Config.ENEMY_AMOUNT; i++) {
            tilePane.getChildren().add(texture("ui/enemy_pre.png"));
        }
        tilePane.setLayoutX(25);
        tilePane.setLayoutY(50);

        Texture levelFlag = texture("ui/levelFlag.png");
        levelFlag.setLayoutX(25);
        levelFlag.setLayoutY(460);

        Text levelText = getUIFactoryService().newText("", Color.BLACK, 43);
        levelText.setLayoutX(38);
        levelText.setLayoutY(520);
        levelText.textProperty().bind(getip("level").asString());

        Texture b1 = texture("ui/bulletLevel.png");
        b1.setVisible(false);
        Texture b2 = texture("ui/bulletLevel.png");
        b2.setVisible(false);
        Texture b3 = texture("ui/bulletLevel.png");
        b3.setVisible(false);
        HBox box = new HBox(5,b1,b2,b3);
        box.setLayoutX(15);
        box.setLayoutY(560);
        ObservableList<Node> bulletLevelNodes = box.getChildren();
        int bulletLevel = geti("playerBulletLevel");
        for (int i = 0; i < bulletLevel; i++) {
            bulletLevelNodes.get(i).setVisible(true);
        }
        getip("playerBulletLevel").addListener((ob, ov, nv) ->{
            for (int i = 0; i < nv.intValue(); i++) {
                bulletLevelNodes.get(i).setVisible(true);
            }
        });

        getChildren().addAll(tilePane, levelFlag, levelText,box);
        setPrefSize(24 * 6, 24 * 28);
        setLayoutX(24 * 28);
        setLayoutY(0);
        setStyle("-fx-background-color: #666666");

        ObservableList<Node> enemyPreNodes = tilePane.getChildren();
        getip("spawnedEnemy").addListener((ob, ov, nv) -> {
            for (int i = enemyPreNodes.size() - 1; i >= Config.ENEMY_AMOUNT - nv.intValue(); i--) {
                enemyPreNodes.get(i).setVisible(false);
            }
        });
    }


}
