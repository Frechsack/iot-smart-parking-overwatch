package overwatch.skeleton;

/**
 * Basis für die Erfassung einer Größe in Breite und Höhe.
 */
public interface Size {
     /**
      * Gibt die Breite in Pixeln an.
      * @return Die Breite in Pixeln.
      */
     int width();

     /**
      * Gibt die Höhe in Pixeln an.
      * @return Die Höhe in Pixeln.
      */
     int height();

     /**
      * Gibt die Fläche in Pixeln an.
      * @return Die Fläche in Pixeln.
      */
     default int area(){
          return width() * height();
     }
}
