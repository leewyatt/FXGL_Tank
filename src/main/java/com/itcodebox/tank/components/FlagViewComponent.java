package com.itcodebox.tank.components;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import javafx.scene.Node;

import java.util.List;

/**
 * @author LeeWyatt
 */
public class FlagViewComponent extends Component {

    private boolean failed;


    public void hitFlag() {
        if (failed) {
            return;
        }
        List<Node> nodes = entity.getViewComponent().getChildren();
        if (nodes.size() > 0) {
            entity.getViewComponent().removeChild(nodes.get(0));
        }
        entity.getViewComponent().addChild(FXGL.texture("map/flag_failed.png"));
        failed = true;
    }

    public boolean isFailed() {
        return failed;
    }
}
