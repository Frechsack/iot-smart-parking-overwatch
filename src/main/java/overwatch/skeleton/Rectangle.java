package overwatch.skeleton;

public record Rectangle(int x, int y, int width, int height, int area, int endX, int endY ) implements Outline {

    public Rectangle(int x, int y, int width, int height){
        this(x, y, width, height, width * height, x + width - 1, y+ height - 1);
    }
}
