package overwatch;


import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_video.BackgroundSubtractor;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;

import static org.bytedeco.opencv.global.opencv_highgui.imshow;
import static org.bytedeco.opencv.global.opencv_highgui.waitKey;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_video.createBackgroundSubtractorMOG2;

public class OpenCvApplication {

    public static void main(String[] args) {
       // Mat mat = imread("/home/frechsack/_dev_video0.png");
       // imwrite("/home/frechsack/out.png", mat);

        BackgroundSubtractor backSub= createBackgroundSubtractorMOG2();
        VideoCapture capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            System.err.println("Unable to open: ");
            System.exit(0);
        }
        Mat frame = new Mat(), fgMask = new Mat();
        while (true) {
            capture.read(frame);
            if (frame.empty()) {
                break;
            }


            // update the background model
            backSub.apply(frame, fgMask);
            // get the frame number and write it on the current frame
            rectangle(frame, new Point(10, 2), new Point(100, 20), new Scalar(255, 255, 255,-1));

            MatVector contours = new MatVector();

            findContours(fgMask, contours, CV_RETR_EXTERNAL, CV_CHAIN_APPROX_NONE);

            for (int i = 0; i < contours.size(); i++){
                Rect contour = boundingRect(contours.get(i));
                rectangle(fgMask, contour, new Scalar(255,0,0,-1));

            }

            // show the current frame and the fg masks
            imshow("Frame", frame);
            imshow("FG Mask", fgMask);

            // get the input from the keyboard
            int keyboard = waitKey(30);
            if (keyboard == 'q' || keyboard == 27) {
                break;
            }
        }
        waitKey();
        backSub.close();
        capture.close();
        System.exit(0);
    }

}
