package com.itcodebox.tank.components;

import javafx.geometry.Point2D;

/**
 * @author LeeWyatt
 */
public enum Dir {
    UP(new Point2D(0, -1)), RIGHT(new Point2D(1, 0)), DOWN(new Point2D(0, 1)), LEFT(new Point2D(-1, 0));

    public Point2D vector;

    Dir(Point2D vector) {
        this.vector = vector;
    }

}
