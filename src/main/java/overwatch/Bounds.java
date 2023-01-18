package overwatch;

public record Bounds(int x, int y, int width, int height, int lastX, int lastY) {

    public Bounds(int x, int y, int width, int height) {
        this(x,y,width,height, x+width, y+height);
    }
    public int area(){
        return width * height;
    }
}
