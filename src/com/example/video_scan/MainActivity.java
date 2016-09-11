package com.example.video_scan;

import com.android.serialport.SerialPort;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;
//import org.xboot.test.SerialActivity.ReadThread;
import com.android.serialport.SerialPort;
import com.example.explorer.ExDialog;

import android.R.integer;
import android.R.string;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;


public class MainActivity extends Activity {
	private Button bt_start_record;
	private Button btPlayOld;
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
	private Button curFreqUpButton;
	private Button curFreqDownButton;
//	private Button freqSet;
	private EditText curFreqEditText;
	private double curFreqHz=(double)1080*1000*1000;
	private double maxFreqHz=(double)2530*1000*1000;
	private double minFreqHz=(double)980*1000*1000;
	private double freqHz=(double)980*1000*1000;
	private double freqSpanHz=(double)100*1000;
	private double MHZ=(double)1000*1000;
	private double KHZ=(double)1000;
	private static final int REQUEST_EX = 1;
	
	//串口测试临时数据----start
	private Button mBtnStart;
	private Button mBtnSend;
	private TextView mTextMsg;
	final String mTestString = "0123456789";
	//---------------------end
	FileOutputStream mOutputStream = null;
	
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				String dataRecv = msg.getData().getString("RECV");
				if(dataRecv!=null)
					refreshLogView("RECV:" + dataRecv + "\r\n");
				String dataSend = msg.getData().getString("SEND");
				if(dataSend!=null)
					refreshLogView("SEND:" + dataSend + "\r\n");				
				//mTextMsg.append("RECV:" + data + "\r\n");
				break;
			case 2:
				curFreqEditText.setText(msg.getData().getString("FREQ")+"");
				break;
			default:
				break;
			}
		}
	};
	
	private TextWatcher curFreqTextWatcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub	
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub			
		}
	};
	
	private void freqInputErrDialog(Context context, String msg){ 	  
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		final AlertDialog dialog; 
		builder.setTitle("错误:" + msg + "MHz");//设置标题 
		builder.setIcon(R.drawable.ic_launcher);//设置图标 
		builder.setMessage("频率超出范围" + "(" + minFreqHz/MHZ + "-" + maxFreqHz/MHZ +"MHz)" 
								+ "\r\n" + "请重新输入");//设置内容
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				printfCurFreqToScreen(curFreqHz);
			}
		});
		dialog=builder.show(); 	
	};
	
	void refreshLogView(String msg){
		mTextMsg.append(msg);
		int offset=mTextMsg.getLineCount()*mTextMsg.getLineHeight();
		if(offset>mTextMsg.getHeight()){
			mTextMsg.scrollTo(0,offset-mTextMsg.getHeight());
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
        btPlayOld = (Button) findViewById(R.id.playOld);
//        freqSet = (Button) findViewById(R.id.freq_set);
        curFreqDownButton = (Button) findViewById(R.id.freq_down);
        curFreqUpButton = (Button) findViewById(R.id.freq_up);
        curFreqEditText = (EditText) findViewById(R.id.cur_freq);
        curFreqEditText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        curFreqEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        curFreqEditText.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
//        curFreqEditText.setText(Double.toString(curFreqHz));
        curFreqEditText.addTextChangedListener(curFreqTextWatcher);
        curFreqEditText.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {  
            @Override  
            public void onFocusChange(View v, boolean hasFocus) {  
                if(hasFocus) {
		        // 此处为得到焦点时的处理内容
		        } else {
		        // 此处为失去焦点时的处理内容
		        	double tmpValue=scanfCurFreqFromScreen(curFreqEditText);
					if((tmpValue<minFreqHz) || (tmpValue>maxFreqHz)){
						freqInputErrDialog(MainActivity.this,"");
					}else {
						curFreqHz = tmpValue;
					}
		        }
            }
        });
        curFreqEditText.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				if(actionId == EditorInfo.IME_ACTION_DONE){
		        	double tmpValue=scanfCurFreqFromScreen(curFreqEditText);
					if((tmpValue<minFreqHz) || (tmpValue>maxFreqHz)){
						freqInputErrDialog(MainActivity.this,Double.toString(tmpValue/MHZ));
					}else {
						curFreqHz = tmpValue;
						setCurFreqSerialPort(curFreqHz);
						InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE); 
						imm.hideSoftInputFromWindow(curFreqEditText.getWindowToken(), 0);
					}					
					return true;
				}
				return false;
			}
		});
        
        printfCurFreqToScreen(curFreqHz);
