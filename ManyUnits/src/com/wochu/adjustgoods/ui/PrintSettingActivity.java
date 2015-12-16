package com.wochu.adjustgoods.ui;



import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wochu.adjustgoods.R;
import com.wochu.adjustgoods.printer.Device;
import com.wochu.adjustgoods.printer.PrintService;
import com.wochu.adjustgoods.printer.PrinterClass;
import com.wochu.adjustgoods.printer.PrinterClassFactory;

import zpSDK.zpSDK.zpSDK;






import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

//zkc.bluetooth.api

public class PrintSettingActivity extends ListActivity {
	private String TAG = "BtSetting";
	private ArrayAdapter<String> mPairedDevicesArrayAdapter = null;
	public static ArrayAdapter<String> mNewDevicesArrayAdapter = null;
	public static List<Device> deviceList=new ArrayList<Device>();
	private Button bt_scan;
	public Handler mhandler;
	public Handler _handler;
	private LinearLayout layoutscan;
	private TextView tv_status;
	private Thread tv_update;
	private boolean tvFlag = true;
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static PrinterClass pl;
	public static boolean connect;
	Context _context;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_printsetting);
		// 允许主线程连接网络
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		 
		
		_context=this;
		mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.drawable.device_name);
		
		InitListView();		
		initHandler();
		pl = PrinterClassFactory.create(0, this, mhandler, _handler);
		layoutscan = (LinearLayout) findViewById(R.id.layoutscan);
		layoutscan.setVisibility(View.GONE);

		mNewDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.drawable.device_name);
		deviceList=new ArrayList<Device>();
		bt_scan = (Button) findViewById(R.id.bt_scan);
		bt_scan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(deviceList!=null)
				{
					deviceList.clear();
				}
				if (!pl.IsOpen()) {
					pl.open(_context);
				}
				layoutscan.setVisibility(View.VISIBLE);
				mNewDevicesArrayAdapter.clear();
				pl.scan();				
				deviceList = pl.getDeviceList();
				InitListView();
			}
		});

		tv_status = (TextView) findViewById(R.id.tv_status);
		tv_update = new Thread() {
			public void run() {
				while (tvFlag) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					tv_status.post(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							if (pl != null) {
								if (pl.getState() == PrinterClass.STATE_CONNECTED) {
									tv_status.setText("连接成功");
									//MainActivity.checkState=true;
									tvFlag=false;
									PrintSettingActivity.this.finish();
									
								} else if (pl.getState() == PrinterClass.STATE_CONNECTING) {
									tv_status.setText("正在连接");
								} 
								else if(pl.getState()==PrinterClass.STATE_SCAN_STOP)
								{
									tv_status.setText("连接失败");
									layoutscan.setVisibility(View.GONE);
									InitListView();
								}
								else if(pl.getState()==PrinterClass.STATE_SCANING)
								{
									tv_status.setText("正在扫描");
									InitListView();
								}
								else {
									int ss=pl.getState();
									tv_status.setText("连接失败");
								}
							}
						}
					});
				}
			}
		};
		tv_update.start();
		
	}
	
	private void InitListView()
	{
		SimpleAdapter simpleAdapter=new SimpleAdapter(this,getData("simple-list-item-2"),android.R.layout.simple_list_item_2,new String[]{"title", "description"},new int[]{android.R.id.text1, android.R.id.text2});
		setListAdapter(simpleAdapter);
	}
	
	/**
     * 当List的项被选中时触发
     */
    protected void onListItemClick(ListView listView, View v, int position, long id) {  
    	Map map = (Map)listView.getItemAtPosition(position);
    	String cmd=map.get("description").toString();
//		pl.connect(cmd);
//	if( BtSPP.OpenPrinter(cmd)){
		
//		finish();
//	}else{
//		Toast.makeText(getApplicationContext(), "连接失败", Toast.LENGTH_SHORT).show();
//	}
    	BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    	BluetoothDevice device = bluetoothAdapter.getRemoteDevice(cmd);
    	if(zpSDK.zp_open(bluetoothAdapter, device)){
    		connect = true;
    		Intent resultIntent = new Intent();
    		Bundle bundle = new Bundle();
    		bundle.putString("result", cmd);
    		resultIntent.putExtras(bundle);
    		this.setResult(RESULT_OK, resultIntent);
    		finish();
    	}
    }
    
	
	/**
     * 构造SimpleAdapter的第二个参数，类型为List<Map<?,?>>
     * @param title
     * @return
     */
    private List<Map<String, String>> getData(String title) {
    	List<Map<String, String>> listData = new ArrayList<Map<String, String>>();
    	if(deviceList!=null)
    	{
	    	for(int i=0;i<deviceList.size();i++)
	    	{
	    		Map<String, String> map = new HashMap<String, String>();
	    		map.put("title", deviceList.get(i).deviceName);
	    		map.put("description", deviceList.get(i).deviceAddress);
	    		
	    		listData.add(map);
	    	}
    	}
    	return listData;
    }

	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
    @Override
    public void onBackPressed() {
	new AlertDialog.Builder(this).setTitle("退出打印")
		.setIcon(android.R.drawable.ic_dialog_info)
		.setPositiveButton("YES", new DialogInterface.OnClickListener() {
 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
			// 点击“确认”后的操作
				//MainActivity.checkState=false;
				finish();
		    }
		})
		.setNegativeButton("NO", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
			// 点击“返回”后的操作,这里不设置没有任何操作
		    }
		}).show();
	// super.onBackPressed();
    }

