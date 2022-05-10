package com.leewyatt.github.tank;

import java.io.Serializable;

/**
 * @author LeeWyatt
 */
public enum GameType implements Serializable {
    /**
     * 游戏实体枚举类
     */
    BRICK, GREENS, FLAG, SEA, SNOW, STONE, PLAYER, ENEMY, BULLET, ITEM, BORDER_WALL, EMPTY
}
