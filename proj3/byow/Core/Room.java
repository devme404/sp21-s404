package byow.Core;

public final class Room {
    final int id;
    final int width, height, left_bottom_x, left_bottom_y;

    Room(int id, int width, int height, int left_bottom_x, int left_bottom_y) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.left_bottom_x = left_bottom_x;
        this.left_bottom_y = left_bottom_y;
    }

    public int left() {
        return left_bottom_x;
    }

    public int bottom() {
        return left_bottom_y;
    }

    public int right() {
        return left_bottom_x + width - 1;
    }

    public int top() {
        return left_bottom_y + height - 1;
    }

    public boolean overlap(Room other) {
        return left_bottom_x <= other.right() && other.left_bottom_x <= right()
            && left_bottom_y <= other.top() && other.left_bottom_y <= top();
    }

    public int centerX() {
        return (left() + right()) / 2;
    }

    public int centerY() {
        return (bottom() + top()) / 2;
    }

    public Side sideToward(Room other) {
        int dx = other.centerX() - centerX();
        int dy = other.centerY() - centerY();

        if (Math.abs(dx) > Math.abs(dy)) {
            return dx > 0 ? Side.RIGHT : Side.LEFT;
        } else {
            return dy > 0 ? Side.TOP : Side.BOTTOM;
        }
    }
}
