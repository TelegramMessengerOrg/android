package net.hockeyapp.android.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Locale;
import net.hockeyapp.android.Constants;
import net.hockeyapp.android.Tracking;
import net.hockeyapp.android.UpdateManagerListener;
import net.hockeyapp.android.utils.HockeyLog;
import net.hockeyapp.android.utils.Util;
import net.hockeyapp.android.utils.VersionCache;
import net.hockeyapp.android.utils.VersionHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.exoplayer2.C;

public class CheckUpdateTask extends AsyncTask<Void, String, JSONArray> {
    protected String appIdentifier = null;
    private Context context = null;
    protected UpdateManagerListener listener;
    protected Boolean mandatory = Boolean.valueOf(false);
    protected String urlString = null;
    private long usageTime = 0;

    public CheckUpdateTask(WeakReference<? extends Context> weakContext, String urlString, String appIdentifier, UpdateManagerListener listener) {
        this.appIdentifier = appIdentifier;
        this.urlString = urlString;
        this.listener = listener;
        Context ctx = null;
        if (weakContext != null) {
            ctx = (Context) weakContext.get();
        }
        if (ctx != null) {
            this.context = ctx.getApplicationContext();
            this.usageTime = Tracking.getUsageTime(ctx);
            Constants.loadFromContext(ctx);
        }
    }

    public void attach(WeakReference<? extends Context> weakContext) {
        Context ctx = null;
        if (weakContext != null) {
            ctx = (Context) weakContext.get();
        }
        if (ctx != null) {
            this.context = ctx.getApplicationContext();
            Constants.loadFromContext(ctx);
        }
    }

    public void detach() {
        this.context = null;
    }

    protected int getVersionCode() {
        return Integer.parseInt(Constants.APP_VERSION);
    }

    protected JSONArray doInBackground(Void... args) {
        Exception e;
        try {
            int versionCode = getVersionCode();
            JSONArray json = new JSONArray(VersionCache.getVersionInfo(this.context));
            if (getCachingEnabled() && findNewVersion(json, versionCode)) {
                HockeyLog.verbose("HockeyUpdate", "Returning cached JSON");
                return json;
            }
            URLConnection connection = createConnection(new URL(getURLString("json")));
            connection.connect();
            InputStream inputStream = new BufferedInputStream(connection.getInputStream());
            String jsonString = convertStreamToString(inputStream);
            inputStream.close();
            json = new JSONArray(jsonString);
            if (findNewVersion(json, versionCode)) {
                return limitResponseSize(json);
            }
            return null;
        } catch (IOException e2) {
            e = e2;
            if (this.context != null && Util.isConnectedToNetwork(this.context)) {
                HockeyLog.error("HockeyUpdate", "Could not fetch updates although connected to internet");
                e.printStackTrace();
            }
            return null;
        } catch (JSONException e3) {
            e = e3;
            HockeyLog.error("HockeyUpdate", "Could not fetch updates although connected to internet");
            e.printStackTrace();
            return null;
        }
    }

    protected URLConnection createConnection(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.addRequestProperty("User-Agent", "HockeySDK/Android 4.1.3");
        if (VERSION.SDK_INT <= 9) {
            connection.setRequestProperty("connection", "close");
        }
        return connection;
    }

    private boolean findNewVersion(JSONArray json, int versionCode) {
        boolean newerVersionFound = false;
        int index = 0;
        while (index < json.length()) {
            try {
                boolean largerVersionCode;
                JSONObject entry = json.getJSONObject(index);
                if (entry.getInt("version") > versionCode) {
                    largerVersionCode = true;
                } else {
                    largerVersionCode = false;
                }
                boolean newerApkFile;
                if (entry.getInt("version") == versionCode && VersionHelper.isNewerThanLastUpdateTime(this.context, entry.getLong("timestamp"))) {
                    newerApkFile = true;
                } else {
                    newerApkFile = false;
                }
                boolean minRequirementsMet;
                if (VersionHelper.compareVersionStrings(entry.getString("minimum_os_version"), VersionHelper.mapGoogleVersion(VERSION.RELEASE)) <= 0) {
                    minRequirementsMet = true;
                } else {
                    minRequirementsMet = false;
                }
                if ((largerVersionCode || newerApkFile) && minRequirementsMet) {
                    if (entry.has("mandatory")) {
                        this.mandatory = Boolean.valueOf(this.mandatory.booleanValue() | entry.getBoolean("mandatory"));
                    }
                    newerVersionFound = true;
                }
                index++;
            } catch (JSONException e) {
                return false;
            }
        }
        return newerVersionFound;
    }

