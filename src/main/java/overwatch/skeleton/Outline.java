package overwatch.skeleton;

import java.util.Arrays;

/**
 * Basis um die Position und Größe eines Objekts zu erfassen.
 */
public interface Outline extends Position, Size {

    /**
     * Die letzte Position des Objekts auf der x-Achse.
     * @return Die letzte Position des Objekts auf der x-Achse.
     */
    default int endX(){
        return x() + width() -1;
    }
    /**
     * Die letzte Position des Objekts auf der y-Achse.
     * @return Die letzte Position des Objekts auf der y-Achse.
     */
    default int endY(){
        return y() + height() - 1;
    }


    record Rectangle(int x, int y, int width, int height, int area, int endX, int endY ) implements Outline {}

    /**
     * Erstellt eine neue Outline.
     * @param x Die Position auf der x-Achse.
     * @param y Die Position auf der y-Achse.
     * @param width Die Breite des Objekts.
     * @param height Die Höhe des Objekts.
     * @return Gibt die erstellte Outline zurück.
     */
    static Outline of(int x, int y, int width, int height) {
        return new Rectangle(x, y, width, height, width * height, x + width - 1, y+ height - 1);
    }

    /**
     * Findet zu einer Position eine passende Outline.
     * @param x Die Position auf der x-Achse.
     * @param y Die Position auf der y-Achse.
     * @param outlines Die zu durchsuchenden Outlines.
     * @return Gibt eine passende Outline zurück.
     * @param <E> Generischer typ.
     * @throws IllegalArgumentException Sollte keine passende Outline gefunden werden.
     */
    static <E extends Outline> E findOutlineForPosition(final int x, final int y, final E[] outlines ){
        for(E outline : outlines)
            if (outline.x() <= x && outline.endX() >= x && outline.y() <= y && outline.endY() >= y)
                return outline;
        throw new IllegalArgumentException("There is no matching outline for coordinates x: '" + x + "' y: '" + y + "'");
    }

    /**
     * Berechnet die überschneidende Fläche zwischen zwei Outlines.
     * @param a Die erste Fläche.
     * @param b Die zweite Fläche.
     * @return Der Größe der überschneidenden Fläche in Pixeln. Ergibt Bullshit für nicht überlappende Flächen. Vorher mit {@link #isIntersecting(Outline, Outline)} prüfen.
     */
    static int intersectionArea(Outline a, Outline b){
        int x = Math.max(a.x(), b.x());
        int y = Math.max(a.y(), b.y());
        int endX = Math.min(a.endX(), b.endX());
        int endY = Math.min(a.endY(), b.endY());
        int width = endX - x;
        int height = endY - y;
        return width * height;
    }

    /**
     * Prüft, ob zwei Flächen sich überlappen.
     * @param a Die erste Fläche.
     * @param b Die zweite Fläche.
     * @return {@code true} sollten sie sich überlappen, ansonsten {@code false}.
     */
    static boolean isIntersecting(Outline a, Outline b) {
        if(a.x() <= b.x() &&
           a.endX() >= b.endX() &&
           a.y() <= b.y() &&
           a.endY() >= b.endY())
            return true;


        // Intersecting
        return a.endY() >= b.y() &&
                a.y() <= b.endY() &&
                a.x() <= b.endX() &&
                a.endX() >= b.x();
    }

    /**
     * Führt mehrere Flächen zusammen.
     * @param outlines Die zusammenzuführenden Flächen.
     * @return Die neue Gesamtfläche.
     */
    static Outline compose(Outline[] outlines){
        int x = Arrays.stream(outlines)
                .mapToInt(Position::x).min().orElse(0);
        int y = Arrays.stream(outlines)
                .mapToInt(Position::y).min().orElse(0);
        int width = Arrays.stream(outlines)
                .mapToInt(Outline::endX).max().orElse(0) - x + 1;
        int height = Arrays.stream(outlines)
                .mapToInt(Outline::endY).max().orElse(0) - y + 1;
        return of(x,y, width, height);
    }

    /**
     * Führt zwei Flächen zusammen.
     * @param a Die erste Fläche.
     * @param b Die zweite Fläche.
     * @return Die neue Gesamtfläche.
     */
    static Outline compose(Outline a, Outline b){
        int x = Math.min(a.x(), b.x());
        int y = Math.min(a.y(), b.y());
        int width = Math.max(a.endX(), b.endX()) - x + 1;
        int height = Math.max(a.endY(), b.endY()) - y + 1;
        return of(x,y,width,height);
    }
}
