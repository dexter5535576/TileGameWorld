package byow.Core;

/**
 * The Space class is used to generate the game world.
 * It generates a space with position (x,y), width, and height.
 * The space then is classified as room or hallway.
 * Hallway can also classified to be vertical or horizontal.
 * @author Junliang Lu
 */

public class Space {
    protected static final int MAX_ROOM_SIZE = 6;
    private int xPos;
    private int yPos;
    private int w;
    private int h;

    public Space(int xPos, int yPos, int w, int h) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.w = w;
        this.h = h;
    }

    public int getxPos() {
        return xPos;
    }

    public int getyPos() {
        return yPos;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    public boolean isRoom() {
        return w > 1 && h > 1 && w <= MAX_ROOM_SIZE && h <= MAX_ROOM_SIZE;
    }

    public boolean isHallWay() {
        return (w == 1 || h == 1);
    }

    public boolean isHorizontalHallWay() {
        return isHallWay() && w > 1;
    }

    public boolean isSizeOneHallway() {
        return w == 1 && h == 1;
    }

    public boolean isVerticalHallWay() {
        return isHallWay() && h > 1;
    }
}
