package com.example.video_scan;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity {
	private Button bt_start_record;
	private Button bt_stop_record;
	private SurfaceView mSurfaceview;         // 显示视频的控件
	private MediaRecorder mMediaRecorder;     // MediaRecorder对象，錄製視頻的類
	private SurfaceHolder mSurfaceHolder;    // 
	private File mRecVedioPath;                // 录制的音視頻文件路徑
	private File mRecAudioFile;                // 录制的音視頻文件
	private TextView tv01,tv02,tv03,tv04,tv05;
	private int hour = 0;
	private int minute = 0;
	private int second = 0;
	private boolean bool;	
	private Camera camera;
	private SurfaceHolder.Callback callback;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* 全局Activity设置 */
        // 隐去Title（程序的名字）
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 設置全屏：隐去电池等图标和一切修饰部分（状态栏部分）
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 选择支持半透明模式,在有surfaceview的activity中使用。        
        getWindow().setFormat(PixelFormat.TRANSLUCENT);        
        setContentView(R.layout.activity_main);
              
        bt_start_record = (Button) findViewById(R.id.start);
        bt_stop_record = (Button) findViewById(R.id.stop);
        initSurfaceView();
        
        
    }

    private void initSurfaceView() {
    	mSurfaceview = (SurfaceView) this.findViewById(R.id.mediarecorder2_Surfaceview); 
    	mSurfaceview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    	mSurfaceview.setKeepScreenOn(true);
        callback = new SurfaceHolder.Callback() {
			
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				// TODO Auto-generated method stub
				//打开相机，同时进行各种控件的初始化mediaRecord等
                camera = Camera.open();
                mMediaRecorder = new MediaRecorder();
			}
			
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width,
					int height) {
				// TODO Auto-generated method stub
				doChange(holder);
			}
		};
        //为SurfaceView设置回调函数
        mSurfaceview.getHolder().addCallback(callback);
    }
    
    //当我们的程序开始运行，即使我们没有开始录制视频，我们的surFaceView中也要显示当前摄像头显示的内容
    private void doChange(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            //设置surfaceView旋转的角度，系统默认的录制是横向的画面，把这句话注释掉运行你就会发现这行代码的作用
            camera.setDisplayOrientation(getDegree());
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    } 
    public int getDegree() {
        //获取当前屏幕旋转的角度
        int rotating = this.getWindowManager().getDefaultDisplay().getRotation();
        int degree = 0;
        return degree;
    }    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
