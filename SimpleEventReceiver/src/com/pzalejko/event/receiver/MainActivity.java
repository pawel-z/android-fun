package com.pzalejko.event.receiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.content.Context;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String TAG = "EventReceiver";
	private static final int VIBRATION_TIME = 1500;// 1.5s
	private static final int REPEAT_VIBRATION = 10;

	private static final int DEFAULT_PORT_NUMBER = 4444;
	private ServerSocket serverSocket;

	private EditText vTome;
	private EditText vOccurrence;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		vTome = (EditText) findViewById(R.id.vibrationTimeEdit);
		vOccurrence = (EditText) findViewById(R.id.vibrationOccurrenceEdit);

		vTome.setText(Integer.toString(VIBRATION_TIME));
		vOccurrence.setText(Integer.toString(REPEAT_VIBRATION));

		TextView serverIpInfo = (TextView) findViewById(R.id.textView1);
		serverIpInfo.setText(getResources().getString(R.string.server_ip) + getIpAddress());

		try {
			serverSocket = new ServerSocket(DEFAULT_PORT_NUMBER);
			startListening();
		} catch (IOException e) {
			logError(R.string.could_not_initialize_server, e);
		}

	}

	private void startListening() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {

					InputStreamReader inputStreamReader = null;
					Socket clientSocket = null;
					try {
						clientSocket = serverSocket.accept();
						inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
						BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

						if (bufferedReader.readLine() != null) {
							// an event has been received.
							startVibration();
							inputStreamReader.close();
							clientSocket.close();
						}
					} catch (IOException e) {
						logError(R.string.error_occurred, e);
					} finally {
						if (clientSocket != null) {
							try {
								clientSocket.close();
							} catch (IOException e) {
								logError(R.string.cannot_close_resource, e);
							}
						}

						if (inputStreamReader != null) {
							try {
								inputStreamReader.close();
							} catch (IOException e) {
								logError(R.string.cannot_close_resource, e);
							}
						}
					}
				}

			}
		}).start();
	}

	private void startVibration() {
		int time = getVTime();
		int occurrence = getOccurrence();

		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		for (int i = 0; i < occurrence; i++) {
			v.vibrate(time);
			waitForNext(time);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private String getIpAddress() {
		WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
		return Formatter.formatIpAddress(wifiInfo.getIpAddress());
	}

	private void logError(int msgId, Exception e) {
		String errorMsg = getResources().getString(msgId);
		Log.e(TAG, errorMsg, e);
		showToast(errorMsg);
	}

	private void showToast(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}

	private int getVTime() {
		try {
			return Integer.parseInt(vTome.getText().toString());
		} catch (NumberFormatException e) {
			return VIBRATION_TIME;
		}
	}

	private int getOccurrence() {
		try {
			return Integer.parseInt(vOccurrence.getText().toString());
		} catch (NumberFormatException e) {
			return REPEAT_VIBRATION;
		}
	}

	private void waitForNext(int time) {
		try {
			Thread.sleep(time + time * 2);
		} catch (InterruptedException e) {
			logError(R.string.vibration_error, e);
		}
	}
}
