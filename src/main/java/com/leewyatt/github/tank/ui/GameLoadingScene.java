package com.leewyatt.github.tank.ui;

import com.almasb.fxgl.app.scene.LoadingScene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * @author LeeWyatt
 */
public class GameLoadingScene extends LoadingScene {

    public GameLoadingScene() {
        Rectangle rect = new Rectangle(getAppWidth(),getAppHeight(), Color.web("#666666"));
        getContentRoot().getChildren().setAll(rect);
    }
}
