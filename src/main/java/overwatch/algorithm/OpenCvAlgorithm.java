package overwatch.algorithm;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_video.BackgroundSubtractor;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import overwatch.model.Capture;
import overwatch.model.Zone;
import overwatch.skeleton.Outline;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_video.createBackgroundSubtractorKNN;
import static org.bytedeco.opencv.global.opencv_video.createBackgroundSubtractorMOG2;
import static overwatch.skeleton.Outline.isIntersecting;

public final class OpenCvAlgorithm extends Algorithm {

    private final @NotNull Zone[] zones;

    private final @NotNull OpenCvRessource[] openCvResources;

    private final @NotNull BufferedImage image;

    private volatile @NotNull @UnmodifiableView Collection<Outline> objects = List.of();

    private volatile @NotNull @UnmodifiableView Collection<Zone> zonesWithObjects = List.of();

    public OpenCvAlgorithm(@NotNull Zone[] zones){
        this.zones = zones;
        final @NotNull Capture[] captures = Arrays.stream(zones).map(Zone::capture).distinct().toArray(Capture[]::new);
        final @NotNull Outline outerBounds = Outline.compose(captures);
        this.openCvResources = Arrays.stream(captures)
                .filter(it -> !it.isVirtual())
                .map(this::createRessource)
                .toArray(OpenCvRessource[]::new);
        this.image = new BufferedImage(outerBounds.width(), outerBounds.height(), BufferedImage.TYPE_INT_RGB);
    }

    private OpenCvRessource createRessource(Capture capture){
        return new OpenCvRessource(
                capture,
                new VideoCapture(Integer.parseInt(capture.deviceName().substring(capture.deviceName().lastIndexOf("o")+1))),
                new Mat(),
                new Mat(),
                createBackgroundSubtractorMOG2(100,16,true));
    }

    @Override
    public void close() {
        Arrays.stream(openCvResources).forEach(it -> {
            it.captureDevice.close();
            it.subtract.close();
            it.foregroundFrame.close();
            it.sourceFrame.close();
        });
        image.flush();
    }

    @Override
    public synchronized @NotNull @UnmodifiableView Collection<Zone> compute() {
        final List<Outline> outlines = Arrays.stream(openCvResources).parallel().flatMap(it -> {
            it.captureDevice.read(it.sourceFrame);
            it.subtract.apply(it.sourceFrame, it.foregroundFrame);

            final MatVector contours = new MatVector();

            findContours(it.foregroundFrame, contours, CV_RETR_EXTERNAL, CV_CHAIN_APPROX_NONE);

            final float scaleX = (float) it.sourceFrame.cols() / (float) it.capture.width();
            final float scaleY = (float) it.sourceFrame.rows() / (float) it.capture.height();
            final Collection<Outline> outlinesPerRessource = new ArrayList<>((int) contours.size());
            for (int i = 0; i < contours.size(); i++){
                Rect contour = boundingRect(contours.get(i));

                int x = (int) ((float) contour.x() / scaleX);
                int y = (int) ((float)contour.y() / scaleY);
                int width = (int) ((float) contour.width() / scaleX);
                int height = (int) ((float) contour.height() / scaleY);
                int area = width * height;

                if(area > SIGNIFICANT_AREA_TO_DETECT)
                    outlinesPerRessource.add(Outline.of(x, y, width, height));
            }
            return outlinesPerRessource.stream();
        }).collect(Collectors.toList());

        for (int i = 0; i < outlines.size(); i++){
            Outline a = outlines.get(i);
            for (int y = i; y < outlines.size(); y++) {
                Outline b = outlines.get(y);
                if(a == b)
                    continue;
                if(!isIntersecting(a, b, INTERSECTION_THRESHOLD))
                    continue;
                a = Outline.compose(a, b);
                outlines.remove(y--);
            }
            outlines.set(i, a);
        }
        this.objects = outlines;
        this.zonesWithObjects = findZonesWithObject(zones, outlines).toList();
        return zonesWithObjects;
    }

    @Override
    public synchronized BufferedImage computeImage() {
        final @NotNull @UnmodifiableView Collection<Outline> outlines = this.objects;
        final @NotNull @UnmodifiableView Collection<Zone> zonesWithObjects = this.zonesWithObjects;
        final BiPredicate<Integer, Integer> isPixelModified = (x, y) -> {
            OpenCvRessource ressource = findRessourceForPosition(x,y);

            final float scaleX = (float) ressource.sourceFrame.cols() / (float) ressource.capture.width();
            final float scaleY = (float) ressource.sourceFrame.rows() / (float) ressource.capture.height();
            return ressource.foregroundFrame.ptr((int)(y * scaleY),(int)(x * scaleX)).get() > 0;
        };
        renderImage(image, isPixelModified, zones, zonesWithObjects, outlines);
        return image;
    }

    private @NotNull OpenCvRessource findRessourceForPosition(final int x, final int y) {
        for (OpenCvRessource openCvResource : openCvResources) {
            final Capture capture = openCvResource.capture;
            if (capture.x() <= x && x <= capture.endX() && capture.y() <= y && y <= capture.endY())
                return openCvResource;
        }
        throw new IllegalStateException("Pixel is not in any capture. Please take a look at the calling code.");
    }

    private record OpenCvRessource(Capture capture, VideoCapture captureDevice, Mat sourceFrame, Mat foregroundFrame, BackgroundSubtractor subtract) {}
}
