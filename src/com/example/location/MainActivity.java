package com.example.location;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends Activity {

	public static final int SHOW_LOCATION = 0;

	private TextView positionTextView;

	private LocationManager locationManager;

	private String provider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		positionTextView = (TextView) findViewById(R.id.position_text_view);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// 获取所有可用的位置提供器
		List<String> providerList = locationManager.getProviders(true);
		if (providerList.contains(LocationManager.GPS_PROVIDER)) {
			provider = LocationManager.GPS_PROVIDER;
		} else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
			provider = LocationManager.NETWORK_PROVIDER;
		} else {
			// 当没有可用的位置提供器时，弹出Toast提示用户
			Toast.makeText(this, "No location provider to use",
					Toast.LENGTH_SHORT).show();
			return;
		}
		Location location = locationManager.getLastKnownLocation(provider);
		if (location != null) {
			// 显示当前设备的位置信息
			showLocation(location);
		}
		locationManager.requestLocationUpdates(provider, 5000, 1,
				locationListener);
	}

	protected void onDestroy() {
		super.onDestroy();
		if (locationManager != null) {
			// 关闭程序时将监听器移除
			locationManager.removeUpdates(locationListener);
		}
	}

	LocationListener locationListener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onLocationChanged(Location location) {
			// 更新当前设备的位置信息
			showLocation(location);
		}
	};

	private void showLocation(final Location location) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// 组装反向地理编码的接口地址
					StringBuilder url = new StringBuilder();
					url.append("http://api.map.baidu.com/geocoder/v2/?ak=nUvlnh8wk6cDupYs5foacyXz&location=");
					url.append(location.getLatitude()).append(",")
							.append(location.getLongitude());
					url.append("&output=json&pois=1");
					HttpClient httpClient = new DefaultHttpClient();
					HttpGet httpGet = new HttpGet(url.toString());
					// 在请求消息头中指定语言，保证服务器会返回中文数据
					httpGet.addHeader("Accept-Language", "zh-CN");
					HttpResponse httpResponse = httpClient.execute(httpGet);
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						HttpEntity entity = httpResponse.getEntity();
						String response = EntityUtils.toString(entity, "utf-8");
						JSONObject jsonObject = new JSONObject(response);
						if(jsonObject.get("status").toString().equals("0")){
							StringBuilder address =new StringBuilder();
							address.append(jsonObject.getJSONObject("result").getString("formatted_address"));
							address.append("\r\n").append("纬度：").append(location.getLatitude()).append("\r\n")
							.append("经度").append(location.getLongitude());
							String aString =address.toString();
							Log.d("Mainactivity",aString);
							Message message = new Message();
							message.what = SHOW_LOCATION;
							message.obj = aString;
							handler.sendMessage(message);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SHOW_LOCATION:
				String currentPosition = (String) msg.obj;
				positionTextView.setText(currentPosition);
				break;
			default:
				break;
			}
		}

	};
	}
