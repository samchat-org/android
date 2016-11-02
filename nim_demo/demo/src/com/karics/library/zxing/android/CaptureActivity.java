package com.karics.library.zxing.android;

import com.android.samchat.R;
import com.android.samservice.SamService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.karics.library.zxing.android.BeepManager;
import com.karics.library.zxing.android.CaptureActivityHandler;
import com.karics.library.zxing.android.FinishListener;
import com.karics.library.zxing.android.InactivityTimer;
import com.karics.library.zxing.android.IntentSource;
import com.karics.library.zxing.camera.CameraManager;
import com.karics.library.zxing.encode.CodeCreator;
import com.karics.library.zxing.view.ViewfinderView;
import com.netease.nim.uikit.NimConstants;
import com.netease.nim.uikit.common.type.ModeEnum;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import android.content.Context;

/**
 * 杩欎釜activity鎵撳紑鐩告満锛屽湪鍚庡彴绾跨▼鍋氬父瑙勭殑鎵弿锛涘畠缁樺埗浜嗕竴涓粨鏋渧iew鏉ュ府鍔╂纭湴鏄剧ず鏉″舰鐮侊紝鍦ㄦ壂鎻忕殑鏃跺€欐樉绀哄弽棣堜俊鎭紝
 * 鐒跺悗鍦ㄦ壂鎻忔垚鍔熺殑鏃跺€欒鐩栨壂鎻忕粨鏋?
 *
 */
