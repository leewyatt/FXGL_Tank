package com.leewyatt.github.tank.ui;

import com.almasb.fxgl.texture.Texture;
import javafx.scene.control.RadioButton;
import javafx.scene.input.KeyCode;

import static com.almasb.fxgl.dsl.FXGL.play;
import static com.almasb.fxgl.dsl.FXGL.texture;

/**
 * @author LeeWyatt
 */
public class MainMenuButton extends RadioButton {

    public MainMenuButton(String text, Runnable action) {
        Texture texture = texture("ui/icon.png");
        texture.setRotate(180);
        texture.setVisible(false);
        setGraphic(texture);
        setGraphicTextGap(30);
        getStyleClass().add("main-menu-btn");
        setText(text);
        selectedProperty().addListener((ob, ov, nv) -> {
            texture.setVisible(nv);
        });

        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                play("select.wav");
                action.run();
            }
        });
        setOnMouseClicked(event -> {
            play("select.wav");
            action.run();
        });

        setOnMouseEntered(e -> {
                    play("mainMenuHover.wav");
                    setSelected(true);
                }
        );
        focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                play("mainMenuHover.wav");
                setSelected(true);
            }
        });
    }
}
