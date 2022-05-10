package com.leewyatt.github.tank.ui;

import com.leewyatt.github.tank.GameConfig;
import com.leewyatt.github.tank.GameType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.ImageCursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.almasb.fxgl.dsl.FXGL.*;

/**
 * @author LeeWyatt
 */
public class ConstructPane extends BorderPane {

    private Canvas canvas;
    /**
     * width=24  height=24
     */
    private static final int CELL_SIZE = 24;

    /**
     * map real row = 26+ (top border wall)1 + 1(bottom border wall) = 28
     * map real col = 26+ (left border wall)1 + 1(right border wall) = 28
     */
    private static final int ROW = 26;
    private static final int COL = 26;
    private final ImageView previewImgView;
    private GameType[][] map = new GameType[ROW][COL];
    private GameType gameType = GameType.BRICK;
    private boolean isBig;
    private boolean showGrid = false;
    private int idIndex = 100;

    private int layoutX;
    private int layoutY;

    public ConstructPane() {
        getStyleClass().add("construct-pane");
        setPrefSize(getAppWidth(), getAppHeight());
        previewImgView = new ImageView(image("map/" + gameType.toString().toLowerCase(Locale.ROOT) + ".png"));
        previewImgView.setMouseTransparent(true);
        initMapData();
        setCenter(initCenterPane());
        setRight(initRightBox());
    }

    private VBox initRightBox() {
        TilePane tp = initTilesButtonPane();
        VBox.setMargin(tp, new Insets(30, 0, 0, 0));
        VBox bottomBox = initBottomBox();
        VBox rightBox = new VBox(10);
        rightBox.getStyleClass().add("right-box");
        rightBox.setAlignment(Pos.TOP_CENTER);
        rightBox.getChildren().addAll(tp, bottomBox);
        rightBox.setPrefSize(6 * CELL_SIZE, getAppHeight());
        return rightBox;
    }

    private VBox initBottomBox() {
        CheckBox gridCheckBox = new CheckBox("Show Grid");
        gridCheckBox.getStyleClass().add("grid-check-box");
        gridCheckBox.setAlignment(Pos.CENTER);
        gridCheckBox.setSelected(showGrid);
        gridCheckBox.selectedProperty().addListener((ob, ov, nv) -> {
            showGrid = nv;
            drawMap();
        });

        Button btnBack = new Button("Back to Menu");
        btnBack.setGraphic(new Region());
        btnBack.setId("btn-back");
        btnBack.setOnAction(event -> {
            saveConstructMap(false);
            getGameController().gotoMainMenu();
        });

        Button btnClear = new Button("Clear Tiles");
        btnClear.setGraphic(new Region());
        btnClear.setId("btn-clear");
        btnClear.setOnAction(event -> {
            resetMapData();
            drawMap();
        });

        Button btnLoad = new Button("Reload Map");
        btnLoad.setGraphic(new Region());
        btnLoad.setId("btn-reload");
        btnLoad.setOnAction(event -> {
            initMapData();
            drawMap();
        });

        Button btnStart = new Button("Start Game");
        btnStart.setGraphic(new Region());
        btnStart.setId("btn-start");
        btnStart.setOnAction(event -> {
            saveConstructMap(true);
        });
        VBox bottomBox = new VBox(15, gridCheckBox, btnBack, btnClear,btnLoad, btnStart);
        bottomBox.getStyleClass().add("bottom-box");
        VBox.setMargin(bottomBox, new Insets(20, 0, 0, 0));
        return bottomBox;
    }

    private TilePane initTilesButtonPane() {
        Button btnBrick = creatTileBtn(GameType.BRICK, false);
        Button btnBrick4 = creatTileBtn(GameType.BRICK, true);
        Button btnStone = creatTileBtn(GameType.STONE, false);
        Button btnStone4 = creatTileBtn(GameType.STONE, true);
        Button btnSnow = creatTileBtn(GameType.SNOW, false);
        Button btnSnow4 = creatTileBtn(GameType.SNOW, true);
        Button btnSea = creatTileBtn(GameType.SEA, false);
        Button btnSea4 = creatTileBtn(GameType.SEA, true);
        Button btnGreens = creatTileBtn(GameType.GREENS, false);
        Button btnGreens4 = creatTileBtn(GameType.GREENS, true);
        Button btnEmpty = creatTileBtn(GameType.EMPTY, false);
        Button btnEmpty4 = creatTileBtn(GameType.EMPTY, true);

        TilePane tp = new TilePane(
                btnBrick, btnBrick4, btnStone, btnStone4, btnSnow, btnSnow4, btnSea, btnSea4, btnGreens, btnGreens4, btnEmpty, btnEmpty4
        );
        tp.setVgap(8);
        return tp;
    }

