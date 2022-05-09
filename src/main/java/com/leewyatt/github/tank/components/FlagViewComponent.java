package com.leewyatt.github.tank.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;

/**
 * @author LeeWyatt
 */
public class FlagViewComponent extends Component {
    public void hitFlag() {
        entity.getViewComponent().clearChildren();
        entity.getViewComponent().addChild(FXGL.texture("map/flag_failed.png"));
    }
}
