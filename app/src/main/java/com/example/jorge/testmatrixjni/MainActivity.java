package com.example.jorge.testmatrixjni;

import android.annotation.TargetApi;
import android.app.Activity;


import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;



public class MainActivity extends Activity {
    private CameraPreview camPreview;
    private ImageView MyCameraPreview = null;
    private FrameLayout mainLayout;
    private Button btn_grabar;
//    private int PreviewSizeWidth = 2560;
//    private int PreviewSizeHeight = 1440;
//    private int PreviewSizeWidth = 1920;
//    private int PreviewSizeHeight = 1080;
//    private int PreviewSizeWidth = 640;
//    private int PreviewSizeHeight = 480;
    private int PreviewSizeWidth = 720;
    private int PreviewSizeHeight = 405;
    private MediaRecorder mrec;
    private SurfaceView camView;
    private SurfaceHolder camHolder;
    private Boolean grabando = false;
    private File file;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //Set this APK Full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Set this APK no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        anchoYAltoPantalla();

        //
        // Create my camera preview
        //
        MyCameraPreview = new ImageView(this);

        camView = new SurfaceView(this);
        camHolder = camView.getHolder();
        camPreview = new CameraPreview(PreviewSizeWidth, PreviewSizeHeight, MyCameraPreview);

        camHolder.addCallback(camPreview);
        camHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        botonGuardar(camView);

        mainLayout.addView(btn_grabar);

        final Toast toast = Toast.makeText(this, "width: "+PreviewSizeWidth+" height: "+PreviewSizeHeight, Toast.LENGTH_LONG);

//        file = new File(getApplicationContext().getFilesDir(), "matrix_view");
//        file = new File(Environment.getExternalStorageDirectory() + File.separator
//                + Environment.DIRECTORY_DCIM + File.separator + "FILE_NAME");
//        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator+ "test.mp4");

//        file=new File("/mnt/sdcard/test.mp4");
        file = new File(Environment.getExternalStorageDirectory(), "Notes");
        mrec = new MediaRecorder();
        mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mrec.setAudioSource(MediaRecorder.AudioSource.MIC);
        mrec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mrec.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
        mrec.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mrec.setVideoFrameRate(10);
        mrec.setMaxDuration(10000);
        mrec.setVideoSize(PreviewSizeWidth, PreviewSizeHeight);
        mrec.setOutputFile(file.getPath());
        mrec.setPreviewDisplay(camHolder.getSurface());
        if (mrec != null) {

            try {
                mrec.prepare();

            } catch (IllegalStateException e) {
                Log.e("IllegalStateException", e.toString());
            } catch (IOException e) {
                Log.e("IOException", e.toString());
            }
        }



        btn_grabar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!grabando){
                    btn_grabar.setText("Pausar");
                    grabando = true;
                    // Perform action on click
//                    File file = new File(getApplicationContext().getFilesDir(), "matrix_view");
//                    mrec = new MediaRecorder();
//
//                    mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//                    mrec.setPreviewDisplay(camHolder.getSurface());
//                    mrec.setAudioSource(MediaRecorder.AudioSource.MIC);
//                    mrec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//                    mrec.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
//                    mrec.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//                    mrec.setVideoSize(PreviewSizeWidth, PreviewSizeHeight);
                    //mrec.setPreviewDisplay(camHolder.getSurface());

//                    mrec.setOutputFile(file.getPath());
//                    try {
//                        mrec.prepare();
                        mrec.start();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                    toast.show();

                }
                else{
                    btn_grabar.setText("Grabar");
                    grabando = false;
                    try{
                        mrec.stop();
                        mrec.release();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }





            }
        });



    }
    @TargetApi(11)
    private void botonGuardar(SurfaceView camView) {
        btn_grabar = new Button(this);
        btn_grabar.setText("Grabar");

        mainLayout = (FrameLayout) findViewById(R.id.frameLayout1);
        mainLayout.addView(camView, new ViewGroup.LayoutParams(PreviewSizeWidth, PreviewSizeHeight));
        mainLayout.addView(MyCameraPreview, new ViewGroup.LayoutParams(PreviewSizeWidth, PreviewSizeHeight));

        ViewGroup.LayoutParams layoutParams =  new ViewGroup.LayoutParams(200, 200);

        btn_grabar.setLayoutParams(layoutParams);
        btn_grabar.setX((PreviewSizeWidth/2)-100);
        btn_grabar.setY(PreviewSizeHeight+100);
    }

    private void anchoYAltoPantalla() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels; // ancho absoluto en pixels
        int height = metrics.heightPixels;

        PreviewSizeWidth = width;
        PreviewSizeHeight =(int)( width * 0.5625);
    }

    protected void onPause()
    {
        if ( camPreview != null) {
            camPreview.onPause();
            mrec.release();
            mrec.reset();
            mrec.release();
        }
        super.onPause();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
  //  public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
//    static {
//        System.loadLibrary("native-lib");
//    }
}
