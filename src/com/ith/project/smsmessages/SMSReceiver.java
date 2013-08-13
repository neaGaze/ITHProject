package com.ith.project.smsmessages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		SmsMessage msgs = null;
		String message = null;
		String receivedFrom = null;
		StringBuilder strBuilder = new StringBuilder();
		if (bundle != null) {
			Object[] pdus = (Object[]) bundle.get("pdus");
			for (int i = 0; i < pdus.length; i++) {
				msgs = SmsMessage.createFromPdu((byte[]) pdus[i]);
				receivedFrom = msgs.getDisplayOriginatingAddress();
				strBuilder.append(msgs.getDisplayMessageBody() + " ");
				Log.e("Originating Address",
						"" + msgs.getDisplayOriginatingAddress());
				Log.e("Originating Message", "" + receivedFrom);
			}

			message = strBuilder.toString();

			/** To notify that the SMS has been received **/
			Intent smsReceiverIntent = new Intent();
			smsReceiverIntent.setAction("SMS_RECEIVED_ACTION");
			smsReceiverIntent.putExtra("MSG_BODY", message);
			smsReceiverIntent.putExtra("MSG_SENDER_NUMBER", receivedFrom);
			context.sendBroadcast(smsReceiverIntent);

		}

	}

}
