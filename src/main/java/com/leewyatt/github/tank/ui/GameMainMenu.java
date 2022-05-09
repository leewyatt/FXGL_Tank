package com.leewyatt.github.tank.ui;

import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.input.view.KeyView;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxgl.ui.DialogService;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;
import static javafx.scene.input.KeyCode.*;

/**
 * @author LeeWyatt
 */
public class GameMainMenu extends FXGLMenu {

    private final TranslateTransition tt;

    public GameMainMenu() {
        super(MenuType.MAIN_MENU);
        Texture texture = texture("ui/logo.png");
        texture.setLayoutX(144);
        texture.setLayoutY(160);

        MainMenuButton newGameBtn = new MainMenuButton("START", this::fireNewGame);
        MainMenuButton helpBtn = new MainMenuButton("HELP", this::instructions);
        MainMenuButton exitBtn = new MainMenuButton("EXIT", ()->getGameController().exit());
        ToggleGroup tg = new ToggleGroup();
        tg.getToggles().addAll(newGameBtn,helpBtn,exitBtn);
        newGameBtn.setSelected(true);
        VBox menuBox = new VBox(
                5,
                newGameBtn,
                helpBtn,
                exitBtn
        );
        menuBox.setAlignment(Pos.CENTER_LEFT);
        menuBox.setLayoutX(240);
        menuBox.setLayoutY(390);
        menuBox.setVisible(false);

        Texture tankTexture = FXGL.texture("ui/tankLoading.png");

        tt = new TranslateTransition(Duration.seconds(2), tankTexture);
        tt.setInterpolator(Interpolators.ELASTIC.EASE_OUT());
        tt.setFromX(172);
        tt.setFromY(252);
        tt.setToX(374);
        tt.setToY(252);
        tt.setOnFinished(e -> menuBox.setVisible(true));

        Rectangle bgRect = new Rectangle(getAppWidth(), getAppHeight());
        Line line = new Line(30, 580, 770, 580);
        line.setStroke(Color.web("#B9340D"));
        line.setStrokeWidth(2);
        Texture textureWall = texture("ui/fxgl.png");
        textureWall.setLayoutX(310);
        textureWall.setLayoutY(600);

        getContentRoot().getChildren().addAll(bgRect, texture, tankTexture, menuBox, line,textureWall);
    }

    @Override
    public void onCreate() {
        FXGL.play("mainMenuLoad.wav");
        tt.play();
    }

    private void instructions() {
        GridPane pane = new GridPane();
        pane.setHgap(20);
        pane.setVgap(15);
        pane.addRow(0, getUIFactoryService().newText("Movement"), new HBox(4, new KeyView(UP), new KeyView(DOWN), new KeyView(LEFT), new KeyView(RIGHT)));
        pane.addRow(1, new Region(), getUIFactoryService().newText("Up,Down,Left,Right"));
        pane.addRow(2, getUIFactoryService().newText("Shoot"), new KeyView(SPACE));
        pane.addRow(3, new Region(), getUIFactoryService().newText("Space"));
        DialogService dialogService = getDialogService();
        dialogService.showBox("Help", pane, getUIFactoryService().newButton("OK"));
    }

}