//        mBtnStart = (Button)findViewById(R.id.serial_test);
//        mBtnSend = (Button)findViewById(R.id.serial_send);
        mTextMsg = (TextView)findViewById(R.id.serial_recv);
        mTextMsg.setMovementMethod(ScrollingMovementMethod.getInstance()) ;
        initSurfaceView();
        
		mTextMsg.setText("");
		serialTest("/dev/ttyAMA1");
		
		btPlayOld.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.putExtra("explorer_title",
						getString(R.string.dialog_read_from_dir));
				intent.setDataAndType(Uri.fromFile(new File("/storage/sdcard1")), "*/*");
				intent.setClass(MainActivity.this, ExDialog.class);
				startActivityForResult(intent, REQUEST_EX);
			}
		});
		
//		freqSet.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				double tmpValue=scanfCurFreqFromScreen(curFreqEditText);
//				if((tmpValue<minFreqHz) || (tmpValue>maxFreqHz)){
//					freqInputErrDialog(MainActivity.this,"");
//				}else {
//					curFreqHz = tmpValue;
//					setCurFreqSerialPort(curFreqHz);
////					serialSendDatas("AT_SMFREQ=" + curFreqHz/KHZ + "\r\n");
//				}
//			}
//		});
		
		curFreqUpButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				curFreqUpProcess();
	
			}
		});	
		
		curFreqUpButton.setOnTouchListener(new OnTouchListener() {
			boolean upLongClicked; 
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					curFreqUpButton.setBackgroundResource(R.drawable.bg_alibuybutton_pressed);
					upLongClicked = true; 
					Thread t = new Thread(){
						@Override
						public void run() {
							// TODO Auto-generated method stub
							super.run();
							while(upLongClicked){
								curFreqUpProcess();
								try{  
                                    Thread.sleep(250);  
                                }catch(InterruptedException e){  
                                    e.printStackTrace();  
                                }
							}
						}
						
					};
					t.start();
					return true;
				}else if (event.getAction() == MotionEvent.ACTION_UP) {
					upLongClicked = false;
					curFreqUpButton.setBackgroundResource(R.drawable.bg_alibuybutton_default);
					return true;
				}
				return false;
			}
		});
		curFreqDownButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				curFreqDownProcess();
			}
		});
		
		
		curFreqDownButton.setOnTouchListener(new OnTouchListener() {
			boolean downLongClicked; 
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					curFreqDownButton.setBackgroundResource(R.drawable.bg_alibuybutton_pressed);
					downLongClicked = true; 
					Thread t = new Thread(){
						@Override
						public void run() {
							// TODO Auto-generated method stub
							super.run();
							while(downLongClicked){
								curFreqDownProcess();
								try{  
                                    Thread.sleep(250);  
                                }catch(InterruptedException e){  
                                    e.printStackTrace();  
                                }
							}
						}
						
					};
					t.start();
					return true;
				}else if (event.getAction() == MotionEvent.ACTION_UP) {
					downLongClicked = false;
					curFreqDownButton.setBackgroundResource(R.drawable.bg_alibuybutton_default);
					return true;
				}
				return false;
			}
		});
		
//        mBtnStart.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
////				// TODO Auto-generated method stub
////				mTextMsg.setText("");
////				//serialTest("/dev/ttyAMA0");
////				serialTest("/dev/ttyAMA1");
////				//serialTest("/dev/ttyAMA2");
////				//serialTest("/dev/ttyAMA3");
//			}
//		});
//        mBtnSend.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				serialSendDatas("1234567890");
//			}
//		});
        
    }

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		String path;
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_EX) {
				Uri uri = intent.getData();
				mTextMsg.append("select: " + uri);
			}
		}
	};
    
    private void initSurfaceView() {
    	mSurfaceview = (SurfaceView) this.findViewById(R.id.mediarecorder2_Surfaceview); 
    	mSurfaceview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    	mSurfaceview.setKeepScreenOn(true);
        callback = new SurfaceHolder.Callback() {
			
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				// TODO Auto-generated method stub
                // 如果camera不为null ,释放摄像头  
                if (camera != null)  
                {  
                    //7.结束程序时，调用Camera的StopPriview()结束取景预览，并调用release()方法释放资源.  
                    camera.stopPreview();  
                    camera.release();  
                    camera = null;  
                } 				
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
    };
    
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
    };

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
//			readSerial();
//			readSerial();
//			readSerial();
//			readSerial();
//			readSerial();

			if (mSerialPort != null) {
				mSerialPort.close();
				mSerialPort = null;
			}