public final class CaptureActivity extends Activity implements
        SurfaceHolder.Callback {

    private static final String TAG = CaptureActivity.class.getSimpleName();

    // 鐩告満鎺у埗
    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private IntentSource source;
    private Collection<BarcodeFormat> decodeFormats;
    private Map<DecodeHintType, ?> decodeHints;
    private String characterSet;
    // 鐢甸噺鎺у埗
    private InactivityTimer inactivityTimer;
    // 澹伴煶銆侀渿鍔ㄦ帶鍒?
    private BeepManager beepManager;

    //private ImageButton imageButton_back;
    private FrameLayout back_arrow_layout;
    private RelativeLayout titlebar_layout;
    private ImageView back_icon;
    private TextView titlebar_name;
    private TextView titlebar_right_text;
    private FrameLayout titlebar_right_layout;
    private RelativeLayout scanner_layout;
    private RelativeLayout myqrcode_layout;
    private HeadImageView avatar_hv;
    private TextView username_tv;
    private ImageView qr_code_iv;
    private TextView service_category;

    //0:customer  1:sp
    private int mode;
    private boolean isScanner;

    //public static OnMyQRCodeListner callback;
    private Bitmap qrcode;

    public interface OnMyQRCodeListner {
		public void OnMyQRCodeClick();
	}
		
    private void setTitlebarCustomerMode(){
        titlebar_layout.setBackgroundColor(getResources().getColor(R.color.samchat_color_customer_titlebar_bg));
        back_arrow_layout.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_customer);
        back_icon.setImageResource(R.drawable.samchat_arrow_left);
        titlebar_name.setTextColor(getResources().getColor(R.color.samchat_color_customer_titlbar_title));
        titlebar_right_text.setTextColor(getResources().getColor(R.color.samchat_color_dark_blue));
        titlebar_right_layout.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_customer);
    }

    private void setTitlebarSPMode(){
        titlebar_layout.setBackgroundColor(getResources().getColor(R.color.samchat_color_sp_titlebar_bg));
        back_arrow_layout.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_sp);
        back_icon.setImageResource(R.drawable.samchat_arrow_left_sp);
        titlebar_name.setTextColor(getResources().getColor(R.color.samchat_color_sp_titlbar_title));
        titlebar_right_text.setTextColor(getResources().getColor(R.color.samchat_color_white));
        titlebar_right_layout.setBackgroundResource(R.drawable.samchat_action_bar_button_selector_sp);
    }

    private boolean isCustomerMode(){
        return (mode == 0);
    }

    private void setTitleBar(){
        if(isCustomerMode()){
            setTitlebarCustomerMode();
        }else{
            setTitlebarSPMode();
        }
    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    /*public static void start(Context context){
        Intent intent = new Intent(context, CaptureActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }*/

    public static void startActivityForResult(Activity activity, int requestCode, int m, boolean isScan) {
        Intent intent = new Intent(activity, CaptureActivity.class);
        intent.putExtra("mode", m);
        intent.putExtra("isScanner",isScan);
        activity.startActivityForResult(intent, requestCode);
    }

    private void parseIntent() {
		mode = getIntent().getIntExtra("mode", ModeEnum.CUSTOMER_MODE.getValue());
		isScanner = getIntent().getBooleanExtra("isScanner",true);
    }

    private void switchScanner(){
        if(isScanner){
            pauseScanner();
            scanner_layout.setVisibility(View.GONE);
            myqrcode_layout.setVisibility(View.VISIBLE);
            titlebar_name.setText(getString(R.string.samchat_my_qr_code));
            titlebar_right_text.setText(getString(R.string.samchat_qr_scanner));
            isScanner = false;
            Log.i("test","isScanner:"+isScanner);
        }else{
            isScanner = true;
            scanner_layout.setVisibility(View.VISIBLE);
            myqrcode_layout.setVisibility(View.GONE);
            titlebar_name.setText(getString(R.string.samchat_qr_scanner));
            titlebar_right_text.setText(getString(R.string.samchat_my_qr_code));
            resumeScanner();
            Log.i("test","isScanner:"+isScanner);
        }
    }

    private void setupQrCode(long unique_id,int width, int height){
        try{
			qrcode = CodeCreator.createQRCode(NimConstants.QRCODE_PREFIX+unique_id,width,height);
			qr_code_iv.setImageBitmap(qrcode);
		}catch (WriterException e){
			e.printStackTrace();
		}
    }


    /**
     * OnCreate涓垵濮嬪寲涓€浜涜緟鍔╃被锛屽InactivityTimer锛堜紤鐪狅級銆丅eep锛堝０闊筹級浠ュ強AmbientLight锛堥棯鍏夌伅锛?
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        parseIntent();

        // 淇濇寔Activity澶勪簬鍞ら啋鐘舵€?
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.capture);

        back_arrow_layout = (FrameLayout) findViewById(R.id.back_arrow_layout);
        titlebar_layout = (RelativeLayout) findViewById(R.id.titlebar_layout);
        back_icon = (ImageView) findViewById(R.id.back_icon);
        titlebar_name = (TextView) findViewById(R.id.titlebar_name);
        titlebar_right_text = (TextView) findViewById(R.id.titlebar_right_text);
        scanner_layout = (RelativeLayout) findViewById(R.id.scanner_layout);
        myqrcode_layout = (RelativeLayout) findViewById(R.id.myqrcode_layout);
        titlebar_right_layout = (FrameLayout) findViewById(R.id.titlebar_right_layout);
        avatar_hv = (HeadImageView) findViewById(R.id.avatar);
        username_tv = (TextView) findViewById(R.id.username);
        qr_code_iv = (ImageView) findViewById(R.id.qr_code);
        service_category =  (TextView) findViewById(R.id.service_category);
				
        setTitleBar();
        int labelWidth = ScreenUtil.screenWidth;
        labelWidth -= ScreenUtil.dip2px(96);

        avatar_hv.loadBuddyAvatar(SamService.getInstance().get_current_user().getAccount(), 80);
        username_tv.setText(SamService.getInstance().get_current_user().getusername());
        setupQrCode(SamService.getInstance().get_current_user().getunique_id(),labelWidth,labelWidth);
        if(mode == ModeEnum.CUSTOMER_MODE.getValue()){
            service_category.setVisibility(View.GONE);
        }else{
           service_category.setVisibility(View.VISIBLE); 
           service_category.setText(SamService.getInstance().get_current_user().getservice_category());
        }
				
        hasSurface = false;

        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);

        
        //imageButton_back = (ImageButton) findViewById(R.id.capture_imageview_back);
        back_arrow_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        titlebar_right_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchScanner();
                /*if(callback !=null){
                    callback.OnMyQRCodeClick();
                }*/
            }
        });

        if(isScanner){
            scanner_layout.setVisibility(View.VISIBLE);
            myqrcode_layout.setVisibility(View.GONE);
            titlebar_name.setText(getString(R.string.samchat_qr_scanner));
            titlebar_right_text.setText(getString(R.string.samchat_my_qr_code));
			}else{
            scanner_layout.setVisibility(View.GONE);
            myqrcode_layout.setVisibility(View.VISIBLE);
            titlebar_name.setText(getString(R.string.samchat_my_qr_code));
            titlebar_right_text.setText(getString(R.string.samchat_qr_scanner));
        }

    }

    private void resumeScanner(){
         // CameraManager蹇呴』鍦ㄨ繖閲屽垵濮嬪寲锛岃€屼笉鏄湪onCreate()涓€?
        // 杩欐槸蹇呴』鐨勶紝鍥犱负褰撴垜浠涓€娆¤繘鍏ユ椂闇€瑕佹樉绀哄府鍔╅〉锛屾垜浠苟涓嶆兂鎵撳紑Camera,娴嬮噺灞忓箷澶у皬
        // 褰撴壂鎻忔鐨勫昂瀵镐笉姝ｇ‘鏃朵細鍑虹幇bug
        cameraManager = new CameraManager(getApplication());

        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        viewfinderView.setCameraManager(cameraManager);

        handler = null;

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            // activity鍦╬aused鏃朵絾涓嶄細stopped,鍥犳surface浠嶆棫瀛樺湪锛?
            // surfaceCreated()涓嶄細璋冪敤锛屽洜姝ゅ湪杩欓噷鍒濆鍖朿amera
            initCamera(surfaceHolder);
        } else {
            // 閲嶇疆callback锛岀瓑寰卻urfaceCreated()鏉ュ垵濮嬪寲camera
            surfaceHolder.addCallback(this);
        }

        beepManager.updatePrefs();
        inactivityTimer.onResume();

        source = IntentSource.NONE;
        decodeFormats = null;
        characterSet = null;
    }

    @Override
    protected void onResume() {
        if(isScanner){
            resumeScanner();
        }
        super.onResume();
    }

    private void pauseScanner(){
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        beepManager.close();
        cameraManager.closeDriver();
        if (!hasSurface) {
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
    }

    @Override
    protected void onPause() {
        if(isScanner){
            pauseScanner();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        if(qrcode != null && !qrcode.isRecycled()){
			qrcode.recycle();  
        }
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            if(isScanner){
                initCamera(holder);
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i("test","surfaceDestroyed");
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    /**
     * 鎵弿鎴愬姛锛屽鐞嗗弽棣堜俊鎭?
     *
     * @param rawResult
     * @param barcode
     * @param scaleFactor
     */
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        inactivityTimer.onActivity();

        boolean fromLiveScan = barcode != null;
        //杩欓噷澶勭悊瑙ｇ爜瀹屾垚鍚庣殑缁撴灉锛屾澶勫皢鍙傛暟鍥炰紶鍒癆ctivity澶勭悊
        if (fromLiveScan) {
            beepManager.playBeepSoundAndVibrate();

            //Toast.makeText(this, "鎵弿鎴愬姛", Toast.LENGTH_SHORT).show();

            Intent intent = getIntent();
            intent.putExtra("codedContent", rawResult.getText());
            intent.putExtra("codedBitmap", barcode);
            setResult(RESULT_OK, intent);
            finish();
        }

    }

    /**
     * 鍒濆鍖朇amera
     *
     * @param surfaceHolder
     */
    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            return;
        }
        try {
            // 鎵撳紑Camera纭欢璁惧
            cameraManager.openDriver(surfaceHolder);
            // 鍒涘缓涓€涓猦andler鏉ユ墦寮€棰勮锛屽苟鎶涘嚭涓€涓繍琛屾椂寮傚父
            if (handler == null) {
                handler = new CaptureActivityHandler(this, decodeFormats,
                        decodeHints, characterSet, cameraManager);
            }
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    /**
     * 鏄剧ず搴曞眰閿欒淇℃伅骞堕€€鍑哄簲鐢?
     */
    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage(getString(R.string.msg_camera_framework_bug));
        builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
        builder.setOnCancelListener(new FinishListener(this));
        builder.show();
    }

}
