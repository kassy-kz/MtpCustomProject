package orz.kassy.mtpcustom;

import android.mtp.MtpDeviceInfo;
import android.mtp.custom.MtpDevice;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

    private static final int OPTIONS_ITEM_ID_UP      = 0;
    private static final int OPTIONS_ITEM_ID_DOWN    = 1;
    private static final int OPTIONS_ITEM_ID_FOCUS   = 2;
    private static final int OPTIONS_ITEM_ID_SHUTTER = 3;
    private static final String TAG = null;
    private int mTmpInt = 1;

    private UsbManager mManager;
    private UsbDevice mDevice;
    private UsbDeviceConnection mDeviceConnection;
    private UsbInterface mInterface;
    private android.mtp.custom.MtpDevice mMtpDevice; // カスタムライブラリ

    /**
     * MTPデバイスへの接続を行う
     */
	private void doMtpConnect() {
		mManager = (UsbManager)getSystemService(Context.USB_SERVICE);

        // check for existing devices        
        for (UsbDevice device :  mManager.getDeviceList().values()) {
            UsbDeviceConnection connection = mManager.openDevice(device);
            if (connection != null) {
                mDeviceConnection = connection;
                mMtpDevice = new MtpDevice(device);
                if(mMtpDevice != null ){
                    boolean ret;
                    ret = mMtpDevice.open(mDeviceConnection);
                    if(!ret){
                        Toast.makeText(this, "device open NG", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "device open OK", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Log.e(TAG,"mMtpDevice is null");                    
                }
            }else {
                Log.e(TAG,"connection is null");
            }
        }
	}

	/**
	 * MTPデバイスの情報を取得する
	 */
	private void doMtpGetDeviceInfo() {
		MtpDeviceInfo deviceInfo = mMtpDevice.getDeviceInfo();
		if(deviceInfo == null) {
			logToast("device info null");   
		}
		String str = deviceInfo.getModel();
		logToast(str);
	}

	/**
	 * (独自追加APIで)シャッターを切る
	 */
	private void doMtpShutter() {
		mMtpDevice.doDeviceShutter();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);

        menu.add(Menu.NONE, OPTIONS_ITEM_ID_UP, Menu.NONE, "Button1")
            .setIcon(R.drawable.ic_launcher)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menu.add(Menu.NONE, OPTIONS_ITEM_ID_SHUTTER, Menu.NONE, "Button2")
            .setIcon(R.drawable.ic_launcher)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn1 = (Button) findViewById(R.id.button1);
        btn1.setOnClickListener(this);
        Button btn2 = (Button) findViewById(R.id.button2);
        btn2.setOnClickListener(this);
        Button btn3 = (Button) findViewById(R.id.button3);
        btn3.setOnClickListener(this);
        Button btn4 = (Button) findViewById(R.id.button4);
        btn4.setOnClickListener(this);
    }

    
    /**
     * OptionsMenu をクリックしたとき
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem menu){
        switch(menu.getItemId()){
            case OPTIONS_ITEM_ID_UP:
               break;
            case OPTIONS_ITEM_ID_SHUTTER:
                break;
             default:
                break;
        }
        return false;
    }


	private void doMtpSetShutterSpeed(int value) {
		mMtpDevice.setShutterSpeed(value);
	}
    
	private void logToast(String str){
	   Toast.makeText(this, str, Toast.LENGTH_LONG).show();
	   Log.e(TAG,str);
	}
	
//    static {
//        System.loadLibrary("mtpcustom");
//        System.loadLibrary("mtpjnicustom");
//    }

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.button1:
	        doMtpConnect();
			break;

		case R.id.button2:
            doMtpGetDeviceInfo();
			break;
			
		case R.id.button3:
			doMtpShutter();
			break;
			
		case R.id.button4:
			shutterSpeed *= 2;
			doMtpSetShutterSpeed(shutterSpeed);
			logToast("shutterSpeed = " + shutterSpeed);
			//func3();
			//logToast("native func "+func1());
			
			break;
			
		}
		
	}

	// memo 0x0000 000f => 10"
	//      0x0000 00ff => 1/8000
	//      0x0000 001f => 0"6;
	//      0xff00 0000 => bulb
	private int shutterSpeed = 0x0000001f;
	
	public native int add(int x, int  y);
    public native String  stringFromJNI();
    public native int  func1();
    public native int  func3();
    public native void native_close();
    private native MtpDeviceInfo native_get_device_info();
// kashimoto add start
    private native MtpDeviceInfo native_get_device_info2();
// kashimoto add end

}