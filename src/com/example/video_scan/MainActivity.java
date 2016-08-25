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
	private SurfaceView mSurfaceview;         // ��ʾ��Ƶ�Ŀؼ�
	private MediaRecorder mMediaRecorder;     // MediaRecorder������uҕ�l���
	private SurfaceHolder mSurfaceHolder;    // 
	private File mRecVedioPath;                // ¼�Ƶ���ҕ�l�ļ�·��
	private File mRecAudioFile;                // ¼�Ƶ���ҕ�l�ļ�
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
        /* ȫ��Activity���� */
        // ��ȥTitle����������֣�
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // �O��ȫ������ȥ��ص�ͼ���һ�����β��֣�״̬�����֣�
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // ѡ��֧�ְ�͸��ģʽ,����surfaceview��activity��ʹ�á�        
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
				//�������ͬʱ���и��ֿؼ��ĳ�ʼ��mediaRecord��
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
        //ΪSurfaceView���ûص�����
        mSurfaceview.getHolder().addCallback(callback);
    }
    
    //�����ǵĳ���ʼ���У���ʹ����û�п�ʼ¼����Ƶ�����ǵ�surFaceView��ҲҪ��ʾ��ǰ����ͷ��ʾ������
    private void doChange(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            //����surfaceView��ת�ĽǶȣ�ϵͳĬ�ϵ�¼���Ǻ���Ļ��棬����仰ע�͵�������ͻᷢ�����д��������
            camera.setDisplayOrientation(getDegree());
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    } 
    public int getDegree() {
        //��ȡ��ǰ��Ļ��ת�ĽǶ�
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
