package com.wochu.adjustgoods.ui;


import zpSDK.zpSDK.zpSDK;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.wochu.adjustgoods.R;
import com.wochu.adjustgoods.bean.SendingDetail;
import com.wochu.adjustgoods.myinterface.AutoEquit;
import com.wochu.adjustgoods.net.ServerUrl;
import com.wochu.adjustgoods.printer.BarcodeCreater;
import com.wochu.adjustgoods.printer.MyLablePrinter;
import com.wochu.adjustgoods.printer.PrintService;
import com.wochu.adjustgoods.receiver.PDAReceiver;
import com.wochu.adjustgoods.utils.LogUtil;



/**
 * 打印功能
 * @author Administrator
 *
 */
public class PrintAct extends Activity implements OnClickListener,AutoEquit{
private Button btn_unbindFbox;
private int scanCount = 0;
private String scanCode;

public static final int MESSAGE_STATE_CHANGE = 1;
public static final int MESSAGE_READ = 2;
public static final int MESSAGE_WRITE = 3;
public static final int MESSAGE_DEVICE_NAME = 4;
public static final int MESSAGE_TOAST = 5;

private int time ;
private boolean start;
private Handler handler = new Handler(){
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case 0:
			scanCode();
			break;
		case 1:
			time++;
			//锁屏十分钟activity关闭
			if(time<600){
				if(start){
					handler.sendEmptyMessageDelayed(1, 1000);
				}else{
					time = 0;

					
				}
			}else{
//				start =true;
				finish();
				time = 0;
			}
			break;
		default:
			break;
		}
	}
};
private EditText et_FBoxcode;
private Button btn_confirmFBox;
private Button btn_cancleFBox;
private Button btn_print;
private ImageView barcode;
private SendingDetail sendingDetail;
private BroadcastReceiver receiver;
private String addr;
private TextView tv_printText;

@Override
protected void onCreate(Bundle savedInstanceState) {
	// TODO Auto-generated method stub
	super.onCreate(savedInstanceState);
	setContentView(R.layout.act_recovery);
	initView();
	receiver = new PDAReceiver() {
		
		@Override
		public void dispathCode(String code) {
			// TODO Auto-generated method stub
			handleCode(code);
		}
	};
	IntentFilter scanDataIntentFilter = new IntentFilter();
	//scanDataIntentFilter.addAction("com.android.scancontext"); //前台输出
scanDataIntentFilter.addAction("com.android.scanservice.scancontext"); //后台输出
	registerReceiver(receiver, scanDataIntentFilter);
	
	
	//获取剪切板内容
	final ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);  
	cm.addPrimaryClipChangedListener(new OnPrimaryClipChangedListener() {  
	    @Override  
	    public void onPrimaryClipChanged() {  
	        ClipData data = cm.getPrimaryClip();  
	                Item item = data.getItemAt(0);  
	        Toast.makeText(getApplicationContext(), item.getText().toString(), 0).show(); 
	    }  
	});
	
//	autoEquit();
}
/**
 * 初始化布局
 */
private void initView(){
	btn_unbindFbox = (Button) findViewById(R.id.btn_unbindFbox);
	et_FBoxcode = (EditText) findViewById(R.id.et_FBoxcode);
	btn_confirmFBox = (Button) findViewById(R.id.btn_confirmFBox);
	btn_cancleFBox = (Button) findViewById(R.id.btn_cancleFBox);
	btn_print = (Button) findViewById(R.id.btn_print);
	barcode = (ImageView) findViewById(R.id.barcode);
	tv_printText = (TextView) findViewById(R.id.tv_printText);
	btn_unbindFbox.setOnClickListener(this);
	btn_confirmFBox.setOnClickListener(this);
	btn_cancleFBox.setOnClickListener(this);
	//长按连续扫码5次
	btn_print.setOnClickListener(this);
	btn_unbindFbox.setOnLongClickListener(new OnLongClickListener() {
		
		@Override
		public boolean onLongClick(View v) {
			Toast.makeText(PrintAct.this, "进入连扫5次模式", Toast.LENGTH_SHORT).show();
			autoScan();
			return true;
		}
	});
}
@Override
public void onClick(View v) {
	switch (v.getId()) {
	case R.id.btn_unbindFbox:
		scanCode();
		break;
	case R.id.btn_confirmFBox:
		scanCode = et_FBoxcode.getText().toString();
		handleCode(scanCode);
		break;
	case R.id.btn_cancleFBox:
		scanCode = null;
		et_FBoxcode.setText("");
		break;
	case R.id.btn_print:
		if(PrintSettingActivity.connect){
			if(sendingDetail!=null&&sendingDetail.DATA!=null){
			System.out.println("打印");
		print();
			}else{
				Toast.makeText(getApplicationContext(), "请检查发货箱", 0).show();
			}
//			print();
		}else {
			Intent intent = new Intent(PrintAct.this,PrintSettingActivity.class);
			startActivityForResult(intent, 0);
		}
		
	break;
	default:
		break;
	}
	
}
@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==RESULT_OK){
			Bundle bundle = data.getExtras();
			addr = bundle.getString("result");
			
		}
	}
