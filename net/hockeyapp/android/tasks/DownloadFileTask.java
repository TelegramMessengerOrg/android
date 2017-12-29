package net.hockeyapp.android.tasks;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.StrictMode;
import android.os.StrictMode.VmPolicy;
import android.os.StrictMode.VmPolicy.Builder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;
import net.hockeyapp.android.R;
import net.hockeyapp.android.listeners.DownloadFileListener;
import org.telegram.messenger.exoplayer2.util.MimeTypes;

public class DownloadFileTask extends AsyncTask<Void, Integer, Long> {
    protected Context mContext;
    private String mDownloadErrorMessage;
    protected String mFilePath = (Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download");
    protected String mFilename = (UUID.randomUUID() + ".apk");
    protected DownloadFileListener mNotifier;
    protected ProgressDialog mProgressDialog;
    protected String mUrlString;

    public DownloadFileTask(Context context, String urlString, DownloadFileListener notifier) {
        this.mContext = context;
        this.mUrlString = urlString;
        this.mNotifier = notifier;
        this.mDownloadErrorMessage = null;
    }

    public void attach(Context context) {
        this.mContext = context;
    }

    public void detach() {
        this.mContext = null;
        this.mProgressDialog = null;
    }

    protected Long doInBackground(Void... args) {
        OutputStream output;
        IOException e;
        Long valueOf;
        Throwable th;
        InputStream input = null;
        OutputStream output2 = null;
        URLConnection connection = createConnection(new URL(getURLString()), 6);
        connection.connect();
        int lengthOfFile = connection.getContentLength();
        String contentType = connection.getContentType();
        if (contentType == null || !contentType.contains(MimeTypes.BASE_TYPE_TEXT)) {
            try {
                File dir = new File(this.mFilePath);
                if (dir.mkdirs() || dir.exists()) {
                    File file = new File(dir, this.mFilename);
                    InputStream input2 = new BufferedInputStream(connection.getInputStream());
                    try {
                        output = new FileOutputStream(file);
                    } catch (IOException e2) {
                        e = e2;
                        input = input2;
                        try {
                            e.printStackTrace();
                            valueOf = Long.valueOf(0);
                            if (output2 != null) {
                                try {
                                    output2.close();
                                } catch (IOException e3) {
                                    e3.printStackTrace();
                                }
                            }
                            if (input != null) {
                                input.close();
                            }
                            return valueOf;
                        } catch (Throwable th2) {
                            th = th2;
                            if (output2 != null) {
                                try {
                                    output2.close();
                                } catch (IOException e32) {
                                    e32.printStackTrace();
                                    throw th;
                                }
                            }
                            if (input != null) {
                                input.close();
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        input = input2;
                        if (output2 != null) {
                            output2.close();
                        }
                        if (input != null) {
                            input.close();
                        }
                        throw th;
                    }
                    try {
                        byte[] data = new byte[1024];
                        long total = 0;
                        while (true) {
                            int count = input2.read(data);
                            if (count == -1) {
                                break;
                            }
                            total += (long) count;
                            publishProgress(new Integer[]{Integer.valueOf(Math.round((((float) total) * 100.0f) / ((float) lengthOfFile)))});
                            output.write(data, 0, count);
                        }
                        output.flush();
                        valueOf = Long.valueOf(total);
                        if (output != null) {
                            try {
                                output.close();
                            } catch (IOException e322) {
                                e322.printStackTrace();
                            }
                        }
                        if (input2 != null) {
                            input2.close();
                        }
                        output2 = output;
                        input = input2;
                    } catch (IOException e4) {
                        e322 = e4;
                        output2 = output;
                        input = input2;
                        e322.printStackTrace();
                        valueOf = Long.valueOf(0);
                        if (output2 != null) {
                            output2.close();
                        }
                        if (input != null) {
                            input.close();
                        }
                        return valueOf;
                    } catch (Throwable th4) {
                        th = th4;
                        output2 = output;
                        input = input2;
                        if (output2 != null) {
                            output2.close();
                        }
                        if (input != null) {
                            input.close();
                        }
                        throw th;
                    }
                    return valueOf;
                }
                throw new IOException("Could not create the dir(s):" + dir.getAbsolutePath());
            } catch (IOException e5) {
                e322 = e5;
                e322.printStackTrace();
                valueOf = Long.valueOf(0);
                if (output2 != null) {
                    output2.close();
                }
                if (input != null) {
                    input.close();
                }
                return valueOf;
            }
        }
        this.mDownloadErrorMessage = "The requested download does not appear to be a file.";
        valueOf = Long.valueOf(0);
        if (output2 != null) {
            try {
                output2.close();
            } catch (IOException e3222) {
                e3222.printStackTrace();
            }
        }
        if (input != null) {
            input.close();
        }
        return valueOf;
    }

    protected void setConnectionProperties(HttpURLConnection connection) {
        connection.addRequestProperty("User-Agent", "HockeySDK/Android 4.1.3");
        connection.setInstanceFollowRedirects(true);
        if (VERSION.SDK_INT <= 9) {
            connection.setRequestProperty("connection", "close");
        }
    }

    protected URLConnection createConnection(URL url, int remainingRedirects) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        setConnectionProperties(connection);
        int code = connection.getResponseCode();
        if ((code != 301 && code != 302 && code != 303) || remainingRedirects == 0) {
            return connection;
        }
        URL movedUrl = new URL(connection.getHeaderField("Location"));
        if (url.getProtocol().equals(movedUrl.getProtocol())) {
            return connection;
        }
        connection.disconnect();
        return createConnection(movedUrl, remainingRedirects - 1);
    }

    protected void onProgressUpdate(Integer... args) {
        try {
            if (this.mProgressDialog == null) {
                this.mProgressDialog = new ProgressDialog(this.mContext);
                this.mProgressDialog.setProgressStyle(1);
                this.mProgressDialog.setMessage("Loading...");
                this.mProgressDialog.setCancelable(false);
                this.mProgressDialog.show();
            }
            this.mProgressDialog.setProgress(args[0].intValue());
        } catch (Exception e) {
        }
    }

    protected void onPostExecute(Long result) {
        if (this.mProgressDialog != null) {
            try {
                this.mProgressDialog.dismiss();
            } catch (Exception e) {
            }
        }
        if (result.longValue() > 0) {
            this.mNotifier.downloadSuccessful(this);
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setDataAndType(Uri.fromFile(new File(this.mFilePath, this.mFilename)), "application/vnd.android.package-archive");
            intent.setFlags(268435456);
            VmPolicy oldVmPolicy = null;
            if (VERSION.SDK_INT >= 24) {
                oldVmPolicy = StrictMode.getVmPolicy();
                StrictMode.setVmPolicy(new Builder().penaltyLog().build());
            }
            this.mContext.startActivity(intent);
            if (oldVmPolicy != null) {
                StrictMode.setVmPolicy(oldVmPolicy);
                return;
            }
            return;
        }
        try {
            String message;
            AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
            builder.setTitle(R.string.hockeyapp_download_failed_dialog_title);
            if (this.mDownloadErrorMessage == null) {
                message = this.mContext.getString(R.string.hockeyapp_download_failed_dialog_message);
            } else {
                message = this.mDownloadErrorMessage;
            }
            builder.setMessage(message);
            builder.setNegativeButton(R.string.hockeyapp_download_failed_dialog_negative_button, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    DownloadFileTask.this.mNotifier.downloadFailed(DownloadFileTask.this, Boolean.valueOf(false));
                }
            });
            builder.setPositiveButton(R.string.hockeyapp_download_failed_dialog_positive_button, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    DownloadFileTask.this.mNotifier.downloadFailed(DownloadFileTask.this, Boolean.valueOf(true));
                }
            });
            builder.create().show();
        } catch (Exception e2) {
        }
    }

    protected String getURLString() {
        return this.mUrlString + "&type=apk";
    }
}