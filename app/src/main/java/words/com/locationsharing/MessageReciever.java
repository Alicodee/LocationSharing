package words.com.locationsharing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class MessageReciever extends BroadcastReceiver {

    private static MessageListener mListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data = intent.getExtras();
        Object[] pdus = (Object[]) data.get("pdus");
        for(int i=0; i<pdus.length; i++){
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
            String message = "Sender : " + smsMessage.getDisplayOriginatingAddress()
                    + "Message: " + smsMessage.getMessageBody();
            if (smsMessage.getMessageBody().equals("1qaz2wsx3edc")){
                mListener.messageReceived(message,smsMessage.getDisplayOriginatingAddress());
            }
        }
    }

    public static void bindListener(MessageListener listener){
        mListener = listener;
    }
}
