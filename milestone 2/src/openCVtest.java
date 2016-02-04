import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * Created by Naimen on 04-02-2016.
 */
public class openCVtest {
    public static void main(String[] args) {
        System.loadLibrary("opencv_java310");
        Mat m  = Mat.eye(3, 3, CvType.CV_8UC1);
        System.out.println("m = " + m.dump());
    }
}
