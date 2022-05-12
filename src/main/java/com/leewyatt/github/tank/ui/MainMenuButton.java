package com.leewyatt.github.tank.ui;

import com.almasb.fxgl.texture.Texture;
import javafx.scene.control.RadioButton;
import javafx.scene.input.KeyCode;

import static com.almasb.fxgl.dsl.FXGL.play;
import static com.almasb.fxgl.dsl.FXGL.texture;

/**
 * @author LeeWyatt
 * 主菜单的单选按钮
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
        //如果选择了,才显示前面的坦克图片
        selectedProperty().addListener((ob, ov, nv) -> texture.setVisible(nv));
        //按下Enter,执行这个按钮对应的方法
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                play("select.wav");
                action.run();
            }
        });
        //点击按钮,执行这个按钮对应的方法
        setOnMouseClicked(event -> {
            play("select.wav");
            action.run();
        });
        //鼠标移入,也选算中,播放声音
        setOnMouseEntered(e -> {
                    play("mainMenuHover.wav");
                    setSelected(true);
                }
        );
        //获得焦点就选中.并且播放声音
        focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                play("mainMenuHover.wav");
                setSelected(true);
            }
        });
    }
}
