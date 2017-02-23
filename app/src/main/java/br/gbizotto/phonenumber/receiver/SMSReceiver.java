package br.gbizotto.phonenumber.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {
    public SMSReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Object[] pdus = (Object[])intent.getExtras().get("pdus");
        String format = intent.getStringExtra(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        SmsMessage shortMessage;

        Telephony.Sms.Intents.getMessagesFromIntent(intent);
        //TODO see:
        // http://droidcoders.blogspot.ca/2011/09/sms-receive.html
        // http://www.worldbestlearningcenter.com/tips/Android-receive-sms-programmatically.htm

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            shortMessage = SmsMessage.createFromPdu((byte[]) pdus[0], format);
        } else {
            shortMessage = SmsMessage.createFromPdu((byte[]) pdus[0]);
        }

        Log.d("SMSReceiver","SMS message sender: " + shortMessage.getOriginatingAddress());
        Log.d("SMSReceiver","SMS message text: "+ shortMessage.getDisplayMessageBody());
    }
}
