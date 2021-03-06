package orz.kassy.mtpcustom;

import java.util.ArrayList;

import android.mtp.MtpDeviceInfo;
import android.mtp.custom.MtpDevice;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener, RecognitionListener {

	private SpeechRecognizer mSpeechRecognizer;

    private static final int OPTIONS_ITEM_ID_UP      = 0;
    private static final int OPTIONS_ITEM_ID_DOWN    = 1;
    private static final int OPTIONS_ITEM_ID_FOCUS   = 2;
    private static final int OPTIONS_ITEM_ID_SHUTTER = 3;
    private static final String TAG = null;
	private static final int SHUTTER_SPEED_MAX = 156;
	private static final int SHUTTER_SPEED_MIN = 0;
	private static final int APERTURE_MAX = 156;
	private static final int APERTURE_MIN = 0;
    private int mTmpInt = 1;

    private UsbManager mManager;
    private UsbDevice mDevice;
    private UsbDeviceConnection mDeviceConnection;
    private UsbInterface mInterface;
    private android.mtp.custom.MtpDevice mMtpDevice; // カスタムライブラリ

    
	// memo 
	//      0x0000 00ff => 1/8000
	//      0x0000 009c => 1/8000(MAX)
	//      0x0000 001f => 0"6;
    //      0x0000 000f => 10"
	//      0xff00 0000 => bulb (MIN)
	private int mShutterSpeed = 0x0000000f;
	private int mAperture     = 0x00000045;
	
    /**
     * MTPデバイスへの接続を行う
     * 必ず最初に一回呼び出すこと
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

	/**
	 * （独自追加APIで）シャッタースピードを変更する
	 * @param value　シャッタースピード値
	 */
	private void doMtpSetShutterSpeed(int value) {
		mMtpDevice.setShutterSpeed(value);
	}

	/**
	 * （独自追加APIで）絞りを変更する
	 * @param value　絞り値
	 */
	private void doMtpSetAperture(int value) {
		mMtpDevice.setAperture(value);
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
	
	
	/**
	 * Activity生成時
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnShutter = (Button) findViewById(R.id.buttonShutter);
        btnShutter.setOnClickListener(this);
        Button btnSpeedLeft = (Button) findViewById(R.id.buttonSpeedLeft);
        btnSpeedLeft.setOnClickListener(this);
        Button btnSpeedRight = (Button) findViewById(R.id.buttonSpeedRight);
        btnSpeedRight.setOnClickListener(this);

        Button btnApertureLeft = (Button) findViewById(R.id.buttonApertureLeft);
        btnApertureLeft.setOnClickListener(this);
        Button btnApertureRight = (Button) findViewById(R.id.buttonApertureRight);
        btnApertureRight.setOnClickListener(this);

        // 音声認識準備
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
		mSpeechRecognizer.setRecognitionListener(this);

        
    }

    
    @Override
    protected void onResume() {
        super.onResume();
		startVoiceRecognition();
     ///   doMtpConnect();
    }
    
	@Override
	public void onPause() {
		super.onPause();
		
		mSpeechRecognizer.cancel();
	}

	/**
	 * 音声認識を開始する
	 */
	void startVoiceRecognition() {
		Log.v("", "startListening");
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
		mSpeechRecognizer.startListening(intent);
	}

    
    /**
     * OptionsMenu をクリックしたとき
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem menu){
        switch(menu.getItemId()){
            case OPTIONS_ITEM_ID_UP:
                doMtpConnect();
                break;
            case OPTIONS_ITEM_ID_SHUTTER:
                doMtpGetDeviceInfo();
                break;
             default:
                break;
        }
        return false;
    }

    // ログとトースト両方出す
	private void logToast(String str){
	   Toast.makeText(this, str, Toast.LENGTH_LONG).show();
	   Log.e(TAG,str);
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
			
		case R.id.buttonShutter:
			doMtpShutter();
			break;
			
		case R.id.buttonSpeedLeft:
			setHigherShutterSpeed();
			break;
		case R.id.buttonSpeedRight:
			setLowerShutterSpeed();
			break;
			
		case R.id.buttonApertureLeft:
			setLargeAperture();
			break;
		case R.id.buttonApertureRight:
			setSmallAperture();
			break;
			
		}
		
	}

	private void setSmallAperture() {
		mAperture--;
		if(mAperture < APERTURE_MIN){
			mAperture = APERTURE_MIN;
		}
		doMtpSetAperture(mAperture);
		Log.i(TAG,"shutter speed value = "+mAperture);
	}

	private void setLargeAperture() {
		mAperture++;
		if(mAperture > APERTURE_MAX){
			mAperture = APERTURE_MAX;
		}
		doMtpSetAperture(mAperture);
		Log.i(TAG,"shutter speed value = "+mAperture);
	}

	private void setLowerShutterSpeed() {
		mShutterSpeed--;
		if(mShutterSpeed < SHUTTER_SPEED_MIN){
			mShutterSpeed = SHUTTER_SPEED_MIN;
		}
		doMtpSetShutterSpeed(mShutterSpeed);
		Log.i(TAG,"shutter speed value = "+mShutterSpeed);
	}

	private void setHigherShutterSpeed() {
		mShutterSpeed++;
		if(mShutterSpeed > SHUTTER_SPEED_MAX){
			mShutterSpeed = SHUTTER_SPEED_MAX;
		}
		doMtpSetShutterSpeed(mShutterSpeed);
		Log.i(TAG,"shutter speed value = "+mShutterSpeed);
	}

	
	public native int add(int x, int  y);
    public native String  stringFromJNI();
    public native int  func1();
    public native int  func3();
    public native void native_close();
    private native MtpDeviceInfo native_get_device_info();
// kashimoto add start
    private native MtpDeviceInfo native_get_device_info2();
// kashimoto add end

	@Override
	public void onBeginningOfSpeech() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBufferReceived(byte[] buffer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEndOfSpeech() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError(int error) {
		// エラーの場合は再度音声認識を開始
		startVoiceRecognition();
		
	}

	@Override
	public void onEvent(int eventType, Bundle params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPartialResults(Bundle partialResults) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReadyForSpeech(Bundle params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResults(Bundle results) {
		// 音声認識が行われた場合にその結果が渡される
		ArrayList<String> strings = results
				.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		Log.i("VoiceRec",strings.toString());
		// 結果は文字列配列で渡されるので、その中にコマンドがあるか
		if (strings.contains("絞れ")) {
		    setLargeAperture();
		} else if (strings.contains("開け")) {
		    setSmallAperture();
		} else if (strings.contains("速く")) {
            setHigherShutterSpeed();
        } else if (strings.contains("遅く")) {
            setLowerShutterSpeed();
        } else if (strings.contains("シャッター") || strings.contains("shutter")) {
            doMtpShutter();
		}
		startVoiceRecognition();
		
	}

	@Override
	public void onRmsChanged(float rmsdB) {
		// TODO Auto-generated method stub		
	}

}
