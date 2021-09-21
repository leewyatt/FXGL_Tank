package com.itcodebox.tank.ui;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.input.view.KeyView;
import com.almasb.fxgl.texture.Texture;
import javafx.beans.binding.Bindings;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;
import static javafx.scene.input.KeyCode.*;

/**
 * @author LeeWyatt
 */
public class GameMenu extends FXGLMenu {
    public GameMenu() {
        super(MenuType.MAIN_MENU);
        Texture texture = texture("logo.png");
        texture.setLayoutX(72);
        texture.setLayoutY(160);
        VBox menuBox = new VBox(
                5,
                new LabelButton("New Game", Color.LIGHTGREEN, this::fireNewGame),
                new LabelButton("Help", Color.LIGHTGREEN, this::instructions),
                new LabelButton("Exit", Color.DARKRED, this::fireExit)
        );
        menuBox.setAlignment(Pos.TOP_CENTER);
        menuBox.setLayoutX(180);
        menuBox.setLayoutY(390);
        menuBox.setVisible(false);

        Texture tankTexture = FXGL.texture("tank/H1U.png");
        tankTexture.setRotate(90);
        FXGL.animationBuilder(this)
                .duration(Duration.seconds(1.6))
                .interpolator(Interpolators.ELASTIC.EASE_OUT())
                .onFinished(() -> menuBox.setVisible(true))
                .translate(tankTexture)
                .from(new Point2D(150, 252))
                .to(new Point2D(302, 252))
                .buildAndPlay();

        Text tip = getUIFactoryService().newText("Powered by FXGL game engine",Color.web("#BC4E40"),22);
        tip.setLayoutX(150);
        tip.setLayoutY(590);
        //Background is black
        Rectangle bgRect = new Rectangle(getAppWidth(), getAppHeight());
        getContentRoot().getChildren().addAll(bgRect, texture, tankTexture, menuBox,tip);
    }

    private static class LabelButton extends Label {
        LabelButton(String name, Color hoverColor, Runnable action) {
            setText(name);
            setTextFill(Color.WHITE);
            setFont(FXGL.getAssetLoader().loadFont("airstrikeacad.ttf").newFont(50));
            textFillProperty().bind(
                    Bindings.when(hoverProperty())
                            .then(hoverColor)
                            .otherwise(Color.WHITE)
            );
            setOnMouseClicked(e -> action.run());
        }
    }

    private void instructions() {
        GridPane pane = new GridPane();
        pane.setHgap(20);
        pane.setVgap(15);
        pane.addRow(0, getUIFactoryService().newText("Movement"), new HBox(4, new KeyView(UP), new KeyView(DOWN), new KeyView(LEFT), new KeyView(RIGHT)));
        pane.addRow(1, new Region(), getUIFactoryService().newText("Up,Down,Left,Right"));
        pane.addRow(2, getUIFactoryService().newText("Shoot"), new KeyView(SPACE));
        pane.addRow(3, new Region(), getUIFactoryService().newText("Space"));
        getDialogService().showBox("Help", pane, getUIFactoryService().newButton("OK"));
    }



}
