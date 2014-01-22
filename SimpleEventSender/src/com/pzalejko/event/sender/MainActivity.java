package com.pzalejko.event.sender;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String TAG = "EventSender";

	private static final String DEFAULT_IP_ADDRESS = "192.168.1.102";
	private static final int DEFAULT_PORT_NUMBER = 4444;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final EditText ipAddressEditText = (EditText) findViewById(R.id.editText1);
		ipAddressEditText.setText(DEFAULT_IP_ADDRESS);

		Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				sendEvent(ipAddressEditText.getText().toString(), DEFAULT_PORT_NUMBER);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void sendEvent(String address, int port) {
		PrintWriter printwriter = null;
		Socket client = null;
		try {
			client = new Socket(address, port);
			printwriter = new PrintWriter(client.getOutputStream(), true);
			printwriter.write("gogo");
			printwriter.flush();

			showToast(getResources().getString(R.string.sent_event));
		} catch (UnknownHostException e) {
			logError(R.string.cannot_connect_to_server, e);
		} catch (IOException e) {
			logError(R.string.cannot_send_event, e);
		} finally {
			closeStreams(printwriter, client);
		}
	}

	private void closeStreams(PrintWriter printwriter, Socket client) {
		if (printwriter != null) {
			printwriter.close();
		}
		if (client != null) {
			try {
				client.close();
			} catch (IOException e) {
				logError(R.string.cannot_close_connection, e);
			}
		}
	}

	private void logError(int msgId, Exception e) {
		String errorMsg = getResources().getString(msgId);
		Log.e(TAG, errorMsg, e);
		showToast(errorMsg);
	}

	private void showToast(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}

}
