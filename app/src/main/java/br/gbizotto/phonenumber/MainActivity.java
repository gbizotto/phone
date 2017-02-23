package br.gbizotto.phonenumber;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (hasPhonePermissions()) {
            getPhoneNumber();
        }
    }

    private boolean hasPhonePermissions() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return true;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ) {
            requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS}, 1);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getPhoneNumber();
        }
    }

    private void getPhoneNumber() {
        String phoneNumber = getPhoneNumberDefault();

        TextView textView = (TextView) findViewById(R.id.txtPhoneNumber);

        if (!setPhoneNumberOnTextView(textView, phoneNumber)) {

            phoneNumber = getPhoneNumberAlternative();
            TextView textView1 = (TextView) findViewById(R.id.txtPhoneNumberExtra);
            if (!setPhoneNumberOnTextView(textView1, phoneNumber)) {

                String lastResort = lastResort();
                TextView textView2 = (TextView) findViewById(R.id.txtPhoneNumberLastResort);
                setPhoneNumberOnTextView(textView2, lastResort);
            }
        }
    }

    private boolean setPhoneNumberOnTextView(TextView textView, String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            textView.setText("telefone null ou vazio");
            return false;
        }
        textView.setText(phoneNumber);
        return true;
    }

    private String getPhoneNumberDefault() {
        TelephonyManager tMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        return tMgr.getLine1Number();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private String getPhoneNumberAlternative() {
        SubscriptionManager subscriptionManager=(SubscriptionManager)getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

        List<SubscriptionInfo> subscriptionInfoList=subscriptionManager.getActiveSubscriptionInfoList();

        StringBuilder phones = new StringBuilder();
        if(subscriptionInfoList!=null && subscriptionInfoList.size()>0){
            for(SubscriptionInfo info:subscriptionInfoList){

                Log.v(MainActivity.class.getSimpleName(), info.toString());

                if (!TextUtils.isEmpty(info.getNumber())) {
                    phones.append(info.getNumber())
                            .append(", ");
                }
            }
        }

        return phones.toString();
    }

    private String lastResort() {
        String s1 = null;
        String main_data[] = {"data1", "is_primary", "data3", "data2", "data1", "is_primary", "photo_uri", "mimetype"};
        Object object = getContentResolver().query(Uri.withAppendedPath(android.provider.ContactsContract.Profile.CONTENT_URI, "data"),
                main_data, "mimetype=?",
                new String[]{"vnd.android.cursor.item/phone_v2"},
                "is_primary DESC");
        if (object != null) {
            do {
                if (!((Cursor) (object)).moveToNext())
                    break;
                // This is the phoneNumber
                s1 = ((Cursor) (object)).getString(4);
            } while (true);
            ((Cursor) (object)).close();
        }
        return s1;
    }

}
