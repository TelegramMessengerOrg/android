package android.support.v4.net;

import android.annotation.TargetApi;
import android.net.ConnectivityManager;
import android.support.annotation.RequiresApi;

@RequiresApi(16)
@TargetApi(16)
class ConnectivityManagerCompatJellyBean {
    ConnectivityManagerCompatJellyBean() {
    }

    public static boolean isActiveNetworkMetered(ConnectivityManager cm) {
        return cm.isActiveNetworkMetered();
    }
}