/**
 * 自动扫描
 */
private void autoScan(){
	scanCount++;
	scanCode();
}

/**
 * 解析条码
 * @param code
 */
private void handleCode(String code){
	scanCode= code;
	et_FBoxcode.setText(code);
	comitData();
}
/**
 * 设置文本条码
 * @param code
 */
private void  setText(String code){
	et_FBoxcode.setText(code);
}
/**
 * 提交数据
 */
private void comitData(){
	
	HttpUtils http = new HttpUtils();
	http.send(HttpMethod.GET, ServerUrl.SENDBOXSERVER+et_FBoxcode.getText().toString(), new RequestCallBack<String>() {
		
		@Override
		public void onSuccess(ResponseInfo<String> responseInfo) {
			// TODO Auto-generated method stub
			LogUtil.i("测试提交", responseInfo.result);
			processData(responseInfo.result);
			if(scanCount>0&&scanCount<5){
				scanCount++;
				handler.sendEmptyMessageDelayed(0, 1000);
				
			}else if(scanCount>=5){
				scanCount=0;
			}
			LogUtil.i("响应", responseInfo.result);
		}

		@Override
		public void onFailure(HttpException error, String msg) {
			// TODO Auto-generated method stub
			
		}
	});
	//测试代码
	LogUtil.i("测试提交", "提交了");
	if(scanCount>0&&scanCount<5){
		scanCount++;
		handler.sendEmptyMessageDelayed(0, 1000);
		
	}else if(scanCount>=5){
		scanCount=0;
	}
	
}
/**
 * 启动扫码
 */
private void scanCode(){
	Intent intent = new Intent("android.intent.action.FUNCTION_BUTTON_DOWN");
	sendBroadcast(intent);
}
/**
 * 打印
 */
private void print(){
//if(sendingDetail.DATA!=null){
//	if(sendingDetail.DATA.get(0).TICKETNO!=null){
//	final Bitmap bitmap = getBitmap(sendingDetail.DATA.get(0).TICKETNO);
//	barcode.setImageBitmap(bitmap);
//	PrintSettingActivity.pl.printImage(bitmap);
//	//&&PrintSettingActivity.pl.getState()==PrinterClass.STATE_CONNECTED
//	BtSPP.SPPWrite(new byte[]{0x1d,0x48,0x02});	//设置条码内容打印在条码下方
//	BtSPP.SPPWrite(new byte[]{0x1d,0x77,0x02});	//设置条码宽度0.375
//	BtSPP.SPPWrite(new byte[]{0x1d,0x68,0x40});	//设置条码高度64
//	//打印code128条码
//	BtSPP.SPPWrite(new byte[]{0x1D,0x6B,0x18});
//	try {
//		BtSPP.SPPWrite((sendingDetail.DATA.get(0).TICKETNO+"\0").getBytes("GBK"));
//		BtSPP.SPPWrite("\n".getBytes("GBK"));
//		  
////		BtSPP.SPPWrite(new byte[]{0x1D,0x21,0x00});
////		BtSPP.SPPWrite(new byte[]{0x1B,0x4D,0x00});
//			
//		BtSPP.SPPWrite(new byte[]{0x1B,0x33,0x01});//0行距
//		BtSPP.SPPWrite(String.format(printStringFormat()).getBytes("GBK"));
//		BtSPP.SPPWrite("\n".getBytes("GBK"));
//		BtSPP.SPPWrite("\n".getBytes("GBK"));
//		BtSPP.SPPWrite("\n".getBytes("GBK"));
//		BtSPP.SPPWrite("\n".getBytes("GBK"));
//		BtSPP.SPPWrite("\n".getBytes("GBK"));
//	} catch (UnsupportedEncodingException e1) {
//		// TODO Auto-generated catch block
//		e1.printStackTrace();
//	}
//	
////	PrintSettingActivity.pl.printText(printStringFormat());
//	
//}else{
//	Toast.makeText(getApplicationContext(), "请检查条形码", 0).show();
//}
//}
//	PrintSettingActivity.pl.printText(sendingDetail.toString());
//	PrintSettingActivity.pl.printImage(bitmap);
	if(!PrintSettingActivity.connect){
		
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		zpSDK.zp_open(btAdapter , btAdapter.getRemoteDevice(addr));
	}
//MyLablePrinter.printLable(-2); 
MyLablePrinter.printLable(40,printStringFormat());
clearView();
//MyLablePrinter.printLable(3);

}

