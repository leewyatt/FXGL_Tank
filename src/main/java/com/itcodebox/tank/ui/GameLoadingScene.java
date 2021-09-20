package com.itcodebox.tank.ui;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.scene.LoadingScene;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.scene.Scene;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxgl.ui.ProgressBar;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;

/**
 * @author LeeWyatt
 */
public class GameLoadingScene extends LoadingScene {

    public GameLoadingScene() {
        Texture texture = FXGL.texture("loadingBg.png");
        Text text = FXGL.getUIFactoryService().newText("Loading level", Color.WHITE, 46.0);
        FXGL.centerText(text, getAppWidth() / 2.0, getAppHeight() / 2.0);
        Texture tankTexture = FXGL.texture("tank/H1U.png");
        tankTexture.setRotate(90);
        FXGL.animationBuilder(this)
                .duration(Duration.seconds(.8))
                .interpolator(Interpolators.ELASTIC.EASE_OUT())
                .translate(tankTexture)
                .from(new Point2D(150, 300))
                .to(new Point2D(310, 300))
                .buildAndPlay();

        getContentRoot().getChildren().setAll(texture, text,tankTexture);

    }


}
