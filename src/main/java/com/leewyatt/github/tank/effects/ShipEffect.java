package com.leewyatt.github.tank.effects;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.Effect;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.texture.Texture;
import javafx.util.Duration;

/**
 * @author LeeWyatt
 * 玩家获得船的效果
 */
public class ShipEffect extends Effect {
    private final Texture shipTexture;
    public ShipEffect() {
        super(Duration.INDEFINITE);
        shipTexture = FXGL.texture("item/armed_ship.png");
    }

    @Override
    public void onStart(Entity entity) {
        shipTexture.setTranslateX(entity.getWidth()/2-shipTexture.getWidth()/2.0);
        shipTexture.setTranslateY(entity.getHeight()/2-shipTexture.getHeight()/2.0);
        entity.getViewComponent().addChild(shipTexture);
    }
    
    @Override
    public void onEnd(Entity entity) {
        entity.getViewComponent().removeChild(shipTexture);
    }

  
}
