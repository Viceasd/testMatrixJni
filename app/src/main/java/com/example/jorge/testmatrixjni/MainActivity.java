package com.example.jorge.testmatrixjni;

import android.annotation.TargetApi;
import android.app.Activity;


import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
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
import java.util.List;


public class MainActivity extends Activity {
    private CameraPreview camPreview;
    private ImageView MyCameraPreview = null;
    private FrameLayout mainLayout;
    private Button btn_grabar;
//    private int PreviewSizeWidth = 2560;
//    private int PreviewSizeHeight = 1440;
//    private int PreviewSizeWidth = 1920;
//    private int PreviewSizeHeight = 1080;
    private int PreviewSizeWidth = 640;
    private int PreviewSizeHeight = 480;
//    private int PreviewSizeWidth = 720;
//    private int PreviewSizeHeight = 405;
    private MediaRecorder mrec;
    private SurfaceView camView;
    private SurfaceHolder camHolder;
    private Boolean grabando = false;
    private File file;

    private boolean isRecording = false;
    private MediaRecorder mMediaRecorder;
    private static final String TAG = "Recorder";
    private File mOutputFile;
    private Camera mCamera;


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
//        file = new File(Environment.getExternalStorageDirectory(), "Notes");
//        mrec = new MediaRecorder();
//        mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//        mrec.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mrec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        mrec.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
//        mrec.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//        mrec.setVideoFrameRate(10);
//        mrec.setMaxDuration(10000);
//        mrec.setVideoSize(PreviewSizeWidth, PreviewSizeHeight);
//        mrec.setOutputFile(file.getPath());
//        mrec.setPreviewDisplay(camHolder.getSurface());
//        if (mrec != null) {
//
//            try {
//                mrec.prepare();
//
//            } catch (IllegalStateException e) {
//                Log.e("IllegalStateException", e.toString());
//            } catch (IOException e) {
//                Log.e("IOException", e.toString());
//            }
//        }

        btn_grabar.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
//               alHacerClickBoton();
               onCaptureClick(v);
           }
        });

//        btn_grabar.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                if (!grabando){
//                    btn_grabar.setText("Pausar");
//                    grabando = true;
//                    // Perform action on click
////                    File file = new File(getApplicationContext().getFilesDir(), "matrix_view");
////                    mrec = new MediaRecorder();
////
////                    mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
////                    mrec.setPreviewDisplay(camHolder.getSurface());
////                    mrec.setAudioSource(MediaRecorder.AudioSource.MIC);
////                    mrec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
////                    mrec.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
////                    mrec.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
////                    mrec.setVideoSize(PreviewSizeWidth, PreviewSizeHeight);
//                    //mrec.setPreviewDisplay(camHolder.getSurface());
//
////                    mrec.setOutputFile(file.getPath());
////                    try {
////                        mrec.prepare();
//                        mrec.start();
////                    } catch (Exception e) {
////                        e.printStackTrace();
////                    }
//                    toast.show();
//
//                }
//                else{
//                    btn_grabar.setText("Grabar");
//                    grabando = false;
//                    try{
//                        mrec.stop();
//                        mrec.release();
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//
//                }
//
//
//
//
//
//            }
//        });



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

    public void alHacerClickBoton() {
        if (isRecording) {
            // BEGIN_INCLUDE(stop_release_media_recorder)

            // stop recording and release camera
            try {
                mMediaRecorder.stop();  // stop the recording
            } catch (RuntimeException e) {
                // RuntimeException is thrown when stop() is called immediately after start().
                // In this case the output file is not properly constructed ans should be deleted.
                Log.d(TAG, "RuntimeException: stop() is called immediately after start()");
                //noinspection ResultOfMethodCallIgnored
                mOutputFile.delete();
            }
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock();         // take camera access back from MediaRecorder

            // inform the user that recording has stopped
            setCaptureButtonText("Capture");
            isRecording = false;
            releaseCamera();
            // END_INCLUDE(stop_release_media_recorder)

        } else {

            // BEGIN_INCLUDE(prepare_start_media_recorder)

            new MediaPrepareTask().execute(null, null, null);

            // END_INCLUDE(prepare_start_media_recorder)

        }
    }
    private void setCaptureButtonText(String title) {
        btn_grabar.setText(title);
    }
    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            // clear recorder configuration
            mMediaRecorder.reset();
            // release the recorder object
            mMediaRecorder.release();
            mMediaRecorder = null;
            // Lock camera for later use i.e taking it back from MediaRecorder.
            // MediaRecorder doesn't need it anymore and we will release it if the activity pauses.
            mCamera.lock();
        }
    }
    private void releaseCamera(){
        if (mCamera != null){
            // release the camera for other applications
            mCamera.release();
            mCamera = null;
        }
    }
    class MediaPrepareTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            // initialize video camera
            if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                mMediaRecorder.start();

                isRecording = true;
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                MainActivity.this.finish();
            }
            // inform the user that recording has started
            setCaptureButtonText("Stop");

        }
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private boolean prepareVideoRecorder(){

        // BEGIN_INCLUDE (configure_preview
        mCamera = CameraHelper.getDefaultCameraInstance();

        // We need to make sure that our preview and recording video size are supported by the
        // camera. Query camera to find all the sizes and choose the optimal size given the
        // dimensions of our preview surface.
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
        Camera.Size optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
                mSupportedPreviewSizes, camView.getWidth(), camView.getHeight());

        // Use the same size for recording profile.
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        profile.videoFrameWidth = optimalSize.width;
        profile.videoFrameHeight = optimalSize.height;

        // likewise for the camera object itself.
        parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);
        mCamera.setParameters(parameters);
        try {
            // Requires API level 11+, For backward compatibility use {@link setPreviewDisplay}
            // with {@link SurfaceView}
            mCamera.setPreviewDisplay(camView.getHolder());
        } catch (IOException e) {
            Log.e(TAG, "Surface texture is unavailable or unsuitable" + e.getMessage());
            return false;
        }
        // END_INCLUDE (configure_preview)


        // BEGIN_INCLUDE (configure_media_recorder)
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setPreviewDisplay(camHolder.getSurface());
       // mMediaRecorder.setCamera(camView.getHolder());

        // Step 2: Set sources

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT );
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(profile);

        // Step 4: Set output file
        mOutputFile = CameraHelper.getOutputMediaFile(CameraHelper.MEDIA_TYPE_VIDEO);
        if (mOutputFile == null) {
            return false;
        }
        mMediaRecorder.setOutputFile(mOutputFile.getPath());
        // END_INCLUDE (configure_media_recorder)

        // Step 5: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }
    public void onCaptureClick(View view) {
        if (isRecording) {
            // BEGIN_INCLUDE(stop_release_media_recorder)

            // stop recording and release camera
            try {
                mMediaRecorder.stop();  // stop the recording
            } catch (RuntimeException e) {
                // RuntimeException is thrown when stop() is called immediately after start().
                // In this case the output file is not properly constructed ans should be deleted.
                Log.d(TAG, "RuntimeException: stop() is called immediately after start()");
                //noinspection ResultOfMethodCallIgnored
                mOutputFile.delete();
            }
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock();         // take camera access back from MediaRecorder

            // inform the user that recording has stopped
            setCaptureButtonText("Capture");
            isRecording = false;
            releaseCamera();
            // END_INCLUDE(stop_release_media_recorder)

        } else {

            // BEGIN_INCLUDE(prepare_start_media_recorder)

            new MediaPrepareTask().execute(null, null, null);

            // END_INCLUDE(prepare_start_media_recorder)

        }
    }
}
