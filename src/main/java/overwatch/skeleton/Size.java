package overwatch.skeleton;

public interface Size {
     int width();

     int height();

     default int area(){
          return width() * height();
     }
}