private void showMyToast(String title,String content){
	LayoutInflater inflater = getLayoutInflater();
	   View layout = inflater.inflate(R.layout.mytoast,
	     (ViewGroup) findViewById(R.id.llToast));
	   ImageView image = (ImageView) layout
	     .findViewById(R.id.tvImageToast);
	   image.setImageResource(R.drawable.warn);
	   TextView tv_title = (TextView) layout.findViewById(R.id.tvTitleToast);
	   tv_title.setText(title);
	   TextView text = (TextView) layout.findViewById(R.id.tvTextToast);
	   text.setText(content);
	   Toast toast = new Toast(getApplicationContext());
	   toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 12, 40);
	   toast.setDuration(Toast.LENGTH_SHORT);
	   toast.setView(layout);
	   toast.show();
}
/**
 * 将条码转为位图对象
 * @param code
 * @return
 */
private Bitmap getBitmap(String code){
	Bitmap bitmap = BarcodeCreater.creatBarcode(this, code, PrintService.imageWidth*8, 100, true, 1);
	return bitmap;
}
/**
 * 解析并封装服务器响应的数据
 * @param result
 */
private void processData(String result){
	Gson gson = new Gson();
	sendingDetail = gson.fromJson(result, SendingDetail.class);
	if(sendingDetail.DATA==null){
		Toast.makeText(getApplicationContext(), "此发货箱未绑定订单", 0).show();
	}else{
		
		//展示打印内容
		barcode.setImageBitmap(getBitmap(sendingDetail.DATA.get(0).TICKETNO));
		tv_printText.setText(printFormat());
	}

}


/**
 * 格式化要打印的文字,用于展示打印内容
 * @return
 */
private String printFormat(){
	StringBuilder sb = new StringBuilder();
	if(sendingDetail.DATA.size()>0){
	sb.append("配送地区:   "+sendingDetail.DATA.get(0).SITENAME+"  ");
	sb.append("发货口:"+sendingDetail.DATA.get(0).SENDPORTID+"\r\n");
	sb.append(sendingDetail.DATA.get(0).CONSIGNEEADD+"\r\n");
//	sb.append("支付方式:     "+sendingDetail.DATA.get(0).CUSTOMERPAYTYPENAME+"\r\n");
	sb.append("收件人: "+sendingDetail.DATA.get(0).CONSIGNEE+"  TEL:"+sendingDetail.DATA.get(0).MOBILE+"\r\n");
	String bestDate = sendingDetail.DATA.get(0).RATIONDATE.substring(0, 10);
	sb.append("配送时间:"+bestDate+"  "+sendingDetail.DATA.get(0).RATIONTIMEPERIOD+"\r\n");
//	sb.append(sendingDetail.DATA.get(0).SITEID+"\r\n");
//	sb.append("发货箱号:    "+ scanCode+"\r\n");
	sb.append("订单号:     "+sendingDetail.DATA.get(0).SHEETID+"\r\n");
	}
	
	sb.append("\r\n");
	return sb.toString();
	
}


private String[] printStringFormat(){
	String[] text = new String[8];
	if(sendingDetail.DATA!=null){
	text[0] = sendingDetail.DATA.get(0).TICKETNO;//票号
	text[1] ="配送地区: "+ sendingDetail.DATA.get(0).SITENAME;
	text[2] = sendingDetail.DATA.get(0).CONSIGNEEADD;
	text[3]= "支付方式: "+sendingDetail.DATA.get(0).CUSTOMERPAYTYPENAME;
	text[4] = "发货口: "+ sendingDetail.DATA.get(0).SENDPORTID;
	text[5] = "收件人: "+sendingDetail.DATA.get(0).CONSIGNEE+"  TEL: "+sendingDetail.DATA.get(0).MOBILE;
	text[6] = "配送时间: "+sendingDetail.DATA.get(0).RATIONTIMEPERIOD;
	text[7] = "订单号: "+sendingDetail.DATA.get(0).SHEETID;
	}else{
		text = null;
	}
	
	return text;
}
//
//private void autoEquit(){
//	handler.postDelayed(new Runnable() {
//		
//		@Override
//		public void run() {
//			EquitDelay.equit();
//			
//		}
//	}, 4000);
//}

@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
//		autoEquit();
		equit();
	}

@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		cancleEquit();
	}
@Override
public void equit() {
	start = true;
	handler.sendEmptyMessage(1);
	
}
@Override
public void cancleEquit() {
	// TODO Auto-generated method stub
	start = false;
}

@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		zpSDK.zp_close();
		PrintSettingActivity.connect = false;
	}
/**
 * 初始化界面，清除老数据
 */
private void clearView(){
	if(sendingDetail!=null){
	et_FBoxcode.setText("");
	tv_printText.setText("");
	barcode.setImageBitmap(null);
	}
}
}