//			if (!isInterrupted())
//				interrupt();
		}	
		
		void readSerial() {
			int size;
			while (!isInterrupted()) {
				try {
					if (mInputStream == null)
						return;

//					size = mInputStream.available();
//					if (size <= 0) {
//						try {
//							sleep(50);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//						return;
//					}

					byte[] buffer = new byte[256];
					size = mInputStream.read(buffer);
					if (size > 0) {
						refreshSerialRecvLogView(size,buffer);
//						String data = new String(buffer, 0, size);
//						//if (mTestString.equals(data)) {
//							Message msg = new Message();
//							msg.what = 1;
//							Bundle bundle = new Bundle();
//							bundle.putString("RECV", new String(mDevice
//									+ " RECV : " + data));
//							msg.setData(bundle);
//							mHandler.sendMessage(msg);
//						//}
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}					
			}

		}
    }
    
	public void serialTest(String device) {
		SerialPort mSerialPort = null;
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
	}
	
	public void serialSendDatas(String datas) {
		try {
			//mTextMsg.append(device + " SEND : " + datas + "\n");
			//outStr.append(device);
			//mOutputStream.write(outStr.toString().getBytes());
			mOutputStream.write(datas.getBytes());
			refreshSerialSendLogView(datas);
			//mTextMsg.append("SEND:" + datas + "\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public void refreshSerialSendLogView(String datas) {
		Message msg = new Message();
		msg.what = 1;
		Bundle bundle = new Bundle();
		bundle.putString("SEND", datas);
		msg.setData(bundle);
		mHandler.sendMessage(msg);	
	}

	public void refreshSerialRecvLogView(int size,byte[] buffer) {
		String datas = new String(buffer, 0, size);
		Message msg = new Message();
		msg.what = 1;
		Bundle bundle = new Bundle();
		bundle.putString("RECV", datas);
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}
	
	public void printfCurFreqToScreen(double curFreq) {
		Message msg = new Message();
		msg.what = 2;
		Bundle bundle = new Bundle();
		bundle.putString("FREQ", Double.toString(curFreq/MHZ));
		msg.setData(bundle);
		mHandler.sendMessage(msg);
		
//		curFreqEditText.setText(Double.toString(curFreq/MHZ)+"");
	}

	public double scanfCurFreqFromScreen(EditText curFreqET) {
		StringTokenizer st= new StringTokenizer(curFreqET.getText().toString());
		double tmpValue=Double.parseDouble(st.nextToken());
		
		return tmpValue*MHZ;
	}
	
	public void setCurFreqSerialPort(double curFreq) {
		long tmp= (long)(curFreqHz/KHZ);
		serialSendDatas("AT_SMFREQ=" + tmp + "\r\n");
		return;
	}
	
	public void curFreqUpProcess() {
		
		if((curFreqHz+freqSpanHz)<=maxFreqHz){
			curFreqHz += freqSpanHz;//add 100KHz
			printfCurFreqToScreen(curFreqHz);
			setCurFreqSerialPort(curFreqHz);
//			curFreqEditText.setText(Long.toString(curFreqHz));
//			serialSendDatas("AT_SMFREQ=" + (long)curFreqHz/KHZ + "\r\n");
		}
		else {
			freqInputErrDialog(MainActivity.this,Double.toString(curFreqHz+freqSpanHz));
		}
		return;
	}
	
	public void curFreqDownProcess() {
		if(curFreqHz>=minFreqHz+freqSpanHz){
			curFreqHz -=freqSpanHz;
			printfCurFreqToScreen(curFreqHz);
			setCurFreqSerialPort(curFreqHz);
//			curFreqEditText.setText(Long.toString(curFreqHz));
//			serialSendDatas("AT_SMFREQ=" + curFreqHz/KHZ + "\r\n");
		}else {
			freqInputErrDialog(MainActivity.this,Double.toString(curFreqHz-freqSpanHz));
		}	
	}
}
