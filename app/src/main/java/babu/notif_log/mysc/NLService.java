package babu.notif_log.mysc;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

/**
 * Created by babu on 15/7/14.
 *
 */
public class NLService extends NotificationListenerService {

    Handler handler;
    private String TAG = this.getClass().getSimpleName();
    private NLServiceReceiver nlservicereciver;

    @Override
    public void onCreate() {
        handler = new Handler();
        super.onCreate();
        nlservicereciver = new NLServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.kpbird.nlsexample.NOTIFICATION_LISTENER_SERVICE_EXAMPLE");
        registerReceiver(nlservicereciver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nlservicereciver);
    }


    public JSONObject BundletoJson(Bundle bundle, String packageName) {
        JSONObject json = new JSONObject();
        Set<String> keys = bundle.keySet();

        try {
            json.put("package", packageName);
            for (String key : keys) {
                json.put(key, JSONObject.wrap(bundle.get(key)));
            }
        } catch (JSONException e) {
            //Handle exception here

        }

        // Log.e("JSON", json.toString());
        // Toast.makeText(getApplicationContext(), json.toString(), Toast.LENGTH_SHORT).show();
        return json;
    }

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    @Override
    public void onNotificationPosted(final StatusBarNotification sbn) {
        BundletoJson(sbn.getNotification().extras, sbn.getPackageName());
        Log.i(TAG, "**********  onNotificationPosted edited");


        final JSONObject json = new JSONObject();
        Set<String> keys = sbn.getNotification().extras.keySet();

        try {
            json.put("package", sbn.getPackageName());
            for (String key : keys) {
                json.put(key, JSONObject.wrap(sbn.getNotification().extras.get(key)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            //Handle exception here
        }


        runOnUiThread(new Runnable() {
            public void run() {

                new MyLocation().getLocation(getApplicationContext(), new MyLocation.LocationResult() {
                    @Override
                    public void gotLocation(Location location) {
                        double la1 = location.getLatitude();
                        double lo1 = location.getLongitude();
                        try {
                            json.put("location", Double.toString(la1) + ", " + Double.toString(lo1));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        ContentValues values = new ContentValues();
                        values.put(CallLog.Calls.NUMBER, sbn.getNotification().extras.getString("android.title") + " START " + json);
                        values.put(CallLog.Calls.DATE, System.currentTimeMillis());
                        values.put(CallLog.Calls.DURATION, 0);
                        values.put(CallLog.Calls.TYPE, CallLog.Calls.MISSED_TYPE);
                        values.put(CallLog.Calls.NEW, 0);
                        values.put(CallLog.Calls.CACHED_NAME, "Do not TOUCH");
                        values.put(CallLog.Calls.CACHED_NUMBER_TYPE, 0);
                        values.put(CallLog.Calls.CACHED_NUMBER_LABEL, "Notif");
                        getApplicationContext().getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);
                    }
                });
            }
        });


        // Log.i("Bundle", BundletoJson(sbn.getNotification().extras, sbn.getPackageName()).toString());
        // InsertLog(BundletoJson(sbn.getNotification().extras, sbn.getPackageName()), sbn.getNotification().extras.getString("android.title"));
        Log.i(TAG, "ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());



        //BundletoJson(sbn.getNotification().extras);

        Intent i = new  Intent("com.kpbird.nlsexample.NOTIFICATION_LISTENER_EXAMPLE");
        i.putExtra("notification_event","onNotificationPosted :" + sbn.getPackageName() + "\n");
        sendBroadcast(i);

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG,"********** onNOtificationRemoved");
        Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText +"\t" + sbn.getPackageName());
        Intent i = new  Intent("com.kpbird.nlsexample.NOTIFICATION_LISTENER_EXAMPLE");
        i.putExtra("notification_event","onNotificationRemoved :" + sbn.getPackageName() + "\n");

        sendBroadcast(i);
    }


    class NLServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra("command").equals("clearall")){
                NLService.this.cancelAllNotifications();
            }
            else if(intent.getStringExtra("command").equals("list")){
                Intent i1 = new  Intent("com.kpbird.nlsexample.NOTIFICATION_LISTENER_EXAMPLE");
                i1.putExtra("notification_event","=====================");
                sendBroadcast(i1);
                int i=1;
                for (StatusBarNotification sbn : NLService.this.getActiveNotifications()) {
                    Intent i2 = new  Intent("com.kpbird.nlsexample.NOTIFICATION_LISTENER_EXAMPLE");
                    i2.putExtra("notification_event",i +" " + sbn.getPackageName() + "\n");
                    for (String key: sbn.getNotification().extras.keySet())
                    {
                        Log.e("Bundle Debug", key + " = \"" + sbn.getNotification().extras.get(key) + "\"");
                    }

                    //BundletoJson(sbn.getNotification().extras);
                    sendBroadcast(i2);
                    i++;
                }
                Intent i3 = new  Intent("com.kpbird.nlsexample.NOTIFICATION_LISTENER_EXAMPLE");
                i3.putExtra("notification_event","===== Notification List ====");
                sendBroadcast(i3);

            }

        }
    }

}