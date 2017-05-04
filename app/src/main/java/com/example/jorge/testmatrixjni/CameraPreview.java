/*
*  CameraPreview.java
*/
package com.example.jorge.testmatrixjni;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.ImageView;

import java.io.IOException;

public class CameraPreview implements SurfaceHolder.Callback, Camera.PreviewCallback
{
	private Camera mCamera = null;
	private ImageView MyCameraPreview = null;
	private Bitmap bitmap = null;
	private int[] pixels = null;
	private byte[] FrameData = null;
	private int imageFormat;
	private int PreviewSizeWidth;
 	private int PreviewSizeHeight;
 	private boolean bProcessing = false;
	public boolean pasaUnaVez = false;

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
	    Parameters parameters;
       // arg0.setFormat(ImageFormat.NV21);
	    parameters = mCamera.getParameters();
//		parameters.setPreviewFormat(ImageFormat.NV21);
		// Set the camera preview size
		parameters.setPreviewSize(PreviewSizeWidth, PreviewSizeHeight);

		mCamera.setParameters(parameters);
//		parameters.setPreviewSize(1920, 1080);
		imageFormat = parameters.getPreviewFormat();


	//	mCamera.setParameters(parameters);

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

	//
	// Native JNI
	//



    private Runnable DoImageProcessing = new Runnable()
    {
        public void run()
        {
    		Log.i("MyRealTimeImessing", "DoImageProcessing():");

//			Log.i("frameData",String.valueOf(FrameData[0]));
//			Log.i("pixels",String.valueOf(pixels[0]));
            String salidaPixeles="";

//			for (int i = 0; i<=30;i++){
//				salidaPixeles = salidaPixeles+String.valueOf(pixels[i])+",";
//
//			}
//			Log.i("pixels: ",salidaPixeles);
//			for (int i = 0; i<=30;i++){
//				salidaPixeles = salidaPixeles+String.valueOf(FrameData[i])+",";
//
//			}
//			Log.i("FrameData: ",salidaPixeles);

            Log.i("largo FrameData", String.valueOf(FrameData.length)); // 3110400
			Log.i("largo pixels", String.valueOf(pixels.length)); // 3.686.400
        	bProcessing = true;
            stringFromJNI(PreviewSizeWidth, PreviewSizeHeight, FrameData, pixels);
			salidaPixeles="";
//			for (int i = 0; i<=155;i++){
//				salidaPixeles = salidaPixeles+String.valueOf(pixels[i])+",";
//
//			}
//			Log.i("pixels1",salidaPixeles);
//			for (int i = 0; i<=255;i++){
//				salidaPixeles = salidaPixeles+String.valueOf(FrameData[i])+",";
//
//			}
			Log.i("FrameData1 ",salidaPixeles);

			bitmap.setPixels(pixels, 0, PreviewSizeWidth, 0, 0, PreviewSizeWidth, PreviewSizeHeight);
			MyCameraPreview.setImageBitmap(bitmap);
			bProcessing = false;
        }
    };
   }
