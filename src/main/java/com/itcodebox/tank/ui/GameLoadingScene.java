package com.itcodebox.tank.ui;

import com.almasb.fxgl.app.scene.LoadingScene;
import com.itcodebox.tank.Config;
import javafx.scene.shape.Rectangle;

/**
 * @author LeeWyatt
 */
public class GameLoadingScene extends LoadingScene {

    public GameLoadingScene() {
        Rectangle rect = new Rectangle(getAppWidth(),getAppHeight(), Config.BG_GARY);

        getContentRoot().getChildren().setAll(rect);

    }


}
