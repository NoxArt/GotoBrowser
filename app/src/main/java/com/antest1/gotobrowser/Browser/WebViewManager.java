package com.antest1.gotobrowser.Browser;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import androidx.webkit.ProxyConfig;
import androidx.webkit.ProxyController;
import androidx.webkit.WebViewFeature;

import com.antest1.gotobrowser.Activity.BrowserActivity;
import com.antest1.gotobrowser.BuildConfig;
import com.antest1.gotobrowser.Constants;
import com.antest1.gotobrowser.Helpers.KcUtils;
import com.antest1.gotobrowser.Proxy.LocalProxyServer;
import com.antest1.gotobrowser.R;
import com.antest1.gotobrowser.Helpers.VersionDatabase;
import com.antest1.gotobrowser.Subtitle.KcSubtitleUtils;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.antest1.gotobrowser.Constants.AUTOCOMPLETE_NIT;
import static com.antest1.gotobrowser.Constants.AUTOCOMPLETE_OOI;
import static com.antest1.gotobrowser.Constants.CONNECT_NITRABBIT;
import static com.antest1.gotobrowser.Constants.CONN_DMM;
import static com.antest1.gotobrowser.Constants.CONN_KANSU;
import static com.antest1.gotobrowser.Constants.CONN_NITRABBIT;
import static com.antest1.gotobrowser.Constants.CONN_OOI;
import static com.antest1.gotobrowser.Constants.DMM_COOKIE;
import static com.antest1.gotobrowser.Constants.KANCOLLE_SERVER_LIST;
import static com.antest1.gotobrowser.Constants.OOI_SERVER_LIST;
import static com.antest1.gotobrowser.Constants.PREF_ADJUSTMENT;
import static com.antest1.gotobrowser.Constants.PREF_CONNECTOR;
import static com.antest1.gotobrowser.Constants.PREF_DMM_ID;
import static com.antest1.gotobrowser.Constants.PREF_DMM_PASS;
import static com.antest1.gotobrowser.Constants.PREF_LATEST_URL;
import static com.antest1.gotobrowser.Constants.PREF_PADDING;
import static com.antest1.gotobrowser.Constants.PREF_VPADDING;
import static com.antest1.gotobrowser.Constants.REFRESH_DETECT_CALL;
import static com.antest1.gotobrowser.Constants.RESIZE_DMM;
import static com.antest1.gotobrowser.Constants.RESIZE_OOI_3;
import static com.antest1.gotobrowser.Constants.RESIZE_OSAPI;
import static com.antest1.gotobrowser.Constants.URL_DMM;
import static com.antest1.gotobrowser.Constants.URL_DMM_FOREIGN;
import static com.antest1.gotobrowser.Constants.URL_DMM_LOGIN;
import static com.antest1.gotobrowser.Constants.URL_KANSU;
import static com.antest1.gotobrowser.Constants.URL_NITRABBIT;
import static com.antest1.gotobrowser.Constants.URL_OOI;
import static com.antest1.gotobrowser.Constants.URL_OOI_3;
import static com.antest1.gotobrowser.Constants.URL_OSAPI;
import static com.antest1.gotobrowser.Constants.VERSION_TABLE_VERSION;
import static com.antest1.gotobrowser.Helpers.KcUtils.getStringFromException;

public class WebViewManager {
    public static final String OPEN_KANCOLLE = "open_kancolle";
    public static final String OPEN_RES_DOWN = "open_res_down";
    public static final String userAgent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0";

