package com.hotdealelf.hotdeal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.onesignal.OneSignal;

public class MainActivity extends AppCompatActivity {


    private WebView mWebView;

    BackPressCloseHandler backPressCloseHandler = new BackPressCloseHandler(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        setLayout();
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl("https://www.hotdealelf.com");
        mWebView.setWebViewClient(new WebViewClientClass());
        mWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result){
                return super.onJsAlert(view, url, message, result);
            }
        });

        // 다이얼로그 바디
        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(this);
        // 메세지
        alert_confirm.setMessage("앱 전용 혜택, 실시간 세일정보 등의\n유용한 쇼핑 정보를 받아보시겠습니까?");
        // 확인 버튼 리스너
        alert_confirm.setPositiveButton("동의", null);
        alert_confirm.setNegativeButton("취소", null);
        // 다이얼로그 생성
        AlertDialog alert = alert_confirm.create();

        // 아이콘
//        alert.setIcon(R.drawable.ic_launcher);
        // 다이얼로그 타이틀
        alert.setTitle("마케팅 수신동의 설정");
        // 다이얼로그 보기
        alert.show();

    }

    @Override
    public void onBackPressed(){
        if(mWebView.getOriginalUrl().equalsIgnoreCase("https://www.hotdealelf.com")){
            backPressCloseHandler.onBackPressed();
        }
        else if(mWebView.canGoBack()){
            mWebView.goBack();
        }else{
            backPressCloseHandler.onBackPressed();
        }
    }


    private class WebViewClientClass extends WebViewClient{

        public static final String INTENT_PROTOCOL_START = "intent:";
        public static final String INTENT_PROTOCOL_INTENT = "#Intent;";
        public static final String INTENT_PROTOCOL_END = ";end;";
        public static final String GOOGLE_PLAY_STORE_PREFIX = "market://details?id=";


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url){
            if(url.startsWith(INTENT_PROTOCOL_START)){
                final int customUrlStartIndex = INTENT_PROTOCOL_START.length();
                final int customUrlEndIndex = url.indexOf(INTENT_PROTOCOL_INTENT);
                if (customUrlEndIndex < 0) {
                    return false;
                } else {
                    final String customUrl = url.substring(customUrlStartIndex, customUrlEndIndex);
                    try {
                        Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(customUrl));
                        getBaseContext().startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    } catch (ActivityNotFoundException e) {
                        final int packageStartIndex = customUrlEndIndex + INTENT_PROTOCOL_INTENT.length();
                        final int packageEndIndex = url.indexOf(INTENT_PROTOCOL_END);

                        final String packageName = url.substring(packageStartIndex, packageEndIndex < 0 ? url.length() : packageEndIndex);

                        Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_PLAY_STORE_PREFIX + packageName));
                        getBaseContext().startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                    return true;
                }

            }else{
                return false;
            }
        }
    }


    public class BackPressCloseHandler{
        private long backKeyPressedTime = 0;
        private Toast toast;
        private Activity activity;

        public BackPressCloseHandler(Activity context) {
            this.activity = context;
        }

        public void onBackPressed() {
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis();
                showGuide();
                return;
            }
            if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                activity.finish();
                toast.cancel();
            }
        }

        public void showGuide() {
            toast = Toast.makeText(activity, "뒤로 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }




    private void setLayout(){
        mWebView = (WebView) findViewById(R.id.webview);
    }
}
