/**
 * This is a Tile Rendering Engine which will generate a random game world.
 * This world has an option to turn on line of sight feature to simulate night.
 * You can play the game with input string or with keyboard to visualize interactively.
 * Creating a new game needs you tu type "n" first, then enter seed you want, type "s" to finish.
 * You can load back previous saved game.
 * You can also change character appearance.
 * During game you can type ":q" to save and quit.
 * When hover above a Tile it will show information of that Tile.
 * You can play using keyboard with keys "wasd" to move.
 * Your mission is to find the key.
 * When key found, go to find the door and press "k" to open and enter the door to win.
 * @author Junliang Lu
 */

package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.algs4.WeightedQuickUnionUF;
import edu.princeton.cs.introcs.StdDraw;
import edu.princeton.cs.introcs.Stopwatch;

import java.awt.Color;
import java.awt.Font;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class Engine {
    public static final TERenderer TER = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    private static final String FILEPATH =
            "saveFile.txt";
    private static final String DOGE =
            "C:/Users/dexte/Desktop/Java/cs61b/fa20-p235/proj3/byow/Core/doge.png";
    private static final String SMILEFACE =
            "C:/Users/dexte/Desktop/Java/cs61b/fa20-p235/proj3/byow/Core/smileFace.png";
    private static final int LINEOFSIGHTRADIUS = 5;
    private static final boolean LINEOFSIGHTON = true;
    private int avatarXPos;
    private int avatarYPos;
    private int goldenDoorXPos;
    private int goldenDoorYPos;
    private int keyXPos;
    private int keyYPos;
    private long seed;
    private Random random;
    private boolean hasDoorKey;
    private boolean gameOver;
    private boolean isAvatar2;
    private TETile[][] tiles;

    public Engine() {
        seed = 0;
        avatarXPos = -1;
        avatarYPos = -2;
        goldenDoorXPos = -3;
        goldenDoorYPos = -4;
        keyXPos = -5;
        keyYPos = -6;
        random = new Random(0);
        hasDoorKey = false;
        gameOver = false;
        isAvatar2 = false;
        tiles = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
    }

    private void save() {
        try {
            FileOutputStream fileOut = new FileOutputStream(FILEPATH);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeInt(avatarXPos);
            objectOut.writeInt(avatarYPos);
            objectOut.writeInt(goldenDoorXPos);
            objectOut.writeInt(goldenDoorYPos);
            objectOut.writeInt(keyXPos);
            objectOut.writeInt(keyYPos);
            objectOut.writeLong(seed);
            objectOut.writeObject(random);
            objectOut.writeBoolean(hasDoorKey);
            objectOut.writeBoolean(gameOver);
            objectOut.writeBoolean(isAvatar2);
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    objectOut.writeChar(tiles[x][y].character());
                }
            }
            objectOut.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void load() {
        try {
            FileInputStream fileIn = new FileInputStream(FILEPATH);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            avatarXPos = objectIn.readInt();
            avatarYPos = objectIn.readInt();
            goldenDoorXPos = objectIn.readInt();
            goldenDoorYPos = objectIn.readInt();
            keyXPos = objectIn.readInt();
            keyYPos = objectIn.readInt();
            seed = objectIn.readLong();
            random = (Random) objectIn.readObject();
            hasDoorKey = objectIn.readBoolean();
            gameOver = objectIn.readBoolean();
            isAvatar2 = objectIn.readBoolean();
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    char c = objectIn.readChar();
                    if (c == '@') {
                        tiles[x][y] = Tileset.AVATAR;
                    } else if (c == '&') {
                        tiles[x][y] = Tileset.AVATAR2;
                    } else if (c == '#') {
                        tiles[x][y] = Tileset.WALL;
                    } else if (c == '·') {
                        tiles[x][y] = Tileset.FLOOR;
                    } else if (c == ' ') {
                        tiles[x][y] = Tileset.NOTHING;
                    } else if (c == '"') {
                        tiles[x][y] = Tileset.GRASS;
                    } else if (c == '≈') {
                        tiles[x][y] = Tileset.WATER;
                    } else if (c == '❀') {
                        tiles[x][y] = Tileset.FLOWER;
                    } else if (c == '█') {
                        tiles[x][y] = Tileset.LOCKED_DOOR;
                    } else if (c == '▢') {
                        tiles[x][y] = Tileset.UNLOCKED_DOOR;
                    } else if (c == '▒') {
                        tiles[x][y] = Tileset.SAND;
                    } else if (c == '▲') {
                        tiles[x][y] = Tileset.MOUNTAIN;
                    } else if (c == '♠') {
                        tiles[x][y] = Tileset.TREE;
                    } else if (c == 'L') {
                        tiles[x][y] = Tileset.KEY;
                    }
                }
            }
            objectIn.close();
        } catch (IOException | ClassNotFoundException ex) {
            ex.getMessage();
        }
    }

    private void generateRandomRoom(ArrayList<Space> rooms,
                                           WeightedQuickUnionUF floors,
                                           boolean[][] roomFloors) {
        Space randomRoom = null;
        while (true) {
            int xPos = random.nextInt(WIDTH);
            int yPos = random.nextInt(HEIGHT);
            int w = random.nextInt(WIDTH) + 1;
            int h = random.nextInt(HEIGHT) + 1;
            randomRoom = new Space(xPos, yPos, w, h);
            if (spaceIsInFrame(randomRoom)
                    && randomRoom.isRoom()
                    && checkOverlapAndRoomsGap(randomRoom, roomFloors)) {
                break;
            }
        }
        int room1DPos = xyTo1D(randomRoom.getxPos(), randomRoom.getyPos());
        for (int xRel = 0; xRel < randomRoom.getW(); xRel++) {
            int x = randomRoom.getxPos() + xRel;
            for (int yRel = 0; yRel < randomRoom.getH(); yRel++) {
                int y = randomRoom.getyPos() + yRel;
                tiles[x][y] = Tileset.FLOOR;
                roomFloors[x][y] = true;
                int currFloor1DPos = xyTo1D(x, y);
                floors.union(room1DPos, currFloor1DPos);
            }
        }
        rooms.add(randomRoom);
    }

    private boolean generateHallWay(ArrayList<Space> hallWays,
                                           WeightedQuickUnionUF floors,
                                           boolean[][] roomFloors) {
        Space randomHallWay = null;
        while (true) {
            int xPos = random.nextInt(WIDTH);
            int yPos = random.nextInt(HEIGHT);
            int w = random.nextInt(WIDTH) + 1;
            int h = random.nextInt(HEIGHT) + 1;
            randomHallWay = new Space(xPos, yPos, w, h);
            if (spaceIsInFrame(randomHallWay)
                    && randomHallWay.isHallWay()
                    && checkOverlapConnect(
                            randomHallWay, floors)) {
                break;
            }
        }
        int hallWay1DPos = xyTo1D(randomHallWay.getxPos(), randomHallWay.getyPos());
        for (int xRel = 0; xRel < randomHallWay.getW(); xRel++) {
            int x = randomHallWay.getxPos() + xRel;
            for (int yRel = 0; yRel < randomHallWay.getH(); yRel++) {
                int y = randomHallWay.getyPos() + yRel;
                tiles[x][y] = Tileset.FLOOR;
                int currFloor1DPos = xyTo1D(x, y);
                floors.union(hallWay1DPos, currFloor1DPos);
            }
        }
        hallWays.add(randomHallWay);
        return true;
    }

    private boolean spaceIsInFrame(Space space) {
        return space.getxPos() + space.getW() - 1 < WIDTH
                && space.getyPos() + space.getH() - 1 < HEIGHT;
    }

    private boolean xyPosIsInFrame(int xPos, int yPos) {
        return 0 <= xPos && xPos < WIDTH && 0 <= yPos && yPos < HEIGHT;
    }

    private boolean checkOverlapAndRoomsGap(Space space,
                                                   boolean[][] roomFloors) {
        for (int xRel = -2; xRel < space.getW() + 2; xRel++) {
            int x = space.getxPos() + xRel;
            for (int yRel = -2; yRel < space.getH() + 2; yRel++) {
                int y = space.getyPos() + yRel;
                if (xyPosIsInFrame(x, y) && roomFloors[x][y]) {
                    return false;
                }
            }
        }
        for (int xRel = -1; xRel < space.getW() + 1; xRel++) {
            int x = space.getxPos() + xRel;
            for (int yRel = -1; yRel < space.getH() + 1; yRel++) {
                int y = space.getyPos() + yRel;
                if (!xyPosIsInFrame(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean methodTooLongHelper(Space space,
                                               WeightedQuickUnionUF floors) {
        int hallWay1DPos = xyTo1D(space.getxPos(), space.getyPos());
        if (space.isHorizontalHallWay()) {
            for (int xRel = 0; xRel < space.getW(); xRel++) {
                int x = space.getxPos() + xRel;
                int yUpByOne = space.getyPos() + 1;
                int yDownByOne = space.getyPos() - 1;
                if (xyPosIsInFrame(x, yUpByOne)
                        && tiles[x][yUpByOne].description().equals("floor")) {
                    return false;
                }
                if (xyPosIsInFrame(x, yDownByOne)
                        && tiles[x][yDownByOne].description().equals("floor")) {
                    return false;
                }
            }
            int xHead = space.getxPos();
            int xTail = space.getxPos() + space.getW() - 1;
            int y = space.getyPos();
            boolean headTouch = xyPosIsInFrame(xHead - 1, y)
                    && tiles[xHead - 1][y].description().equals("floor");
            boolean tailTouch = xyPosIsInFrame(xTail + 1, y)
                    && tiles[xTail + 1][y].description().equals("floor");
            if (headTouch) {
                floors.union(hallWay1DPos, xyTo1D(xHead - 1, y));
            }
            if (tailTouch) {
                floors.union(hallWay1DPos, xyTo1D(xTail + 1, y));
            }
            return headTouch || tailTouch;
        } else if (space.isVerticalHallWay()) {
            for (int yRel = 0; yRel < space.getH(); yRel++) {
                int y = space.getyPos() + yRel;
                int xRightByOne = space.getxPos() + 1;
                int xLeftByOne = space.getxPos() - 1;
                if (xyPosIsInFrame(xRightByOne, y)
                        && tiles[xRightByOne][y].description().equals("floor")) {
                    return false;
                }
                if (xyPosIsInFrame(xLeftByOne, y)
                        && tiles[xLeftByOne][y].description().equals("floor")) {
                    return false;
                }
            }
            int yHead = space.getyPos();
            int yTail = space.getyPos() + space.getH() - 1;
            int x = space.getxPos();
            boolean headTouch = xyPosIsInFrame(x, yHead - 1)
                    && tiles[x][yHead - 1].description().equals("floor");
            boolean tailTouch = xyPosIsInFrame(x, yTail + 1)
                    && tiles[x][yTail + 1].description().equals("floor");
            if (headTouch) {
                floors.union(hallWay1DPos, xyTo1D(x, yHead - 1));
            }
            if (tailTouch) {
                floors.union(hallWay1DPos, xyTo1D(x, yTail + 1));
            }
            return headTouch || tailTouch;
        } else {
            int x = space.getxPos();
            int y = space.getyPos();
            boolean touchUp = tiles[x][y + 1].description().equals("floor");
            boolean touchDown = tiles[x][y - 1].description().equals("floor");
            boolean touchRight = tiles[x + 1][y].description().equals("floor");
            boolean touchLeft = tiles[x - 1][y].description().equals("floor");
            if (touchUp) {
                floors.union(hallWay1DPos, xyTo1D(x, y + 1));
            }
            if (touchDown) {
                floors.union(hallWay1DPos, xyTo1D(x, y - 1));
            }
            if (touchRight) {
                floors.union(hallWay1DPos, xyTo1D(x + 1, y));
            }
            if (touchLeft) {
                floors.union(hallWay1DPos, xyTo1D(x - 1, y));
            }
            return touchUp || touchDown || touchRight || touchLeft;
        }
    }

    private boolean checkOverlapConnect(Space space,
                                               WeightedQuickUnionUF floors) {
        for (int xRel = 0; xRel < space.getW(); xRel++) {
            int x = space.getxPos() + xRel;
            for (int yRel = 0; yRel < space.getH(); yRel++) {
                int y = space.getyPos() + yRel;
                if (tiles[x][y].description().equals("floor")) {
                    return false;
                }
            }
        }
        for (int xRel = -1; xRel < space.getW() + 1; xRel++) {
            int x = space.getxPos() + xRel;
            for (int yRel = -1; yRel < space.getH() + 1; yRel++) {
                int y = space.getyPos() + yRel;
                if (!xyPosIsInFrame(x, y)) {
                    return false;
                }
            }
        }
        return methodTooLongHelper(space, floors);
    }

    private int xyTo1D(int x, int y) {
        return x + y * WIDTH;
    }

    private boolean generateWorld(boolean[][] roomFloors) {
        WeightedQuickUnionUF floors = new WeightedQuickUnionUF(WIDTH * HEIGHT);
        ArrayList<Space> rooms = new ArrayList<>();
        int numOfRoomsBound = 25;
        int numOfRooms = random.nextInt(numOfRoomsBound) + 1;
        for (int i = 0; i < numOfRooms; i++) {
            generateRandomRoom(rooms, floors, roomFloors);
        }
        ArrayList<Space> hallWays = new ArrayList<>();

        while (true) {
            if (!generateHallWay(hallWays, floors, roomFloors)) {
                return false;
            }
            Space room0 = rooms.get(0);
            int room01DPos = xyTo1D(room0.getxPos(), room0.getyPos());
            boolean everyRoomConnected = true;
            for (Space room : rooms) {
                int currRoom1DPos = xyTo1D(room.getxPos(), room.getyPos());
                if (!floors.connected(room01DPos, currRoom1DPos)) {
                    everyRoomConnected = false;
                    break;
                }
            }
            if (everyRoomConnected) {
                break;
            }
        }
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                if (tiles[x][y].description().equals("floor")) {
                    for (int r = y - 1; r <= y + 1; r++) {
                        for (int c = x - 1; c <= x + 1; c++) {
                            if (xyPosIsInFrame(c, r)
                                    && tiles[c][r].description().equals("nothing")) {
                                tiles[c][r] = Tileset.WALL;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean generateGoldenDoor() {
        for (int y = HEIGHT - 1; y >= 0; y--) {
            for (int x = WIDTH - 1; x >= 0; x--) {
                if (tiles[x][y].description().equals("wall")) {
                    int yUpByOne = y + 1;
                    int yDownByOne = y - 1;
                    int xRightByOne = x + 1;
                    int xLeftByOne = x - 1;
                    if ((xyPosIsInFrame(x, yUpByOne)
                            && tiles[x][yUpByOne].description().equals("wall")
                            && xyPosIsInFrame(x, yDownByOne)
                            && tiles[x][yDownByOne].description().equals("wall")
                            && (xyPosIsInFrame(xRightByOne, y)
                            && tiles[xRightByOne][y].description().equals("floor")
                            && (!xyPosIsInFrame(xLeftByOne, y)
                            || xyPosIsInFrame(xLeftByOne, y)
                            && tiles[xLeftByOne][y].description().equals("nothing")))
                            || (xyPosIsInFrame(xLeftByOne, y)
                            && tiles[xLeftByOne][y].description().equals("floor")
                            && (!xyPosIsInFrame(xRightByOne, y)
                            || xyPosIsInFrame(xRightByOne, y)
                            && tiles[xRightByOne][y].description().equals("nothing"))))
                            || (xyPosIsInFrame(xRightByOne, y)
                            && tiles[xRightByOne][y].description().equals("wall")
                            && xyPosIsInFrame(xLeftByOne, y)
                            && tiles[xLeftByOne][y].description().equals("wall")
                            && (xyPosIsInFrame(x, yUpByOne)
                            && tiles[x][yUpByOne].description().equals("floor")
                            && (!xyPosIsInFrame(x, yDownByOne)
                            || xyPosIsInFrame(x, yDownByOne)
                            && tiles[x][yDownByOne].description().equals("nothing")))
                            || (xyPosIsInFrame(x, yDownByOne)
                            && tiles[x][yDownByOne].description().equals("floor")
                            && (!xyPosIsInFrame(x, yUpByOne)
                            || xyPosIsInFrame(x, yUpByOne)
                            && tiles[x][yUpByOne].description().equals("nothing"))))) {
                        tiles[x][y] = Tileset.LOCKED_DOOR;
                        goldenDoorXPos = x;
                        goldenDoorYPos = y;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean generateAvatar() {
        int randomNum = random.nextInt(4);
        if (randomNum == 0) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    if (tiles[x][y].description().equals("floor")) {
                        if (!isAvatar2) {
                            tiles[x][y] = Tileset.AVATAR;
                        } else {
                            tiles[x][y] = Tileset.AVATAR2;
                        }
                        avatarXPos = x;
                        avatarYPos = y;
                        return true;
                    }
                }
            }
        } else if (randomNum == 1) {
            for (int y = HEIGHT - 1; y >= 0; y--) {
                for (int x = 0; x < WIDTH; x++) {
                    if (tiles[x][y].description().equals("floor")) {
                        if (!isAvatar2) {
                            tiles[x][y] = Tileset.AVATAR;
                        } else {
                            tiles[x][y] = Tileset.AVATAR2;
                        }
                        avatarXPos = x;
                        avatarYPos = y;
                        return true;
                    }
                }
            }
        } else if (randomNum == 2) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = WIDTH - 1; x >= 0; x--) {
                    if (tiles[x][y].description().equals("floor")) {
                        if (!isAvatar2) {
                            tiles[x][y] = Tileset.AVATAR;
                        } else {
                            tiles[x][y] = Tileset.AVATAR2;
                        }
                        avatarXPos = x;
                        avatarYPos = y;
                        return true;
                    }
                }
            }
        } else {
            for (int y = HEIGHT - 1; y >= 0; y--) {
                for (int x = WIDTH - 1; x >= 0; x--) {
                    if (tiles[x][y].description().equals("floor")) {
                        if (!isAvatar2) {
                            tiles[x][y] = Tileset.AVATAR;
                        } else {
                            tiles[x][y] = Tileset.AVATAR2;
                        }
                        avatarXPos = x;
                        avatarYPos = y;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean generateKey() {
        int randomNum = random.nextInt(4);
        if (randomNum == 0) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    if (tiles[x][y].description().equals("floor")) {
                        tiles[x][y] = Tileset.KEY;
                        keyXPos = x;
                        keyYPos = y;
                        return true;
                    }
                }
            }
        } else if (randomNum == 1) {
            for (int y = HEIGHT - 1; y >= 0; y--) {
                for (int x = 0; x < WIDTH; x++) {
                    if (tiles[x][y].description().equals("floor")) {
                        tiles[x][y] = Tileset.KEY;
                        keyXPos = x;
                        keyYPos = y;
                        return true;
                    }
                }
            }
        } else if (randomNum == 2) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = WIDTH - 1; x >= 0; x--) {
                    if (tiles[x][y].description().equals("floor")) {
                        tiles[x][y] = Tileset.KEY;
                        keyXPos = x;
                        keyYPos = y;
                        return true;
                    }
                }
            }
        } else {
            for (int y = HEIGHT - 1; y >= 0; y--) {
                for (int x = WIDTH - 1; x >= 0; x--) {
                    if (tiles[x][y].description().equals("floor")) {
                        tiles[x][y] = Tileset.KEY;
                        keyXPos = x;
                        keyYPos = y;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        TER.initialize(WIDTH, HEIGHT + 3);
        drawMainMenu();
        boolean extractingSeed = false;
        boolean inGame = false;
        boolean prepareToQuitSave = false;
        boolean inAvatarSelection = false;
        String seedEnteredSoFar = "";
        int currMouseTileXPos = 0;
        int currMouseTileYPos = 0;
        int prevMouseTileXPos = 0;
        int prevMouseTileYPos = 0;
        while (true) {
            if (gameOver) {
                drawWinMessage();
                break;
            }
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                if (inAvatarSelection && c == '1') {
                    isAvatar2 = false;
                    inAvatarSelection = false;
                    drawMainMenu();
                } else if (inAvatarSelection && c == '2') {
                    isAvatar2 = true;
                    inAvatarSelection = false;
                    drawMainMenu();
                } else if (c == 'n' || c == 'N') {
                    extractingSeed = true;
                    drawSeedEnteredSoFar(seedEnteredSoFar);
                } else if (c == 'l' || c == 'L') {
                    load();
                    inGame = true;
                } else if (c == 'c' || c == 'C') {
                    inAvatarSelection = true;
                    drawAvatarSelection();
                } else if (prepareToQuitSave && (c == 'q' || c == 'Q')) {
                    save();
                    break;
                } else if (c == ':') {
                    prepareToQuitSave = true;
                } else if (extractingSeed) {
                    if (c == 's' || c == 'S') {
                        extractingSeed = false;
                        createWorldFromKeyBoard(seedEnteredSoFar);
                        inGame = true;
                    } else {
                        seedEnteredSoFar += c;
                        drawSeedEnteredSoFar(seedEnteredSoFar);
                    }
                } else {
                    processAction(c, true);
                }
            }
            if (inGame) {
                currMouseTileXPos = (int) StdDraw.mouseX();
                currMouseTileYPos = (int) StdDraw.mouseY();
                if (currMouseTileXPos != prevMouseTileXPos
                        || currMouseTileYPos != prevMouseTileYPos) {
                    prevMouseTileXPos = currMouseTileXPos;
                    prevMouseTileYPos = currMouseTileYPos;
                    if (!xyPosIsInFrame(currMouseTileXPos, currMouseTileYPos)) {
                        drawWorld();
                    } else {
                        drawWorld();
                        drawHUD(currMouseTileXPos, currMouseTileYPos);
                    }
                }
            }
        }
        System.exit(0);
    }

    private void processAction(char c, boolean keyBoardMode) {
        if (c == 'k' || c == 'K') {
            openDoor(keyBoardMode);
        } else {
            move(c, keyBoardMode);
        }
        if (keyBoardMode) {
            drawWorld();
        }
    }

    private void createWorldFromKeyBoard(String seedEnteredSoFar) {
        seed = Long.parseLong(seedEnteredSoFar);
        launch();
        drawWorld();
    }

    private void createWorldFromInputString(String seedEnteredSoFar) {
        seed = Long.parseLong(seedEnteredSoFar);
        launch();
    }

    private void drawWorld() {
        StdDraw.clear(Color.BLACK);
        TER.renderFrame(filterWorld());
    }

    private TETile[][] filterWorld() {
        TETile[][] tilesInUse = new TETile[WIDTH][HEIGHT];
        if (LINEOFSIGHTON) {
            for (int x = 0; x < WIDTH; x += 1) {
                for (int y = 0; y < HEIGHT; y += 1) {
                    int xMax = avatarXPos + LINEOFSIGHTRADIUS;
                    int xMin = avatarXPos - LINEOFSIGHTRADIUS;
                    int yMax = avatarYPos + LINEOFSIGHTRADIUS;
                    int yMin = avatarYPos - LINEOFSIGHTRADIUS;
                    if (xMin <= x && x <= xMax && yMin <= y && y <= yMax) {
                        tilesInUse[x][y] = tiles[x][y];
                    } else {
                        tilesInUse[x][y] = Tileset.NOTHING;
                    }
                }
            }
        } else {
            tilesInUse = tiles;
        }
        return tilesInUse;
    }

    private void drawHUD(int currMouseTileXPos, int currMouseTileYPos) {
        TETile[][] tilesInUse = filterWorld();
        if (!tilesInUse[currMouseTileXPos][currMouseTileYPos]
                .description().equals("nothing")) {
            Font font3 = new Font("Monaco", Font.BOLD, 16);
            StdDraw.setFont(font3);
            StdDraw.setPenColor(Color.WHITE);
            String target =
                    tilesInUse[currMouseTileXPos][currMouseTileYPos]
                            .description();
            StdDraw.text(5, HEIGHT + 2, target);
            StdDraw.show();
        }
    }

    private void drawHint1() {
        Font font3 = new Font("Monaco", Font.BOLD, 16);
        StdDraw.setFont(font3);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(WIDTH / 2, HEIGHT + 2, "press K to open door using the key");
        StdDraw.show();
        Stopwatch sw = new Stopwatch();
        while (true) {
            if (sw.elapsedTime() > 3) {
                break;
            }
        }
    }

    private void drawHint2() {
        Font font3 = new Font("Monaco", Font.BOLD, 16);
        StdDraw.setFont(font3);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(WIDTH / 2, HEIGHT + 2, "You have to have a key");
        StdDraw.show();
        Stopwatch sw = new Stopwatch();
        while (true) {
            if (sw.elapsedTime() > 3) {
                break;
            }
        }
    }

    private void drawWinMessage() {
        Font font3 = new Font("Monaco", Font.BOLD, 16);
        StdDraw.setFont(font3);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(WIDTH / 2, HEIGHT + 2, "you successfully escaped!");
        StdDraw.show();
        Stopwatch sw = new Stopwatch();
        while (true) {
            if (sw.elapsedTime() > 10) {
                break;
            }
        }
    }

    private void drawMainMenu() {
        StdDraw.clear(Color.BLACK);
        Font font = new Font("Monaco", Font.BOLD, 50);
        StdDraw.setFont(font);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(WIDTH * 1 / 2, HEIGHT * 8 / 10, "CS61B: THE GAME");
        Font font2 = new Font("Monaco", Font.BOLD, 25);
        StdDraw.setFont(font2);
        StdDraw.text(WIDTH * 1 / 2, HEIGHT * 6 / 10, "New Game (N)");
        StdDraw.text(WIDTH * 1 / 2, HEIGHT * 5 / 10, "Load Game (L)");
        StdDraw.text(WIDTH * 1 / 2, HEIGHT * 4 / 10, "Change Appearance (C)");
        StdDraw.text(WIDTH * 1 / 2, HEIGHT * 3 / 10, "Quit (Q)");
        StdDraw.enableDoubleBuffering();
        StdDraw.show();
    }

    private void drawSeedEnteredSoFar(String seedEnteredSoFar) {
        StdDraw.clear(Color.BLACK);
        Font font2 = new Font("Monaco", Font.BOLD, 25);
        StdDraw.setFont(font2);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(WIDTH * 1 / 2, HEIGHT * 8 / 10, "Seed Entered So Far:");
        StdDraw.text(WIDTH * 1 / 2, HEIGHT * 7 / 10, seedEnteredSoFar);
        StdDraw.show();
    }

    private void drawAvatarSelection() {
        StdDraw.clear(Color.BLACK);
        Font font = new Font("Monaco", Font.BOLD, 50);
        StdDraw.setFont(font);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(WIDTH / 2, HEIGHT * 9 / 10, "Select Your Avatar");
        StdDraw.picture(WIDTH * 4 / 10, HEIGHT * 4 / 10, DOGE, 25, 25);
        StdDraw.picture(WIDTH * 6 / 10, HEIGHT * 5.5 / 10, SMILEFACE, 10, 10);
        StdDraw.text(WIDTH * 4 / 10, HEIGHT * 2 / 10, "1");
        StdDraw.text(WIDTH * 6 / 10, HEIGHT * 2 / 10, "2");
        StdDraw.show();
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quit save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // Fill out this method so that it run the engine using the input
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.
        String seedEnteredSoFar = "";
        boolean extractingSeed = false;
        boolean inAvatarSelection = false;
        char[] chars = input.toCharArray();
        boolean prepareToQuitSave = false;
        for (int i = 0; i < chars.length; i++) {
            if (inAvatarSelection && chars[i] == '1') {
                isAvatar2 = false;
                inAvatarSelection = false;
            } else if (inAvatarSelection && chars[i] == '2') {
                isAvatar2 = true;
                inAvatarSelection = false;
            } else if (chars[i] == 'n' || chars[i] == 'N') {
                extractingSeed = true;
            } else if (chars[i] == 'l' || chars[i] == 'L') {
                load();
            } else if (chars[i] == 'c' || chars[i] == 'C') {
                inAvatarSelection = true;
            } else if (prepareToQuitSave && (chars[i] == 'q' || chars[i] == 'Q')) {
                save();
                break;
            } else if (chars[i] == ':') {
                prepareToQuitSave = true;
            } else if (extractingSeed) {
                if (chars[i] == 's' || chars[i] == 'S') {
                    extractingSeed = false;
                    createWorldFromInputString(seedEnteredSoFar);
                } else {
                    seedEnteredSoFar += chars[i];
                }
            } else {
                processAction(chars[i], false);
            }
        }
        TETile[][] finalReturnTiles = filterWorld();
        //TER.initialize(WIDTH, HEIGHT + 3);
        //TER.renderFrame(finalReturnTiles);
        return finalReturnTiles;
    }

    private void moveW(TETile you, boolean keyBoardMode) {
        if (xyPosIsInFrame(avatarXPos, avatarYPos + 1)
                && !tiles[avatarXPos][avatarYPos + 1].description().equals("wall")
                && !tiles[avatarXPos][avatarYPos + 1]
                .description().equals("locked door")) {
            String temp = tiles[avatarXPos][avatarYPos + 1].description();
            tiles[avatarXPos][avatarYPos + 1] = you;
            tiles[avatarXPos][avatarYPos] = Tileset.FLOOR;
            avatarYPos++;
            if (temp.equals("key")) {
                hasDoorKey = true;
                if (keyBoardMode) {
                    drawHint1();
                }
            }
            if (temp.equals("unlocked door")) {
                gameOver = true;
            }
        }
    }

    private void moveS(TETile you, boolean keyBoardMode) {
        if (xyPosIsInFrame(avatarXPos, avatarYPos - 1)
                && !tiles[avatarXPos][avatarYPos - 1].description().equals("wall")
                && !tiles[avatarXPos][avatarYPos - 1]
                .description().equals("locked door")) {
            String temp = tiles[avatarXPos][avatarYPos - 1].description();
            tiles[avatarXPos][avatarYPos - 1] = you;
            tiles[avatarXPos][avatarYPos] = Tileset.FLOOR;
            avatarYPos--;
            if (temp.equals("key")) {
                hasDoorKey = true;
                if (keyBoardMode) {
                    drawHint1();
                }
            }
            if (temp.equals("unlocked door")) {
                gameOver = true;
            }
        }
    }

    private void moveD(TETile you, boolean keyBoardMode) {
        if (xyPosIsInFrame(avatarXPos + 1, avatarYPos)
                && !tiles[avatarXPos + 1][avatarYPos].description().equals("wall")
                && !tiles[avatarXPos + 1][avatarYPos]
                .description().equals("locked door")) {
            String temp = tiles[avatarXPos + 1][avatarYPos].description();
            tiles[avatarXPos + 1][avatarYPos] = you;
            tiles[avatarXPos][avatarYPos] = Tileset.FLOOR;
            avatarXPos++;
            if (temp.equals("key")) {
                hasDoorKey = true;
                if (keyBoardMode) {
                    drawHint1();
                }
            }
            if (temp.equals("unlocked door")) {
                gameOver = true;
            }
        }
    }

    private void moveA(TETile you, boolean keyBoardMode) {
        if (xyPosIsInFrame(avatarXPos - 1, avatarYPos)
                && !tiles[avatarXPos - 1][avatarYPos].description().equals("wall")
                && !tiles[avatarXPos - 1][avatarYPos]
                .description().equals("locked door")) {
            String temp = tiles[avatarXPos - 1][avatarYPos].description();
            tiles[avatarXPos - 1][avatarYPos] = you;
            tiles[avatarXPos][avatarYPos] = Tileset.FLOOR;
            avatarXPos--;
            if (temp.equals("key")) {
                hasDoorKey = true;
                if (keyBoardMode) {
                    drawHint1();
                }
            }
            if (temp.equals("unlocked door")) {
                gameOver = true;
            }
        }
    }

    private void move(char action, boolean keyBoardMode) {
        TETile you = Tileset.AVATAR;
        if (isAvatar2) {
            you = Tileset.AVATAR2;
        }
        if (action == 'w' || action == 'W') {
            moveW(you, keyBoardMode);
        } else if (action == 's' || action == 'S') {
            moveS(you, keyBoardMode);
        } else if (action == 'd' || action == 'D') {
            moveD(you, keyBoardMode);
        } else if (action == 'a' || action == 'A') {
            moveA(you, keyBoardMode);
        }
    }

    private void openDoor(boolean keyBoardMode) {
        if (nextToDoor()) {
            if (hasDoorKey) {
                tiles[goldenDoorXPos][goldenDoorYPos] = Tileset.UNLOCKED_DOOR;
            } else {
                if (keyBoardMode) {
                    drawHint2();
                }
            }
        }
    }

    private boolean nextToDoor() {
        return tiles[avatarXPos][avatarYPos + 1].description().equals("locked door")
                || tiles[avatarXPos][avatarYPos - 1].description().equals("locked door")
                || tiles[avatarXPos + 1][avatarYPos].description().equals("locked door")
                || tiles[avatarXPos - 1][avatarYPos].description().equals("locked door");
    }

    private void launch() {
        random = new Random(seed);
        boolean[][] roomFloors = new boolean[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                tiles[x][y] = Tileset.NOTHING;
                roomFloors[x][y] = false;
            }
        }
        generateWorld(roomFloors);
        generateGoldenDoor();
        generateAvatar();
        generateKey();
    }
}