    public static boolean checkProxy() {
        return WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE);
    }

    public static boolean setProxy() {
        boolean enabled = WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE);
        if (enabled) {
            ProxyConfig proxyConfig = new ProxyConfig.Builder()
                    .addProxyRule(LocalProxyServer.getServerAddress())
                    .build();
            Executor executor = command -> { };
            Runnable runnable = () -> { };
            ProxyController.getInstance().setProxyOverride(proxyConfig, executor, runnable);
        }
        return enabled;
    }

    public static void clearProxy() {
        boolean enabled = WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE);
        if (enabled) {
            Executor executor = command -> { };
            Runnable runnable = () -> { };
            ProxyController.getInstance().clearProxyOverride(executor, runnable);
        }
    }

    public static void setHardwardAcceleratedFlag(BrowserActivity activity) {
        activity.getWindow().setFlags( WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
    }

    public static void setDataDirectorySuffix(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = KcUtils.getProcessName(context);
            if (!BuildConfig.APPLICATION_ID.equals(processName)) {
                WebView.setDataDirectorySuffix(processName);
            }
        }
    }

    public static void setGestureDetector(BrowserActivity activity, WebViewL webview) {
        View broswerPanel = activity.findViewById(R.id.browser_panel);
        GestureDetector mDetector = new GestureDetector(activity, new BrowserGestureListener(broswerPanel));
        webview.setOnTouchListener((v, event) -> mDetector.onTouchEvent(event));
    }

    @SuppressLint("SetJavaScriptEnabled")
    public static void setWebViewSettings(WebViewL view) {
        view.setInitialScale(1);
        view.getSettings().setLoadWithOverviewMode(true);
        view.getSettings().setSaveFormData(true);
        view.getSettings().setDatabaseEnabled(true);
        view.getSettings().setDomStorageEnabled(true);
        view.getSettings().setUseWideViewPort(true);
        view.getSettings().setJavaScriptEnabled(true);
        view.getSettings().setTextZoom(100);
        view.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        view.getSettings().setSupportMultipleWindows(true);
        // mContentView.getSettings().setBuiltInZoomControls(true);
        view.getSettings().setSupportZoom(false);
        view.getSettings().setUserAgentString(userAgent);
        view.setScrollbarFadingEnabled(true);
        view.getSettings().setAppCacheEnabled(false);
        view.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.getSettings().setOffscreenPreRaster(true);
        }
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        WebView.setWebContentsDebuggingEnabled(true);
    }

    public static void setWebViewClient(BrowserActivity activity, WebViewL webview, List<String> connector_info) {
        SharedPreferences sharedPref = activity.getSharedPreferences(
                activity.getString(R.string.preference_key), Context.MODE_PRIVATE);
        Context context = activity.getApplicationContext();
        boolean is_kcbrowser_mode = activity.isKcMode();
        ResourceProcess res_process = new ResourceProcess(activity);

        webview.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                if (is_kcbrowser_mode) {
                    sharedPref.edit().putString(PREF_LATEST_URL, url).apply();
                    WebViewManager.runLoginScript(activity, webview, url);
                    if (url.contains(Constants.URL_OSAPI) || url.contains(Constants.URL_OOI_3) || url.contains(URL_DMM)) {
                        activity.setStartedFlag();
                        runLayoutAdjustmentScript(activity, webview, url, connector_info);
                    }
                }
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                Uri source = Uri.parse(url);
                if (is_kcbrowser_mode) {
                    WebResourceResponse response = res_process.processWebRequest(source);
                    if (response != null) return response;
                }
                return super.shouldInterceptRequest(view, url);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Uri source = request.getUrl();
                    if (is_kcbrowser_mode) {
                        WebResourceResponse response = res_process.processWebRequest(source);
                        if (response != null) return response;
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }
        });
    }

    public static void setWebViewDownloader(BrowserActivity activity, WebViewL webview) {
        ProgressDialog downloadDialog = activity.getDownloadDialog();
        webview.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            Uri uri = Uri.parse(url);
            String filename = uri.getLastPathSegment();
            Log.e("GOTO", url);
            WebViewManager.setDownloadProgressDialog(downloadDialog);
            WebViewManager.setProgressDialogMessage(downloadDialog,
                    String.format(Locale.US, "Downloading %s...", filename));
            downloadDialog.show();
            getResourceDownloadThread(activity, url, userAgent, mimetype).start();
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private static void setPopupWebViewSetting(WebViewL webview) {
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setSaveFormData(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setUseWideViewPort(true);
        webview.getSettings().setSupportZoom(true);
    }

    public static void setDownloadProgressDialog(ProgressDialog progress) {
        progress.setCancelable(false);
        progress.setProgressNumberFormat(null);
        progress.setProgressPercentFormat(null);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(true);
    }

    public static void setProgressDialogMessage(ProgressDialog progress, String message) {
        progress.setMessage(message);
    }

    public static void enableBrowserCookie(WebViewL webview) {
        if (Build.VERSION.SDK_INT >= 21) {
            webview.getSettings().setMixedContentMode(WebSettings
                    .MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager.getInstance().setAcceptThirdPartyCookies(webview, true);
        } else {
            CookieManager.getInstance().setAcceptCookie(true);
        }
    }


    public static void setPopupView(BrowserActivity activity, WebViewL webview) {
        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                KcUtils.showToast(activity.getApplicationContext(), message);
                return super.onJsAlert(view, url, message, result);
            }

            @SuppressLint("SetJavaScriptEnabled")
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                final WebViewL popupWebView = new WebViewL(activity);
                ImageView closeButton = activity.findViewById(R.id.dmm_browser_close);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    CookieManager.getInstance().setAcceptThirdPartyCookies(popupWebView, true);
                }
                closeButton.setVisibility(View.VISIBLE);

                setPopupWebViewSetting(popupWebView);
                popupWebView.setWebViewClient(new WebViewClient() {
                    @TargetApi(Build.VERSION_CODES.N)
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                        String url = request.getUrl().toString();
                        view.loadUrl(url);
                        return true;
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return true;
                    }
                });

                closeButton.setOnClickListener(v -> {
                    view.removeView(popupWebView);
                    v.setVisibility(View.GONE);
                });
                view.addView(popupWebView);
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(popupWebView);
                resultMsg.sendToTarget();
                return true;
            }

            @Override
            public void onCloseWindow(WebView window) {
                super.onCloseWindow(window);
            }
        });
    }

    public static String getResourceDownloadOutputPath(Context context, Uri uri) {
        String outputpath = "";
        if (uri.getPath().contains(context.getString(R.string.resource_download_prefix))) {
            outputpath = uri.getPath().replace(context.getString(R.string.resource_download_prefix), "")
                    .replace(".zip", "/");
        } else if (uri.getPath().contains("kcs2-all") || uri.getPath().contains("kcs2-event")) {
            outputpath = "/";
        }
        return outputpath;
    }

    public static void setSoundMuteCookie(WebViewL webview) {
        CookieSyncManager syncManager = CookieSyncManager.createInstance(webview.getContext());
        syncManager.sync();
        CookieManager cookieManager = CookieManager.getInstance();
        for (String server: KANCOLLE_SERVER_LIST) {
            String url = "http://".concat(server).concat("/kcs2/");
            cookieManager.setCookie(url, "vol_bgm=0;");
            cookieManager.setCookie(url, "vol_se=0;");
            cookieManager.setCookie(url, "vol_voice=0;");
        }
        for (String server: OOI_SERVER_LIST) {
            String url = "http://".concat(server).concat("/kcs2/");
            cookieManager.setCookie(url, "vol_bgm=0;");
            cookieManager.setCookie(url, "vol_se=0;");
            cookieManager.setCookie(url, "vol_voice=0;");
        }
    }

    public static void runLoginScript(BrowserActivity activity, WebViewL webview, String url) {
        SharedPreferences sharedPref = activity.getSharedPreferences(
                activity.getString(R.string.preference_key), Context.MODE_PRIVATE);
        String login_id = sharedPref.getString(PREF_DMM_ID, ""); // intent.getStringExtra("login_id");
        String login_password = sharedPref.getString(PREF_DMM_PASS, "");

        if (url.contains(URL_DMM_FOREIGN)) {
            webview.evaluateJavascript(DMM_COOKIE, null);
            webview.evaluateJavascript("location.href='".concat(URL_DMM).concat("';"), null);
        }
        if (url.contains(URL_DMM_LOGIN) || url.equals(URL_KANSU) || url.equals(URL_OOI)) {
            webview.evaluateJavascript(
                    String.format(Locale.US, AUTOCOMPLETE_OOI,
                            login_id, login_password), null);
            if (url.contains(URL_DMM_LOGIN)) {
                webview.evaluateJavascript(DMM_COOKIE, null);
            }
        }
        if (url.equals(Constants.URL_NITRABBIT)) {
            webview.evaluateJavascript(CONNECT_NITRABBIT, null);
            webview.evaluateJavascript(
                    String.format(Locale.US, AUTOCOMPLETE_NIT,
                            login_id, login_password), null);
        }
    }

    public static void runLayoutAdjustmentScript(BrowserActivity activity, WebViewL webview, String url, List<String> connector_info) {
        SharedPreferences sharedPref = activity.getSharedPreferences(
                activity.getString(R.string.preference_key), Context.MODE_PRIVATE);
        DisplayMetrics dimension = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dimension);
        int width = dimension.widthPixels;
        int height = dimension.heightPixels;
        int adjust_padding = sharedPref.getInt(PREF_PADDING, getDefaultPadding(width, height));
        int adjust_vpadding = sharedPref.getInt(PREF_VPADDING, 0);
        boolean adjust_layout = sharedPref.getBoolean(PREF_ADJUSTMENT, false);
        if (adjust_layout) {
            if (url.contains(URL_OSAPI)) webview.evaluateJavascript(String.format(
                    Locale.US, RESIZE_OSAPI, adjust_padding, adjust_vpadding), null);
            else if (url.contains(URL_OOI_3)) webview.evaluateJavascript(String.format(
                    Locale.US, RESIZE_OOI_3, adjust_padding, adjust_vpadding), null);
            else if (url.contains(URL_DMM)) webview.evaluateJavascript(String.format(
                    Locale.US, RESIZE_DMM, adjust_padding, adjust_vpadding), null);
        }
        if (url.contains(URL_OSAPI) || url.contains(URL_OOI_3)) {
            webview.evaluateJavascript(String.format(Locale.US,
                    REFRESH_DETECT_CALL, connector_info.get(0)), value -> {
                if (value.equals("true")) {
                    sharedPref.edit().putString(PREF_LATEST_URL, connector_info.get(0)).apply();
                    openPage(activity, webview, connector_info, true);
                }
            });
        }
    }

    public static void openPage(BrowserActivity activity, WebViewL webview, List<String> connector_info, boolean isKcBrowser) {
        SharedPreferences sharedPref = activity.getSharedPreferences(
                activity.getString(R.string.preference_key), Context.MODE_PRIVATE);
        String login_id = sharedPref.getString(PREF_DMM_ID, ""); // intent.getStringExtra("login_id");
        String login_password = sharedPref.getString(PREF_DMM_PASS, "");
        if (connector_info == null || connector_info.size() != 2) return;

        String connector_url_default = connector_info.get(0);
        String connector_url = connector_info.get(1);

        webview.resumeTimers();
        webview.getSettings().setTextZoom(100);
        if (!isKcBrowser) {
            webview.loadUrl(connector_url);
        } else {
            String pref_connector = sharedPref.getString(PREF_CONNECTOR, null);
            if (CONN_KANSU.equals(pref_connector) || CONN_OOI.equals(pref_connector)) {
                String postdata = "";
                try {
                    int connect_mode = connector_url_default.equals(URL_OOI) ? 3 : 4;
                    postdata = String.format(Locale.US, "login_id=%s&password=%s&mode=%d",
                            URLEncoder.encode(login_id, "utf-8"),
                            URLEncoder.encode(login_password, "utf-8"),
                            connect_mode);
                    webview.postUrl(connector_url, postdata.getBytes());
                } catch (UnsupportedEncodingException e) {
                    KcUtils.reportException(e);
                }
            } else {
                webview.loadUrl(connector_url_default);
            }
        }
    }

    public static List<String> getDefaultPage(BrowserActivity activity, boolean isKcBrowser) {
        List<String> url_list = new ArrayList<>();
        SharedPreferences sharedPref = activity.getSharedPreferences(
                activity.getString(R.string.preference_key), Context.MODE_PRIVATE);
        if (!isKcBrowser) {
            String download_url = activity.getString(R.string.resource_download_link);
            url_list.add(download_url);
            url_list.add(download_url);
        } else {
            String pref_connector = sharedPref.getString(PREF_CONNECTOR, null);
            if (CONN_DMM.equals(pref_connector)) {
                url_list.add(URL_DMM);
                url_list.add(URL_DMM);
            } else if (CONN_KANSU.equals(pref_connector) || CONN_OOI.equals(pref_connector)) {
                String connector_url = "";
                String connector_url_default = "";
                if (CONN_KANSU.equals(pref_connector)) {
                    connector_url_default = URL_KANSU;
                    connector_url = URL_KANSU;
                } else {
                    connector_url_default = URL_OOI;
                    connector_url = sharedPref.getString(PREF_LATEST_URL, URL_OOI);
                }
                url_list.add(connector_url_default);
                url_list.add(connector_url);
            } else if (CONN_NITRABBIT.equals(pref_connector)) {
                url_list.add(URL_NITRABBIT);
                url_list.add(sharedPref.getString(PREF_LATEST_URL, URL_NITRABBIT));
            }
        }
        return url_list;
    }


    public static Thread getResourceDownloadThread(BrowserActivity activity, String url, String userAgent, String mimetype) {
        Context context = activity.getApplicationContext();
        ProgressDialog downloadDialog = activity.getDownloadDialog();
        
        Uri uri = Uri.parse(url);
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        VersionDatabase versionTable = new VersionDatabase(context, null, VERSION_TABLE_VERSION);

        return new Thread() {
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = KcUtils.getDownloadRequest(url, userAgent, mimetype);

                String message = "";
                String filename = uri.getLastPathSegment();
                String outputpath = WebViewManager.getResourceDownloadOutputPath(context, uri);
                String version = uri.getQueryParameter("v");
                if (version == null) version = "";

                Log.e("GOTO", outputpath);
                Log.e("GOTO", "version: " + version);

                try {
                    Response response = client.newCall(request).execute();
                    InputStream in = response.body().byteStream();
                    activity.runOnUiThread(() -> WebViewManager.setProgressDialogMessage(downloadDialog,
                            String.format(Locale.US, "Extracting %s...", filename)));
                    KcUtils.unzipResource(context, in, outputpath, versionTable, version);
                    message = String.format(Locale.US, "Process finished: %s", filename);
                } catch (NullPointerException | IOException e) {
                    KcUtils.reportException(e);
                    message = getStringFromException(e);
                } finally {
                    String finish_message = message;
                    activity.runOnUiThread(() -> {
                        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        if (!activity.isFinishing() && downloadDialog != null && downloadDialog.isShowing()) {
                            downloadDialog.dismiss();
                        }
                        KcUtils.showToast(context, finish_message);
                    });
                    versionTable.close();
                }
            }
        };
    }

    public static int getDefaultPadding(int width, int height) {
        if (width < height) {
            int temp = width; width = height; height = temp;
        }
        int ratio_val = width  * 18 / height;
        return (ratio_val - 30) * 20;
    }

}
