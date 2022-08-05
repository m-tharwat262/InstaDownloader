package com.mtma.insta.downloader.activities;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.UrlQuerySanitizer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mtma.insta.downloader.Constants;
import com.mtma.insta.downloader.R;

public class InstagramOfficialLoginActivity extends AppCompatActivity {

    private static final String LOG_TAG = InstagramOfficialLoginActivity.class.getSimpleName();
    private SharedPreferences mSharedPreference;
    private SharedPreferences.Editor mSharedPreferenceEditor;

    private WebView mWebView;

    private final String mURL = "https://www.instagram.com/accounts/login/";

    private String mUserId;
    private String mSessionId;
    private String mCookies;
    private String mCsrfToken;
    private Boolean isLoggedIn = false;
    private String redirect_url = "https://instagram.com/";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_instagram_official);

        initUI();

    }

    public void initUI() {

        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPreferenceEditor = mSharedPreference.edit();

        mWebView = findViewById(R.id.activity_instagram_official_web_view);

        mWebView.clearHistory();
        mWebView.clearFormData();
        mWebView.clearCache(true);
        mWebView.getSettings().setJavaScriptEnabled(true);

        startWebView();

    }


    private void startWebView() {

        mWebView.setWebViewClient(new WebViewClient() {


            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.e("InstagramLogin", "shouldOverrideUrlLoading: "+  url);
                if(url.equalsIgnoreCase(redirect_url)){
                    try {
                        view.loadUrl(url);
                        UrlQuerySanitizer.ValueSanitizer sanitizer = UrlQuerySanitizer.getAllButNulLegal();
                        sanitizer.sanitize(url);
                    }catch (Exception e){
                        Log.e("WebViewClient", "shouldOverrideUrlLoading: " + e );
                    }
                    return  true;
                }else {
                    return false;
                }


            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                CookieManager.getInstance().removeAllCookies(null);


            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (isLoggedIn) {

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            finish();

                        }

                    }, 1500);

                }

            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);

                mCookies = CookieManager.getInstance().getCookie(url);

                Log.i("Unix", "the url is :  " + url);
                Log.i("Unix", "the cookie :  " + mCookies);


                try {

                    mUserId = getFromCookie(url, "ds_user_id");
                    mSessionId = getFromCookie(url, "sessionid");
                    mCsrfToken = getFromCookie(url, "csrftoken");

                    if (mSessionId != null && mCsrfToken != null && mUserId != null) {

                        isLoggedIn = true;

                        mSharedPreferenceEditor.putBoolean(Constants.KEY_LOGIN, true);
                        mSharedPreferenceEditor.putString(Constants.KEY_USER_ID, mUserId);
                        mSharedPreferenceEditor.putString(Constants.KEY_SESSION_ID, mSessionId);
                        mSharedPreferenceEditor.putString(Constants.KEY_COOKIE, mCookies);
                        mSharedPreferenceEditor.putString(Constants.KEY_CSRF_TOKEN, mCsrfToken);
                        mSharedPreferenceEditor.apply();

                    }

                } catch (Exception e){
                    Log.e(LOG_TAG, "Exception from try-catch block inside " + LOG_TAG +" class in getUserData method.", e);
                }


            }

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.i(LOG_TAG, "error toast message in onReceivedError  :  " + description);
                Toast.makeText(InstagramOfficialLoginActivity.this, "error toast message in onReceivedError  :  \" + description", Toast.LENGTH_SHORT).show();
            }

            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }


        });

        mWebView.loadUrl(mURL);


    }

    public String getFromCookie(String siteName, String CookieName) {

        String CookieValue = null;

        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(siteName);
        if(cookies!=null && !cookies.isEmpty()) {
            String[] temp = cookies.split(";");
            for (String ar1 : temp) {
                if (ar1.contains(CookieName)) {
                    String[] temp1 = ar1.split("=");
                    CookieValue = temp1[1];
                    break;
                }
            }
        }
        return CookieValue;
    }

}
