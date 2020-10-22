package com.example.flutter3d;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.opengl.GLU;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Surface;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import io.flutter.app.FlutterActivity;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.GeneratedPluginRegistrant;
import io.flutter.view.TextureRegistry;


public class MainActivity extends FlutterActivity
{
	private TextureRegistry.SurfaceTextureEntry mTextureEntry;
	private SurfaceTexture mSurfaceTexture;
	private GLThread mGLThread;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		GeneratedPluginRegistrant.registerWith(this);

		mTextureEntry = getFlutterView().createSurfaceTexture();
		mSurfaceTexture = mTextureEntry.surfaceTexture();

		MethodChannel methodChannel = new MethodChannel(getFlutterView(), "flutter3d/demo1");
		methodChannel.setMethodCallHandler(this::methodCallHandler);

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		mSurfaceTexture.setDefaultBufferSize(metrics.widthPixels, metrics.heightPixels);
		mGLThread = new GLThread(mSurfaceTexture, metrics.widthPixels, metrics.heightPixels, new CubeRenderer());
		mGLThread.start();
	}

	private void methodCallHandler(MethodCall methodCall, MethodChannel.Result result)
	{
		if(methodCall.method.equals("getTextureId")){
			result.success(mTextureEntry.id());
		}
		else{
			result.notImplemented();
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		mGLThread.terminate();
	}
}


class CubeRenderer implements GLThread.GLRenderer
{
	private Cube mCube = new Cube();
	private float mCubeRotation;

	@Override
	public void onCreated(GL10 gl, EGLConfig config, int width, int height)
	{
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);

		gl.glClearDepthf(1.0f);

		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);

		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();

		GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f, 100.0f);
		gl.glViewport(0, 0, width, height);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	@Override
	public void onDrawFrame(GL10 gl)
	{
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		gl.glLoadIdentity();

		gl.glTranslatef(0.0f, 0.0f, -10.0f);
		gl.glRotatef(mCubeRotation, 1.0f, 1.0f, 1.0f);

		mCube.draw(gl);

		gl.glLoadIdentity();

		mCubeRotation -= 0.15f;
	}
}
