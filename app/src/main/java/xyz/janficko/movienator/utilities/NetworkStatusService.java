package xyz.janficko.movienator.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkStatusService {

    private Context context;

    public NetworkStatusService(Context context) {
        this.context = context;
    }

    /**
     * Checks if connectivity exists or is in the process of being connected. If you want to
     * read or write data use isConnected() instead.
     *
     * @return boolean
     */
    public boolean isOnline() {
        NetworkInfo networkInfo = getNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    /**
     * Indicates if the network if fully usable.
     *
     * @return boolean
     */
    public boolean isConnected() {
        NetworkInfo networkInfo = getNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public NetworkInfo getNetworkInfo() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }
}
