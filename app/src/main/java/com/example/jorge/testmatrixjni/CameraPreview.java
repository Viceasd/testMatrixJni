/*
*  CameraPreview.java
*/
package com.example.jorge.testmatrixjni;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.List;

public class CameraPreview implements SurfaceHolder.Callback, Camera.PreviewCallback
{
	private Camera mCamera;
	private ImageView MyCameraPreview = null;
	private Bitmap bitmap = null;
	private int[] pixels = null;
	private byte[] FrameData = null;
	private int imageFormat;
	private int PreviewSizeWidth;
 	private int PreviewSizeHeight;
 	private boolean bProcessing = false;
	public boolean pasaUnaVez = false;
	private MediaRecorder myMediaRecorder = null;
	private SurfaceView	myCameraSView;
	private SurfaceHolder myCamSHolder;

 	Handler mHandler = new Handler(Looper.getMainLooper());

	public CameraPreview(int PreviewlayoutWidth, int PreviewlayoutHeight,
						 ImageView CameraPreview)
    {
		PreviewSizeWidth = PreviewlayoutWidth;
    	PreviewSizeHeight = PreviewlayoutHeight;
    	MyCameraPreview = CameraPreview;
    	bitmap = Bitmap.createBitmap(PreviewSizeWidth, PreviewSizeHeight, Bitmap.Config.ARGB_8888);
    	pixels = new int[PreviewSizeWidth * PreviewSizeHeight];
    }

	@Override
	public void onPreviewFrame(byte[] arg0, Camera arg1)
	{
		// At preview mode, the frame data will push to here.
		if (imageFormat == ImageFormat.NV21)
        {
			//We only accept the NV21(YUV420) format.
			if ( !bProcessing )
			{
				FrameData = arg0;

				mHandler.post(DoImageProcessing);
			}
        }
	}
    static
    {
        // System.loadLibrary("opencv_java"); //load opencv_java lib
        //System.loadLibrary("ImageProcessing");
        System.loadLibrary("native-lib");
    }
    public native boolean stringFromJNI(int width, int height,
                                        byte[] NV21FrameData, int [] pixels);

	public void onPause()
    {
    	mCamera.stopPreview();
    }

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
	{


//		// BEGIN_INCLUDE (configure_preview
//		mCamera = CameraHelper.getDefaultCameraInstance();
//
//		// We need to make sure that our preview and recording video size are supported by the
//		// camera. Query camera to find all the sizes and choose the optimal size given the
//		// dimensions of our preview surface.
//		Camera.Parameters parameters = mCamera.getParameters();
//		List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
//		List<Camera.Size> mSupportedVideoSizes = null;
//		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
//			mSupportedVideoSizes = parameters.getSupportedVideoSizes();
//		}
//		Camera.Size optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
//				mSupportedPreviewSizes, PreviewSizeWidth, PreviewSizeHeight);
//
//		// Use the same size for recording profile.
//		CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
//		profile.videoFrameWidth = optimalSize.width;
//		profile.videoFrameHeight = optimalSize.height;
//
//		// likewise for the camera object itself.
//		parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);
//		mCamera.setParameters(parameters);



//	    Parameters parameters; // **
//        arg0.setFormat(ImageFormat.NV21);
//	    parameters = mCamera.getParameters();  //   **
//////		parameters.setPreviewFormat(ImageFormat.NV21);
////		// Set the camera preview size
//		parameters.setPreviewSize(PreviewSizeWidth, PreviewSizeHeight); // **
//
//	//	mCamera.setParameters(parameters);
//	//	parameters.setPreviewSize(640, 480);
//	//	parameters.setPreviewSize(2560, 1440);
//		imageFormat = parameters.getPreviewFormat();
//
//
//	//	mCamera.setParameters(parameters);
//
//		mCamera.startPreview();

		//esto es nuevo   *1*1*1
		PrepareMedia(PreviewSizeWidth, PreviewSizeHeight);

		Parameters parameters;

		parameters = mCamera.getParameters();
		// Set the camera preview size
		parameters.setPreviewSize(PreviewSizeWidth, PreviewSizeHeight);
//      parameters.setPreviewSize(1920, 1080);
		imageFormat = parameters.getPreviewFormat();

		mCamera.setParameters(parameters);

		mCamera.startPreview();
	}


	@Override
	public void surfaceCreated(SurfaceHolder arg0)
	{
		mCamera = Camera.open();
		try
		{
			// If did not set the SurfaceHolder, the preview area will be black.

			mCamera.setPreviewDisplay(arg0);
			mCamera.setPreviewCallback(this);
		}
		catch (IOException e)
		{
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0)
	{
    	mCamera.setPreviewCallback(null);
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}


    private Runnable DoImageProcessing = new Runnable()
    {
        public void run()
        {
    		Log.i("MyRealTimeImessing", "DoImageProcessing():");

        	bProcessing = true;
            stringFromJNI(PreviewSizeWidth, PreviewSizeHeight, FrameData, pixels);

			bitmap.setPixels(pixels, 0, PreviewSizeWidth, 0, 0, PreviewSizeWidth, PreviewSizeHeight);
			MyCameraPreview.setImageBitmap(bitmap);
			bProcessing = false;
        }
    };


	public void PrepareMedia(int wid, int hei) {
		myMediaRecorder =  new MediaRecorder();
	//	mCamera.stopPreview();
	//	mCamera.unlock();

		myMediaRecorder.setCamera(mCamera);
		myMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		myMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		CamcorderProfile targetProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
		targetProfile.quality = 60;
		targetProfile.videoFrameWidth = wid;
		targetProfile.videoFrameHeight = hei;
		targetProfile.videoFrameRate = 30;
		targetProfile.videoCodec = MediaRecorder.VideoEncoder.H264;
		targetProfile.audioCodec = MediaRecorder.AudioEncoder.AMR_NB;
		targetProfile.fileFormat = MediaRecorder.OutputFormat.MPEG_4;
		myMediaRecorder.setProfile(targetProfile);
	}

	private boolean realyStart(SurfaceHolder sh) {

		myMediaRecorder.setPreviewDisplay(sh.getSurface());
		try {
			myMediaRecorder.prepare();
		} catch (IllegalStateException e) {
			releaseMediaRecorder();
			Log.d("TEAONLY", "JAVA:  camera prepare illegal error");
			return false;
		} catch (IOException e) {
			releaseMediaRecorder();
			Log.d("TEAONLY", "JAVA:  camera prepare io error");
			return false;
		}

		try {
			myMediaRecorder.start();
		} catch( Exception e) {
			releaseMediaRecorder();
			Log.d("TEAONLY", "JAVA:  camera start error");
			return false;
		}

		return true;
	}



	public boolean StartRecording(String targetFile, SurfaceHolder sh) {

		myMediaRecorder.setOutputFile(targetFile);
		myMediaRecorder.setMaxDuration(7200000); 	// Set max duration 2 hours
		myMediaRecorder.setMaxFileSize(400000000); // Set max file size 4G

		return realyStart(sh);
	}

	public void StopMedia() {
		myMediaRecorder.stop();
		releaseMediaRecorder();
	}

	private void releaseMediaRecorder(){
		if (myMediaRecorder != null) {
			myMediaRecorder.reset();   // clear recorder configuration
			myMediaRecorder.release(); // release the recorder object
			myMediaRecorder = null;
			mCamera.lock();           // lock camera for later use
			mCamera.startPreview();
		}
		myMediaRecorder = null;
	}


}