private void initHandler(){
	
	mhandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_READ:
				 byte[] readBuf = (byte[]) msg.obj;
				 Log.i(TAG, "readBuf:"+readBuf[0]);
				 if(readBuf[0]==0x13)
				 {
					 /*SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd    hh:mm:ss:ssss");       
					 Log.i(TAG, "0x13:"+sDateFormat.format(new java.util.Date()));*/
					 PrintService.isFUll=true;
				 }
				 else if(readBuf[0]==0x11)
				 {
					 /*SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd    hh:mm:ss:ssss");       
					 Log.i(TAG, "0x11:"+sDateFormat.format(new java.util.Date()));*/
					 PrintService.isFUll=false;
				 }
				 else{
	                // construct a string from the valid bytes in the buffer
					 /*SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd    hh:mm:ss:ssss");       
					 Log.i(TAG, "DATA:"+sDateFormat.format(new java.util.Date()));*/
	                String readMessage = new String(readBuf, 0, msg.arg1);
	                if(readMessage.contains("800"))//80mm paper
	                {
	                	PrintService.imageWidth=72;
		                Toast.makeText(getApplicationContext(),"80mm",
	                               Toast.LENGTH_SHORT).show();
	                }
	                else if(readMessage.contains("580"))//58mm paper
	                {
	                	PrintService.imageWidth=48;
		                Toast.makeText(getApplicationContext(),"58mm",
	                               Toast.LENGTH_SHORT).show();
	                }
	                
	                Toast.makeText(getApplicationContext(),readMessage,
                               Toast.LENGTH_SHORT).show();
				 }
				break;
			case MESSAGE_STATE_CHANGE:// 蓝牙连接状
				switch (msg.arg1) {
				case PrinterClass.STATE_CONNECTED:// 已经连接
					break;
				case PrinterClass.STATE_CONNECTING:// 正在连接
					Toast.makeText(PrintSettingActivity.this,"正在连接",
							Toast.LENGTH_SHORT).show();
					break;
				case PrinterClass.STATE_LISTEN:
				case PrinterClass.STATE_NONE:
					break;
				case PrinterClass.SUCCESS_CONNECT:
					pl.write(new byte[]{0x1b,0x2b});
					Toast.makeText(getApplicationContext(), "连接成功",
							Toast.LENGTH_SHORT).show();
					break;
				case PrinterClass.FAILED_CONNECT:
					Toast.makeText(getApplicationContext(), "连接失败",
							Toast.LENGTH_SHORT).show();

					break;
				case PrinterClass.LOSE_CONNECT:
					Toast.makeText(getApplicationContext(),"失去连接",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case MESSAGE_WRITE:

				break;
			}
			super.handleMessage(msg);
		}
	};
	_handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				break;
			case 1:// 扫描完毕
				Device d=(Device)msg.obj;
				if(d!=null)
				{
					if(PrintSettingActivity.deviceList==null)
					{
						PrintSettingActivity.deviceList=new ArrayList<Device>();
					}
					
					if(!checkData(PrintSettingActivity.deviceList,d))
					{
						PrintSettingActivity.deviceList.add(d);
					}
				}
				break;
			case 2:// 停止扫描
				break;
			}
		}
	};
}
private boolean checkData(List<Device> list,Device d)
{
	for (Device device : list) {
		if(device.deviceAddress.equals(d.deviceAddress))
		{
			return true;
		}
	}
	return false;
} 

}
