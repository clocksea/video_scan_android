package com.example.video_scan;

import com.android.serialport.SerialPort;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
//import org.xboot.test.SerialActivity.ReadThread;
import com.android.serialport.SerialPort;

import android.R.string;
import android.app.Activity;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
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
	
	//串口测试临时数据----start
	private Button mBtnStart;
	private Button mBtnSend;
	private TextView mTextMsg;
	final String mTestString = "0123456789";
	//---------------------end
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				String data = msg.getData().getString("RECV");
				mTextMsg.append("--> " + data);
				break;
			default:
				break;
			}
		}
	};
	
	
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
        mBtnStart = (Button)findViewById(R.id.serial_test);
        mTextMsg = (TextView)findViewById(R.id.serial_recv);
        initSurfaceView();
        
        mBtnStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mTextMsg.setText("");
				//serialTest("/dev/ttyAMA0");
				serialTest("/dev/ttyAMA1");
				//serialTest("/dev/ttyAMA2");
				//serialTest("/dev/ttyAMA3");
			}
		});
        
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
    
    class ReadThread extends Thread{
		SerialPort mSerialPort = null;
		String mDevice = null;
		FileInputStream mInputStream = null;

		ReadThread(SerialPort port, String device) {
			mSerialPort = port;
			mDevice = device;
			mInputStream = (FileInputStream) mSerialPort.getInputStream();
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			readSerial();
			readSerial();
			readSerial();
			readSerial();
			readSerial();
			readSerial();

			if (mSerialPort != null) {
				mSerialPort.close();
				mSerialPort = null;
			}
			if (!isInterrupted())
				interrupt();
		}	
		
		void readSerial() {
			int size;
			try {
				if (mInputStream == null)
					return;

				size = mInputStream.available();
				if (size <= 0) {
					try {
						sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return;
				}

				byte[] buffer = new byte[256];
				size = mInputStream.read(buffer);
				if (size > 0) {
					String data = new String(buffer, 0, size);
					if (mTestString.equals(data)) {
						Message msg = new Message();
						msg.what = 1;
						Bundle bundle = new Bundle();
						bundle.putString("RECV", new String(mDevice
								+ " RECV : " + data));
						msg.setData(bundle);
						mHandler.sendMessage(msg);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
    }
    
	public void serialTest(String device) {
		SerialPort mSerialPort = null;
		FileOutputStream mOutputStream = null;
		ReadThread mReadThread = null;
		StringBuffer outStr = null;
		
		try {
			mSerialPort = new SerialPort(new File(device), 57600, 0);
			mOutputStream = (FileOutputStream) mSerialPort.getOutputStream();

			mReadThread = new ReadThread(mSerialPort, device);
			mReadThread.start();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			mTextMsg.append(device + " SEND : " + mTestString + "\n");
			//outStr.append(device);
			//mOutputStream.write(outStr.toString().getBytes());
			mOutputStream.write(mTestString.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
