package com.example.flutter3d;

import android.graphics.SurfaceTexture;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.EGL14.EGL_CONTEXT_CLIENT_VERSION;
import static android.opengl.EGL14.EGL_HEIGHT;
import static android.opengl.EGL14.EGL_OPENGL_ES2_BIT;
import static android.opengl.EGL14.EGL_WIDTH;
import static javax.microedition.khronos.egl.EGL10.EGL_ALPHA_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_BLUE_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_DEPTH_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_GREEN_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_NONE;
import static javax.microedition.khronos.egl.EGL10.EGL_NO_CONTEXT;
import static javax.microedition.khronos.egl.EGL10.EGL_NO_DISPLAY;
import static javax.microedition.khronos.egl.EGL10.EGL_NO_SURFACE;
import static javax.microedition.khronos.egl.EGL10.EGL_RED_SIZE;
import static javax.microedition.khronos.egl.EGL10.EGL_RENDERABLE_TYPE;
import static javax.microedition.khronos.egl.EGL10.EGL_STENCIL_SIZE;


/**
 * Created by Simon Lightfoot <simon@devangels.london> on 07/02/2019.
 */
public class GLThread extends Thread
{
	private final SurfaceTexture mSurfaceTexture;
	private final int mWidth;
	private final int mHeight;
	private final GLRenderer mGLRenderer;

	EGL10 mEgl;
	EGLDisplay mEglDisplay = EGL_NO_DISPLAY;
	EGLContext mEglContext = EGL_NO_CONTEXT;
	EGLSurface mEglSurface = EGL_NO_SURFACE;
	GL10 mGl;

	private volatile boolean quit = false;


	public GLThread(SurfaceTexture surfaceTexture, int width, int height, GLRenderer glRenderer)
	{
		mSurfaceTexture = surfaceTexture;
		mWidth = width;
		mHeight = height;
		mGLRenderer = glRenderer;
	}

	public void terminate()
	{
		quit = true;
		try{
			join(3000);
		}
		catch(InterruptedException e){
			Log.e("GLTerminate", "failed to terminate within 3 seconds");
		}
	}

	@Override
	public void run()
	{
		quit = false;
		init();
		try{
			while(!quit){
				mGLRenderer.onDrawFrame(mGl);
				if(!mEgl.eglSwapBuffers(mEglDisplay, mEglSurface)){
					throw new RuntimeException("eglSwapBuffers failed: " + mEgl.eglGetError());
				}
				//mSurfaceTexture.updateTexImage();
			}
		}
		catch(Exception e){
			// fall thru and exit normally
			Log.e("GLRun", "Run failed", e);
		}
		finally{
			shutdown();
			quit = true;
		}
	}

	private void init()
	{
		mEgl = (EGL10) EGLContext.getEGL();

		mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
		if(mEglDisplay == EGL10.EGL_NO_DISPLAY){
			throw new RuntimeException("eglGetDisplay failed: " + mEgl.eglGetError());
		}

		int[] version = new int[2];
		if(!mEgl.eglInitialize(mEglDisplay, version)){
			throw new RuntimeException("eglInitialize failed: " + mEgl.eglGetError());
		}
		Log.d("gl", "EGL init with version " + version[0] + "." + version[1]);

		int[] eglConfigAttrs = new int[] {
			EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
			EGL_RED_SIZE, 8,
			EGL_GREEN_SIZE, 8,
			EGL_BLUE_SIZE, 8,
			EGL_ALPHA_SIZE, 8,
			EGL_DEPTH_SIZE, 16,
			EGL_NONE,
		};
		int[] numConfigs = new int[1];
		EGLConfig[] configs = new EGLConfig[1];
		if(!mEgl.eglChooseConfig(mEglDisplay, eglConfigAttrs, configs, 1, numConfigs)){
			throw new IllegalArgumentException("eglChooseConfig failed: " + mEgl.eglGetError());
		}
		if(numConfigs[0] <= 0){
			throw new IllegalArgumentException("No configs match eglConfigAttrs");
		}

		mEglContext = mEgl.eglCreateContext(mEglDisplay, configs[0], EGL_NO_CONTEXT, null);
		if(mEglContext == null || mEglContext == EGL_NO_CONTEXT){
			mEglContext = null;
			throw new RuntimeException("eglCreateContext failed: " + mEgl.eglGetError());
		}

		mEglSurface = mEgl.eglCreateWindowSurface(mEglDisplay, configs[0], mSurfaceTexture, null);
		if(mEglSurface == null || mEglSurface == EGL10.EGL_NO_SURFACE){
			throw new RuntimeException("eglCreateWindowSurface failed: " + mEgl.eglGetError());
		}

		if(!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)){
			throw new RuntimeException("eglMakeCurrent failed: " + mEgl.eglGetError());
		}

		mGl = (GL10) mEglContext.getGL();

		mGLRenderer.onCreated(mGl, configs[0], mWidth, mHeight);
	}

	private void shutdown()
	{
		if(!mEgl.eglMakeCurrent(mEglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT)){
			Log.d("GLShutdown", "eglMakeCurrent failed: " + mEgl.eglGetError());
		}
		if(!mEgl.eglDestroyContext(mEglDisplay, mEglContext)){
			Log.d("GLShutdown", "eglDestroyContext failed: " + mEgl.eglGetError());
		}
		if(!mEgl.eglDestroySurface(mEglDisplay, mEglSurface)){
			Log.d("GLShutdown", "eglDestroySurface failed: " + mEgl.eglGetError());
		}
		mEgl.eglTerminate(mEglDisplay);
		mEglDisplay = EGL_NO_DISPLAY;
		mEglSurface = EGL_NO_SURFACE;
		mEglContext = EGL_NO_CONTEXT;
	}

	public interface GLRenderer
	{
		void onCreated(GL10 gl, EGLConfig config, int width, int height);
		void onDrawFrame(GL10 gl);
	}
}
