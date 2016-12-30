package com.fnplus.webview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

/**
 * Created by Abhish3k on 12/30/2016.
 */

public class MainActivity extends AppCompatActivity {

    private String postUrl = "https://www.xda-developers.com/";
    private WebView webView;
    private ProgressBar progressBar;
    private float m_downX;
    private ImageView imgHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        webView = (WebView) findViewById(R.id.webView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imgHeader = (ImageView) findViewById(R.id.backdrop);

        if (!TextUtils.isEmpty(getIntent().getStringExtra("postUrl"))) {
            postUrl = getIntent().getStringExtra("postUrl");
        }

        initWebView();
        initCollapsingToolbar();
        renderPost();


        // enable / disable javascript
        // webView.getSettings().setJavaScriptEnabled(true);

        // loading url into web view
        // webView.loadUrl("http://www.google.com");

        /**
         * loading custom html into webivew
         * */
        /*
        String customHtml = "<html><body><h1>Hello, WebView</h1> <h1>Heading 1</h1><h2>Heading 2</h2><h3>Heading 3</h3>" +
                "<p>This is a sample paragraph.</p></body></html>";
        webView.loadData(customHtml, "text/html", "UTF-8");
        */

        /**
         * Enabling zoom-in controls
         * */
        /*
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(true);
        */

        // Loading local html file into web view
        // webView.loadUrl("file:///android_asset/sample.html");

        /**
         * Loading custom fonts and css
         * */
        /*
        String style = "<style type='text/css'>@font-face { font-family: 'roboto'; src: url('Roboto-Light.ttf');}@font-face { font-family: 'roboto-medium'; src: url('Roboto-Medium.ttf'); }" +
                "body{color:#666;font-family: 'roboto';padding: 0.3em;}";
        style += "a{color:" + String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, R.color.colorPrimaryDark))) + "}</style>";
        String customHtml = "<h1>Hello, WebView</h1> <h1>Heading 1</h1><h2>Heading 2</h2><h3>Heading 3</h3>" +
                "<p>This is a sample paragraph.</p>";
        String content = "<html>" + style + "<body'>" + customHtml + "</body></Html>";
        webView.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "utf-8", null);
        */
    }

    private void initWebView() {
        webView.setWebChromeClient(new MyWebChromeClient(this));
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                /**
                 * Check for the url, if the url is from same domain
                 * open the url in the same activity as new intent
                 * else pass the url to browser activity
                 * */
                if (Utils.isSameDomain(postUrl, url)) {
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    intent.putExtra("postUrl", url);
                    startActivity(intent);
                } else {
                    // launch in-app browser i.e BrowserActivity
                    openInAppBrowser(url);
                }

                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }
        });
        webView.clearCache(true);
        webView.clearHistory();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getPointerCount() > 1) {
                    //Multi touch detected
                    return true;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        // save the x
                        m_downX = event.getX();
                    }
                    break;

                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP: {
                        // set x so that it doesn't move
                        event.setLocation(m_downX, event.getY());
                    }
                    break;

                }
                return false;
            }
        });
    }

    private void renderPost() {
        webView.loadUrl(postUrl);

        // webView.loadUrl("file:///android_asset/sample.html");
    }

    private void openInAppBrowser(String url) {
        Intent intent = new Intent(MainActivity.this, BrowserActivity.class);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    /**
     * Initializing collapsing toolbar
     * Will show and hide the toolbar txtPostTitle on scroll
     */
    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        // hiding & showing the txtPostTitle when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle("Web View");
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });

        // loading toolbar header image
    }

    private class MyWebChromeClient extends WebChromeClient {
        Context context;

        public MyWebChromeClient(Context context) {
            super();
            this.context = context;
        }


    }
}
