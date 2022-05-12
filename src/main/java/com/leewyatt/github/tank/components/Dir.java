package com.leewyatt.github.tank.components;

import javafx.geometry.Point2D;

/**
 * @author LeeWyatt
 */
public enum Dir {
    /**
     * 运动方向
     */
    UP(new Point2D(0, -1)),
    RIGHT(new Point2D(1, 0)),
    DOWN(new Point2D(0, 1)),
    LEFT(new Point2D(-1, 0));

    public final Point2D vector;

    Dir(Point2D vector) {
        this.vector = vector;
    }

    public Point2D getVector() {
        return vector;
    }

}
