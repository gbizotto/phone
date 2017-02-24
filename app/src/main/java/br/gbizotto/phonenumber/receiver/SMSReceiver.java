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

        //TODO read this from DB, shared preferences, whatever is better for the app.
        String savedPhoneNumber = "";
        //TODO read this from DB, shared preferences, whatever is better for the app.
        String verificationCode = "";

        //TODO see:
        // http://droidcoders.blogspot.ca/2011/09/sms-receive.html
        // http://www.worldbestlearningcenter.com/tips/Android-receive-sms-programmatically.htm

        if (pdus.length > 0) {
            SmsMessage[] messages;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            } else {
                messages = new SmsMessage[pdus.length];
                for (int i = 0; i < messages.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
            }

            for (int i = 0; i < messages.length; i++) {
                if (savedPhoneNumber.equals(messages[i].getOriginatingAddress())) {
                    // If the message was sent locally, this can be enough.

                } else if (messages[i].getMessageBody().contains(verificationCode)){
                    // If code in on the body, it's done.
                }
            }
        }
    }
}