    private StackPane initCenterPane() {
        canvas = new Canvas(COL * CELL_SIZE, ROW * CELL_SIZE);
        canvas.setCursor(new ImageCursor(image("ui/drawCursor.png")));
        canvasAddMouseAction();
        previewImgView.setOpacity(0.8);
        Pane prePane = new Pane(previewImgView);
        prePane.setMouseTransparent(true);
        prePane.setClip(new Rectangle(canvas.getWidth(), canvas.getHeight()));
        StackPane centerPane = new StackPane(canvas, prePane);
        centerPane.setMaxSize(canvas.getWidth(), canvas.getHeight());
        setCenter(centerPane);
        drawMap();
        return centerPane;
    }

    private void saveConstructMap(boolean startGame) {
        List<String> list = new ArrayList<>(text("levelStart.txt"));
        String grid = "";
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[j][i] != GameType.EMPTY) {
                    if (map[j][i] == GameType.BRICK) {
                        grid = " gid=\"1\"";
                    } else if (map[j][i] == GameType.SEA) {
                        grid = "";
                    } else if (map[j][i] == GameType.SNOW) {
                        grid = " gid=\"3\"";
                    } else if (map[j][i] == GameType.STONE) {
                        grid = " gid=\"4\"";
                    } else if (map[j][i] == GameType.GREENS) {
                        grid = " gid=\"5\"";
                        ;
                    }
                    //因为有边框,所以x方向是 j+1.
                    //object 里的图像对象在Tiled软件里记录是左下角的坐标,而不是左上角的坐标,所以 i+1+1
                    list.add(String.format(
                            "  <object id=\"%d\" type=\"%s\"%s x=\"%d\" y=\"%d\" width=\"24\" height=\"24\"/>",
                            idIndex++, map[j][i].toString().toLowerCase(Locale.ROOT), grid,
                            (j + 1) * CELL_SIZE, (i + 2) * CELL_SIZE));
                }
            }
        }
        list.addAll(text("levelEnd.txt"));
        //保存地图tmx文件,并开始游戏
        getFileSystemService()
                .writeDataTask(list, GameConfig.CUSTOM_LEVEL_PATH)
                .onSuccess(e -> {
                    //TankApp appCast = getAppCast();
                    //appCast.initLevel = 0;
                    set("level", 0);
                    if (startGame) {
                        getGameController().startNewGame();
                    }
                    saveMapData();
                })
                .run();
    }

    private void saveMapData() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(GameConfig.CUSTOM_LEVEL_DATA));
            out.writeObject(map);
            out.flush();
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadMapData() {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(GameConfig.CUSTOM_LEVEL_DATA));
            map = (GameType[][]) in.readObject();
            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initMapData() {
        boolean exists = getFileSystemService().exists(GameConfig.CUSTOM_LEVEL_DATA);
        if (exists) {
            loadMapData();
            return;
        }
        resetMapData();
    }

    private void resetMapData() {
        //default empty 默认值为空
        for (GameType[] gameTypes : map) {
            Arrays.fill(gameTypes, GameType.EMPTY);
        }
        //对基地周围默认有墙
        map[11][23] = GameType.BRICK;
        map[12][23] = GameType.BRICK;
        map[13][23] = GameType.BRICK;
        map[14][23] = GameType.BRICK;
        map[11][24] = GameType.BRICK;
        map[11][25] = GameType.BRICK;
        map[14][24] = GameType.BRICK;
        map[14][25] = GameType.BRICK;
    }

    /**
     * Mouse handling on canvas 画布上面的鼠标处理
     */
    private void canvasAddMouseAction() {
        canvas.setOnMouseClicked(e -> updateMapView(e.getX(), e.getY()));
        canvas.setOnMouseDragged(e -> updateMapView(e.getX(), e.getY()));
        canvas.setOnMouseExited(e -> {
            previewImgView.setLayoutX(-100);
            previewImgView.setLayoutY(-100);
        });
        canvas.setOnMouseMoved(e -> {
            int x = (int) e.getX() / CELL_SIZE;
            int y = (int) e.getY() / CELL_SIZE;
            if (x < 0 || y < 0 || x > 25 || y > 25) {// 拖动的话,鼠标可能移除界面
                return;
            }
            // 鼠标位置的绘制
            previewImgView.setImage(image("map/" + this.gameType.toString().toLowerCase(Locale.ROOT) + (isBig ? "4" : "") + ".png"));
            previewImgView.setLayoutX(x * CELL_SIZE);
            previewImgView.setLayoutY(y * CELL_SIZE);
            layoutX = x * CELL_SIZE;
            layoutY = y * CELL_SIZE;
        });
    }

    private void updateMapView(double dx, double dy) {
        int x = (int) dx / 24;
        int y = (int) dy / 24;
        if (x < 0 || y < 0 || x > 25 || y > 25) {// 虽然这里的移动没有问题,但如果是拖动的话,鼠标可能移除界面出现负数或者大数
            return;
        }
        // 鼠标位置的赋值 (基地位置除外)
        if (!(x >= 12 && x <= 13 && y >= 24)) {
            map[x][y] = gameType;
        }
        // 如果是大图,还需要判断其他3个位置是否可以赋值
        if (isBig) {
            if (x + 1 <= 25 && !(x + 1 >= 12 && x + 1 <= 13 && y >= 24)) {
                map[x + 1][y] = gameType;
            }
            if (y + 1 <= 25 && !(x >= 12 && x <= 13 && y + 1 >= 24)) {
                map[x][y + 1] = gameType;
            }
            if (x + 1 <= 25 && y + 1 <= 25 && !(x + 1 >= 12 && x + 1 <= 13 && y + 1 >= 24)) {
                map[x + 1][y + 1] = gameType;
            }
        }
        drawMap();
        if (gameType == GameType.EMPTY) {
            previewImgView.setLayoutX(x * CELL_SIZE);
            previewImgView.setLayoutY(y * CELL_SIZE);
        }
    }

    /**
     * @param type  Tile Image Type/ GameType
     * @param isBig true 2*2 big image ; false 1*1 normal image
     * @return Img Button
     */
    private Button creatTileBtn(GameType type, boolean isBig) {
        Button btn = new Button();
        btn.getStyleClass().add("tile-btn");
        String strName = type.toString().toLowerCase(Locale.ROOT);
        btn.setGraphic(texture("map/" + strName + (isBig ? "4" : "") + ".png"));
        btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btn.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                btn.fire();
            }
        });
        btn.setOnAction(event -> {
            this.isBig = isBig;
            this.gameType = type;
            previewImgView.setImage(image("map/" + this.gameType.toString().toLowerCase(Locale.ROOT) + (isBig ? "4" : "") + ".png"));
            previewImgView.setLayoutX(layoutX);
            previewImgView.setLayoutY(layoutY);
        });
        return btn;
    }

    private void drawMap() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        //draw grid 绘制直线网格
        if (showGrid) {
            drawGrid(g);
        }
        //draw map tiles 绘制地图元素
        drawTiles(g);
        //draw flag 绘制旗帜
        g.drawImage(image("map/flag.png"), 12 * CELL_SIZE, 24 * CELL_SIZE);
    }

    private void drawGrid(GraphicsContext g) {
        for (int i = 0; i < map.length; i++) {
            setStroke(g, i);
            g.strokeLine(i * CELL_SIZE, 0, i * CELL_SIZE, 624);
            for (int j = 0; j < map[i].length; j++) {
                setStroke(g, j);
                g.strokeLine(0, j * CELL_SIZE, 624, j * CELL_SIZE);
            }
        }
        g.strokeLine(26 * CELL_SIZE, 0, 26 * CELL_SIZE, 624);
        g.strokeLine(0, 26 * CELL_SIZE, 624, 26 * CELL_SIZE);
    }

    private void setStroke(GraphicsContext g, int index) {
        if (index == 5 || index == 10 || index == 16 || index == 21) {
            g.setLineWidth(0.8);
            g.setStroke(Color.LIGHTBLUE);
        } else {
            g.setLineWidth(0.5);
            g.setStroke(Color.GRAY);
        }
    }

    private void drawTiles(GraphicsContext g) {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[j][i] != GameType.EMPTY) {
                    if (map[j][i] == GameType.BRICK) {
                        g.drawImage(image("map/brick.png"), CELL_SIZE * j, CELL_SIZE * i);
                    } else if (map[j][i] == GameType.SEA) {
                        g.drawImage(image("map/sea.png"), CELL_SIZE * j, CELL_SIZE * i);
                    } else if (map[j][i] == GameType.SNOW) {
                        g.drawImage(image("map/snow.png"), CELL_SIZE * j, CELL_SIZE * i);
                    } else if (map[j][i] == GameType.STONE) {
                        g.drawImage(image("map/stone.png"), CELL_SIZE * j, CELL_SIZE * i);
                    } else if (map[j][i] == GameType.GREENS) {
                        g.drawImage(image("map/greens.png"), CELL_SIZE * j, CELL_SIZE * i);
                    }
                }
            }
        }
    }

}
