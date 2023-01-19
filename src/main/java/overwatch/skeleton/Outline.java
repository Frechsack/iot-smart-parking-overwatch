package overwatch.skeleton;

import java.util.Arrays;

public interface Outline extends overwatch.skeleton.Position, overwatch.skeleton.Size {

    default int endX(){
        return x() + width() -1;
    }

    default int endY(){
        return y() + height() - 1;
    }

    static <E extends Outline> E findOutlineForPosition(final int x, final int y, final E[] outlines ){
        for(E outline : outlines)
            if (outline.x() <= x && outline.endX() >= x && outline.y() <= y && outline.endY() >= y)
                return outline;
        throw new IllegalArgumentException("There is no matching outline for coordinates x: '" + x + "' y: '" + y + "'");
    }

    static Outline compose(Outline[] outlines){
        int x = Arrays.stream(outlines).parallel()
                .mapToInt(Position::x).min().orElse(0);
        int y = Arrays.stream(outlines).parallel()
                .mapToInt(Position::y).min().orElse(0);
        int width = Arrays.stream(outlines).parallel()
                .mapToInt(Outline::endX).max().orElse(0) - x + 1;
        int height = Arrays.stream(outlines).parallel()
                .mapToInt(Outline::endY).max().orElse(0) - y + 1;
        return new Rectangle(x,y, width, height);
    }

    static Outline compose(Outline a, Outline b){
        int x = Math.min(a.x(), b.x());
        int y = Math.min(a.y(), b.y());
        int width = Math.max(a.endX(), b.endX()) - x + 1;
        int height = Math.max(a.endY(), b.endY()) - y + 1;
        return new Rectangle(x,y,width,height);
    }
}
