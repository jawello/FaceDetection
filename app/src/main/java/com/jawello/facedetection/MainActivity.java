package com.jawello.facedetection;

        import android.Manifest;
        import android.app.Activity;
        import android.content.Context;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.os.Bundle;
        import android.support.v4.content.ContextCompat;
        import android.util.Log;
        import android.view.SurfaceView;
        import android.view.WindowManager;
        import android.widget.Toast;

        import org.opencv.android.CameraBridgeViewBase;
        import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
        import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
        import org.opencv.android.OpenCVLoader;
        import org.opencv.core.Mat;
        import org.opencv.android.BaseLoaderCallback;
        import  org.opencv.android.LoaderCallbackInterface;
        import org.opencv.core.MatOfRect;
        import org.opencv.core.Rect;
        import org.opencv.core.Scalar;
        import org.opencv.core.Size;
        import org.opencv.imgproc.Imgproc;
        import org.opencv.objdetect.CascadeClassifier;

        import java.io.File;

public class MainActivity extends Activity implements CvCameraViewListener2 {
    private CameraBridgeViewBase m_openCvCameraView;

    private CascadeClassifier m_cascadeClassifier;
    private int m_absoluteFaceSizeHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        m_openCvCameraView = (CameraBridgeViewBase) findViewById(R.id.view);
        m_openCvCameraView.setCameraIndex(1);
        m_openCvCameraView.setVisibility(SurfaceView.VISIBLE);
        m_openCvCameraView.setCvCameraViewListener(this);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(getApplicationContext(), "No camera permission granted", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (m_openCvCameraView != null)
            m_openCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initDebug();
        m_openCvCameraView.enableView();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (m_openCvCameraView != null)
            m_openCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        m_absoluteFaceSizeHeight = (int) (height * 0.2);
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat inputFrameRgba = inputFrame.rgba();
        Mat inputFrameGray = inputFrame.gray();

        MatOfRect faces = new MatOfRect();

        if (m_cascadeClassifier != null) {
            m_cascadeClassifier.detectMultiScale(inputFrameGray, faces, 1.1, 2, 2,
                    new Size(m_absoluteFaceSizeHeight, m_absoluteFaceSizeHeight), new Size());
        }

        Rect[] facesArray = faces.toArray();
        for (int i = 0; i <facesArray.length; i++)
            Imgproc.rectangle(inputFrameRgba, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);

        return inputFrameRgba;
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    initializeOpenCVDependencies();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    private void initializeOpenCVDependencies() {


        try {
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_default.xml");

            m_cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e("OpenCVActivity", "Error loading cascade", e);
            throw new IllegalStateException("Error loading cascade", e);
        }

        m_openCvCameraView.enableView();
    }
}