    private JSONArray limitResponseSize(JSONArray json) {
        JSONArray result = new JSONArray();
        for (int index = 0; index < Math.min(json.length(), 25); index++) {
            try {
                result.put(json.get(index));
            } catch (JSONException e) {
            }
        }
        return result;
    }

    protected void onPostExecute(JSONArray updateInfo) {
        if (updateInfo != null) {
            HockeyLog.verbose("HockeyUpdate", "Received Update Info");
            if (this.listener != null) {
                this.listener.onUpdateAvailable(updateInfo, getURLString("apk"));
                return;
            }
            return;
        }
        HockeyLog.verbose("HockeyUpdate", "No Update Info available");
        if (this.listener != null) {
            this.listener.onNoUpdateAvailable();
        }
    }

    protected void cleanUp() {
        this.urlString = null;
        this.appIdentifier = null;
    }

    protected String getURLString(String format) {
        StringBuilder builder = new StringBuilder();
        builder.append(this.urlString);
        builder.append("api/2/apps/");
        builder.append(this.appIdentifier != null ? this.appIdentifier : this.context.getPackageName());
        builder.append("?format=" + format);
        if (!TextUtils.isEmpty(Secure.getString(this.context.getContentResolver(), "android_id"))) {
            builder.append("&udid=" + encodeParam(Secure.getString(this.context.getContentResolver(), "android_id")));
        }
        SharedPreferences prefs = this.context.getSharedPreferences("net.hockeyapp.android.login", 0);
        String auid = prefs.getString("auid", null);
        if (!TextUtils.isEmpty(auid)) {
            builder.append("&auid=" + encodeParam(auid));
        }
        String iuid = prefs.getString("iuid", null);
        if (!TextUtils.isEmpty(iuid)) {
            builder.append("&iuid=" + encodeParam(iuid));
        }
        builder.append("&os=Android");
        builder.append("&os_version=" + encodeParam(Constants.ANDROID_VERSION));
        builder.append("&device=" + encodeParam(Constants.PHONE_MODEL));
        builder.append("&oem=" + encodeParam(Constants.PHONE_MANUFACTURER));
        builder.append("&app_version=" + encodeParam(Constants.APP_VERSION));
        builder.append("&sdk=" + encodeParam("HockeySDK"));
        builder.append("&sdk_version=" + encodeParam("4.1.3"));
        builder.append("&lang=" + encodeParam(Locale.getDefault().getLanguage()));
        builder.append("&usage_time=" + this.usageTime);
        return builder.toString();
    }

    private String encodeParam(String param) {
        try {
            return URLEncoder.encode(param, C.UTF8_NAME);
        } catch (UnsupportedEncodingException e) {
            return TtmlNode.ANONYMOUS_REGION_ID;
        }
    }

    protected boolean getCachingEnabled() {
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static java.lang.String convertStreamToString(java.io.InputStream r6) {
        /*
        r2 = new java.io.BufferedReader;
        r4 = new java.io.InputStreamReader;
        r4.<init>(r6);
        r5 = 1024; // 0x400 float:1.435E-42 double:5.06E-321;
        r2.<init>(r4, r5);
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r1 = 0;
    L_0x0012:
        r1 = r2.readLine();	 Catch:{ IOException -> 0x0030 }
        if (r1 == 0) goto L_0x003c;
    L_0x0018:
        r4 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x0030 }
        r4.<init>();	 Catch:{ IOException -> 0x0030 }
        r4 = r4.append(r1);	 Catch:{ IOException -> 0x0030 }
        r5 = "\n";
        r4 = r4.append(r5);	 Catch:{ IOException -> 0x0030 }
        r4 = r4.toString();	 Catch:{ IOException -> 0x0030 }
        r3.append(r4);	 Catch:{ IOException -> 0x0030 }
        goto L_0x0012;
    L_0x0030:
        r0 = move-exception;
        r0.printStackTrace();	 Catch:{ all -> 0x004a }
        r6.close();	 Catch:{ IOException -> 0x0045 }
    L_0x0037:
        r4 = r3.toString();
        return r4;
    L_0x003c:
        r6.close();	 Catch:{ IOException -> 0x0040 }
        goto L_0x0037;
    L_0x0040:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x0037;
    L_0x0045:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x0037;
    L_0x004a:
        r4 = move-exception;
        r6.close();	 Catch:{ IOException -> 0x004f }
    L_0x004e:
        throw r4;
    L_0x004f:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x004e;
        */
        throw new UnsupportedOperationException("Method not decompiled: net.hockeyapp.android.tasks.CheckUpdateTask.convertStreamToString(java.io.InputStream):java.lang.String");
    }
}