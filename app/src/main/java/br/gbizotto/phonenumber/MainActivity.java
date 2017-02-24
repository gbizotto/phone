package br.gbizotto.phonenumber;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import br.gbizotto.phonenumber.receiver.SMSReceiver;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_PHONE = 1;
    private static final int PERMISSION_SMS = 2;

    @BindView(R.id.edtPhoneNumber)
    EditText mEdtPhoneNumber;

    @BindView(R.id.txtInputPhoneNumber)
    TextInputLayout mTxtInputPhoneNumber;

    private SMSReceiver mSmsReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        if (hasPhonePermissions()) {
            getPhoneNumber();
        }
    }

    @OnClick(R.id.btnSendSms)
    public void onSendSmsClick() {
        if (TextUtils.isEmpty(mEdtPhoneNumber.getText())) {
            mTxtInputPhoneNumber.setError(getString(R.string.empty_phone_number));
        } else {
            mTxtInputPhoneNumber.setError(null);
            if (hasSMSPermissions()) {
                sendSMS();

                // Register the broadcast receiver here, so it can be killed along the activity.
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");

                mSmsReceiver = new SMSReceiver();
                registerReceiver(mSmsReceiver, intentFilter);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSmsReceiver != null) {
            unregisterReceiver(mSmsReceiver);
            mSmsReceiver = null;
        }
    }

    private boolean hasPhonePermissions() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return true;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CONTACTS},
                    PERMISSION_PHONE);
            return false;
        }

        return true;
    }

    private boolean hasSMSPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return true;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                            Manifest.permission.READ_SMS,
                            Manifest.permission.SEND_SMS},
                    PERMISSION_SMS);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (grantResults.length > 0) {
            switch (requestCode) {
                case PERMISSION_PHONE:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                            grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                        getPhoneNumber();
                    }
                    break;
                case PERMISSION_SMS:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                            grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                        sendSMS();
                    } else {
                        Toast.makeText(this, R.string.sms_denied, Toast.LENGTH_LONG).show();
                    }
            }
        }
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getPhoneNumber();
        }
    }

    private void sendSMS() {
        SmsManager sm = SmsManager.getDefault();
        String message = "SMS Message";
        sm.sendTextMessage(mEdtPhoneNumber.getText().toString(), null, message, null, null);
    }

    private void getPhoneNumber() {
        String phoneNumber = getPhoneNumberDefault();

        if (TextUtils.isEmpty(phoneNumber)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                phoneNumber = getPhoneNumberAlternative();
            }

            if (TextUtils.isEmpty(phoneNumber)) {
                phoneNumber = lastResort();
            }
        }

        if (!TextUtils.isEmpty(phoneNumber)) {
            mEdtPhoneNumber.setText(phoneNumber);
        }


    }

    private String getPhoneNumberDefault() {
        TelephonyManager tMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        return tMgr.getLine1Number();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private String getPhoneNumberAlternative() {
        SubscriptionManager subscriptionManager = (SubscriptionManager)getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

        List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();

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
            Cursor cursor = (Cursor) object;
            while (cursor.moveToNext()) {
                s1 = cursor.getString(4);
            }
            cursor.close();

        }
        return s1;
    }

}
