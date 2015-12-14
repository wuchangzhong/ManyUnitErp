package com.wochu.adjustgoods.ui;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Inflater;

import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.wochu.adjustgoods.R;
import com.wochu.adjustgoods.bean.OrderDetail;
import com.wochu.adjustgoods.bean.OrderDetail.OrderInfo;
import com.wochu.adjustgoods.myinterface.AutoEquit;
import com.wochu.adjustgoods.net.ServerUrl;
import com.wochu.adjustgoods.receiver.PDAReceiver;
import com.wochu.adjustgoods.ui.decorator.MySpaceDecration;
import com.wochu.adjustgoods.ui.viewholder.MyRecheckViewHolder;
import com.wochu.adjustgoods.ui.viewholder.MyRecheckViewHolder.MyOnItemLongClickListenner;
import com.wochu.adjustgoods.utils.LogUtil;


/**
 * 详细核单界面 主要包括查詢周转箱，提交错误商品信息功能
 * 
 * @author Administrator
 * 
 */
public class RecheckAct extends Activity implements OnClickListener,AutoEquit {
	private RecyclerView rv_details;
	private TextView tv_orderNum;
	private OrderDetail orderDetail;
	private Button btn_getCode;
	private int totalCount = 0;
	private int[] colors;
	private boolean canSend;
	private Button btn_confCode;
	private Button btn_cancCode;
	private String boxCode;
	private EditText et_getCode;
	private Inflater mInflater;
	private LayoutInflater inflater;
	private Button btn_getItemCode;
	private Map<String, Integer> scanInfo;// 扫描集合，键为商品名称，值为差
	private Map<String, Integer> itemInfo;
	private Button btn_comitInfo;
	private Button btn_confItem;
	private int index = 0;
	private MyAdapter mAdapter;
	private AlertDialog.Builder builder;
	private int[] PQTY;
	private boolean[] cancle;// 判定长按是取消还是标记错误
	private String itemCode;
	private Map<String, Integer> wrongItems;
	private int step = 0;// 控制扫描步骤
	private ArrayList<String> sendBoxes;
	private MyRecheckViewHolder mViewHolder;
	private int time ;//自动退出计数
	private boolean start;
	private Handler hanlder = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// 数据加载成功就装在recyclerview显示详细信息
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				mAdapter.notifyDataSetChanged();
				break;
			case 1:
				break;
			default:
				break;
			}
		}

	};
	private SharedPreferences sp;
	private Button btn_cancItem;
	private BroadcastReceiver receiver;
	private Button btn_sendItems;
	private String username;
	private int userID;
	private TextView tv_order_Count;
	private TextView tv_sorting_Count;
	private TextView tv_sendBoxText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.act_recheck);
//		setAbContentView(R.layout.act_recheck);
//		AbViewUtil.scaleView(View.inflate(this, R.layout.act_recheck, null));

		sp = getSharedPreferences("config", MODE_PRIVATE);
		sendBoxes = new ArrayList<String>();
		username = sp.getString("user", "system");
		userID = sp.getInt("userID", 0);
		initView();
		sp = getSharedPreferences("config", MODE_PRIVATE);
		 receiver = new PDAReceiver() {

			@Override
			public void dispathCode(String code) {
				handleCode(code);
			}
		};
		IntentFilter scanDataIntentFilter = new IntentFilter();
		// scanDataIntentFilter.addAction("com.android.scancontext"); //前台输出
		scanDataIntentFilter.addAction("com.android.scanservice.scancontext"); // 后台输出
		registerReceiver(receiver, scanDataIntentFilter);
	}

	/**
	 * 初始化布局
	 */
	private void initView() {
		btn_getCode = (Button) findViewById(R.id.btn_getCode);
		tv_orderNum = (TextView) findViewById(R.id.tv_orderNum);
		rv_details = (RecyclerView) findViewById(R.id.rv_details);
		btn_confCode = (Button) findViewById(R.id.btn_confCode);
		btn_cancCode = (Button) findViewById(R.id.btn_cancCode);
		et_getCode = (EditText) findViewById(R.id.et_getCode);
		btn_sendItems = (Button) findViewById(R.id.btn_sendItems);
		tv_order_Count = (TextView) findViewById(R.id.tv_order_Count);
		tv_sorting_Count = (TextView) findViewById(R.id.tv_sorting_Count);
		tv_sendBoxText = (TextView) findViewById(R.id.tv_sendBoxText);
		// mHolder = new MyRecheckViewHolder(rootView)
		btn_cancCode.setOnClickListener(this);

		// 添加一个观察监听
		et_getCode.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s.toString().length() > 21) {
					Toast.makeText(RecheckAct.this, "请输入正确的条形码",
							Toast.LENGTH_SHORT).show();
				}

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});

		btn_comitInfo = (Button) findViewById(R.id.btn_comitInfo);

		LinearLayoutManager manager = new LinearLayoutManager(this);
		manager.setOrientation(LinearLayoutManager.VERTICAL);
		StaggeredGridLayoutManager manager2 = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
		rv_details.setLayoutManager(manager);
//rv_details.setLayoutManager(manager2);
MySpaceDecration decration = new MySpaceDecration(15);//item间距装饰类
		btn_getCode.setOnClickListener(this);
		btn_sendItems.setOnClickListener(this);
		btn_comitInfo.setOnClickListener(this);
		btn_confCode.setOnClickListener(this);
		inflater = LayoutInflater.from(this);
		rv_details.addItemDecoration(new DividerItemDecoration(this,
				DividerItemDecoration.VERTICAL_LIST));
//		rv_details.addItemDecoration(decration);
		itemInfo = new HashMap<String, Integer>();
	}

	/**
	 * 提交审核结果
	 */
	@SuppressWarnings("deprecation")
	private void comitData() {
		// if(wrong_items.size()>=1){
		//
		// //提交订单号，错误商品信息
		//
		// }

		if (orderDetail != null) {

			if (scanInfo.size() == orderDetail.DATA.size()) {
				HttpUtils http = new HttpUtils();
				RequestParams params = new RequestParams();
				params.setHeader("Content-Type", "application/json");
				try {
					params.setBodyEntity(new StringEntity(getRequestString(),
							"UTF-8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				http.send(HttpMethod.POST, ServerUrl.SEND_RECHECK, params,
						new RequestCallBack<String>() {

							@Override
							public void onSuccess(
									ResponseInfo<String> responseInfo) {
								LogUtil.i("提交网络", "成功");
								LogUtil.i("网络响应", responseInfo.result);
								if (!itemInfo.isEmpty()) {
									itemInfo.clear();
									scanInfo.clear();
									orderDetail.DATA.clear();
									wrongItems.clear();
									boxCode = null;
									et_getCode.setText("");
									itemCode = null;
									tv_orderNum.setText("");
									et_getCode.setText("");

									LogUtil.i("查看iteminfo",
											String.valueOf(itemInfo.size()));
									hanlder.sendEmptyMessage(0);

								}
							}

							@Override
							public void onFailure(HttpException error,
									String msg) {
								// TODO Auto-generated method stub
								LogUtil.e("提交网络", "失败");
							}
						});
			} else {
				String unscanInfo = getUnscanInfo();
				showMyToast("无法提交数据", unscanInfo);
			}
		} else {

			Toast.makeText(RecheckAct.this, "请检查周转箱号是否未绑定订单",
					Toast.LENGTH_SHORT).show();
		}

	}

	/**
	 * 获取订单信息
	 */
	private void getOrderInfo(String boxNo) {
		HttpUtils http = new HttpUtils();
		boxNo = boxNo.replace('z', 'Z');
		http.send(HttpMethod.GET,
				"http://192.168.34.220:8088/api/OrderPick/GetReviewInfo?BoxNO="
						+ boxNo, new RequestCallBack<String>() {

					@Override
					public void onSuccess(ResponseInfo<String> responseInfo) {
						// 请求网络，得到订单信息

						LogUtil.i("订单信息", responseInfo.result);
						if (responseInfo.result.contains("success")
								&& responseInfo.result.contains("ORDERNO")) {
							processOrderInfo(responseInfo.result);
							scanInfo = new HashMap<String, Integer>();

						} else {
							boxCode = null;
							et_getCode.setText("");
							tv_orderNum.setText("");
							Toast.makeText(getApplicationContext(),
									"请检查条形码是否异常", Toast.LENGTH_SHORT).show();
						}

					}

					@Override
					public void onFailure(HttpException error, String msg) {
						boxCode = null;
						et_getCode.setText("");
						Toast.makeText(getApplicationContext(), "服务器异常",
								Toast.LENGTH_SHORT).show();

					}
				});
	}

	/**
	 * 解析订单详情
	 */
	private void processOrderInfo(String result) {
		Gson gson = new Gson();
		orderDetail = gson.fromJson(result, OrderDetail.class);
		colors = new int[orderDetail.DATA.size()];
		cancle = new boolean[orderDetail.DATA.size()];
		PQTY = new int[orderDetail.DATA.size()];

		wrongItems = new HashMap<String, Integer>();
		// 确定商品条目总数
		totalCount = orderDetail.DATA.size();
		for (int x = 0; x < colors.length; x++) {
			colors[x] = Color.TRANSPARENT;
		}
		// 取出所有商品名称和数量

		for (OrderInfo info : orderDetail.DATA) {
			itemInfo.put(info.ITEMNAME, info.QTY);
		}
		// for (String key : itemInfo.keySet()) {
		// System.out.println("名称"+key+"数量"+itemInfo.get(key));
		// }
		tv_orderNum.setText(orderDetail.DATA.get(0).ORDERNO);
		
		int orderCount = 0;
		int sortingCount = 0;
		for(OrderInfo info:orderDetail.DATA){
			orderCount+=info.QTY;
			sortingCount +=info.PQTY;
		}
		tv_order_Count.setText(orderCount+"");
		tv_sorting_Count.setText(sortingCount+"");
		mAdapter = new MyAdapter();
		rv_details.setAdapter(mAdapter);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_getCode:
			Intent intent = new Intent(
					"android.intent.action.FUNCTION_BUTTON_DOWN");
			sendBroadcast(intent);

			break;
		case R.id.btn_confCode:
			boxCode = et_getCode.getText().toString();
			handleCode(boxCode);
			break;

		case R.id.btn_comitInfo:
			// 先判断是否有未处理的商品信息
			
			comitData();
			// 清空map集合 准备下一次扫描

			break;
		case R.id.btn_cancCode:

			et_getCode.setText("");
			break;
		// case R.id.btn_cancItem:
		// itemCode = null;
		// et_getItemCode.setText("");
		// break;
		case R.id.btn_sendItems:
			if (canSend) {
				if (sendBoxes.size() >= 1) {
					// 提交发货信息
					comitSendItems();
				} else {
					Toast.makeText(getApplicationContext(), "请先绑定一个发货箱",
							Toast.LENGTH_SHORT).show();
				}
			} else {

				showDilaog();

			}
			break;
		default:
			break;
		}

	}

	/**
	 * 解析扫描的条码 判断为哪种条码
	 * 
	 * @param code
	 */
	private void handleCode(String code) {
		System.out.println("进来了");
		Pattern pattern1 = Pattern.compile("Z[0-9]{6}");// 周转箱正则
		Matcher matcher1 = pattern1.matcher(code);
		if (matcher1.matches()) {
			// 条码为周转箱
			// 先清除其他数据
			System.out.println("周转箱");

			if (orderDetail != null) {
				orderDetail.DATA.clear();
			}
			if (scanInfo != null) {
				scanInfo.clear();
			}
			if (wrongItems != null) {
				wrongItems.clear();
			}
			if (mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			}
			getOrderInfo(code);
		} else if (pattern1.matches("[A-X][0-9]{6}", code)) {
			// 条码为发货箱
			System.out.println("扫发货箱");
			checkSendBox(code);

		} else if (code.length() > 7) {
			// 为商品条码
			if (orderDetail != null) {

				handleItemCode(code);
			} else {
				Toast.makeText(getApplicationContext(), "请先获取订单信息", 0).show();
			}
		}
	}

	/**
	 * 解析货物条形码
	 * 
	 * @param scanResult
	 */
	private void handleItemCode(String scanResult) {
		boolean flag = false;
		for (OrderInfo info : orderDetail.DATA) {
			// 判断商品条码在不在订单中
			if (info.ITEMCODE.equals(scanResult)
					|| (info.BARCODE != null && info.BARCODE.equals(scanResult))) {

				flag = true;
				// 遍历扫描集合，判断是不是第一次扫到该商品
				boolean exsits = false;
				for (String key : scanInfo.keySet()) {
					if (key.equals(info.ITEMNAME)) {
						exsits = true;

						if (scanInfo.get(key) < -1) {

							scanInfo.put(info.ITEMNAME, scanInfo.get(key) + 1);
							LogUtil.i("扫描条目", info.ITEMNAME + "+1");
							// System.out.println(info.ITEMNAME+"+1");
							for (int x = 0; x < orderDetail.DATA.size(); x++) {
								if (orderDetail.DATA.get(x).ITEMNAME
										.equals(info.ITEMNAME)) {
									PQTY[x] += 1;
									colors[x] = Color.RED;
									mAdapter.notifyDataSetChanged();
								}
							}
						} else if (scanInfo.get(key) >= 0) {

							System.out.println("进来了");
							// 先找到position
							for (int x = 0; x < orderDetail.DATA.size(); x++) {
								if (orderDetail.DATA.get(x).ITEMNAME
										.equals(info.ITEMNAME)) {
									PQTY[x] += 1;
									colors[x] = Color.RED;
									mViewHolder.itemView.setAnimation(shake());
									mAdapter.notifyDataSetChanged();
								}
							}
							scanInfo.put(info.ITEMNAME, scanInfo.get(key) + 1);
						} else if (scanInfo.get(key) == -1) {
							scanInfo.put(info.ITEMNAME, 0);
							for (int x = 0; x < orderDetail.DATA.size(); x++) {
								if (orderDetail.DATA.get(x).ITEMNAME
										.equals(info.ITEMNAME)) {
									PQTY[x] += 1;
									colors[x] = Color.BLUE;

									mAdapter.notifyDataSetChanged();
								}
							}
						}
					}

				}
				// 不存在的时候为第一次扫到
				if (!exsits) {
					// 第一次扫到该商品，放入集合
					scanInfo.put(info.ITEMNAME, 1 - info.QTY);
					for (int x = 0; x < orderDetail.DATA.size(); x++) {
						if (orderDetail.DATA.get(x).ITEMNAME
								.equals(info.ITEMNAME)) {
							PQTY[x] += 1;
							if (info.QTY == 1) {
								colors[x] = Color.BLUE;

							} else {
								// 存入错误集合
								colors[x] = Color.RED;
								mViewHolder.itemView.setAnimation(shake());
							}

							mAdapter.notifyDataSetChanged();
						}
					}

				}

			}

		}
		if (!flag) {
			// 订单中不存在的商品
			// 弹出对话框确认是不是这个周转箱中的物品
			Toast.makeText(RecheckAct.this, "此商品不在当前订单中", Toast.LENGTH_SHORT)
					.show();
			et_getCode.setText("");
		}

		if (checkOrder()) {
			// 发货按钮变色
			btn_sendItems.startAnimation(shake());

			canSend = true;
		} else {
			canSend = false;
		}
	}

	private class MyAdapter extends Adapter<MyRecheckViewHolder> {

		private MyRecheckViewHolder viewHolder;

		@Override
		public MyRecheckViewHolder onCreateViewHolder(ViewGroup parent,
				int viewType) {
			// View view = View.inflate(RecheckAct.this,
			// R.layout.item_goodsdetail, null);
			View view = inflater.inflate(R.layout.item_goodsdetail, parent,
					false);
			MyRecheckViewHolder viewHolder = new MyRecheckViewHolder(view);
			viewHolder.goodsName = (TextView) view
					.findViewById(R.id.tv_goodsName);
			viewHolder.goodsNum = (TextView) view
					.findViewById(R.id.tv_goodsNum);
			viewHolder.scanNum = (TextView) view.findViewById(R.id.tv_scanNum);
			viewHolder.orderNum = (TextView) view
					.findViewById(R.id.tv_OrderNum);
			return viewHolder;
		}

		@Override
		public void onBindViewHolder(final MyRecheckViewHolder holder,
				int position) {
			// 设置条目信息
			mViewHolder = holder;
			holder.goodsName.setText(orderDetail.DATA.get(position).ITEMNAME);
			holder.goodsNum.setText(orderDetail.DATA.get(position).PQTY + "");
			holder.scanNum.setText(PQTY[position] + "");
			holder.itemView.setBackgroundColor(colors[position]);
			holder.orderNum.setText(orderDetail.DATA.get(position).QTY + "");
			// 设置条目长按事件
			holder.setOnItemLongClickListenner(new MyOnItemLongClickListenner() {

				@Override
				public void onItemLongClick(View v, int position) {
					if (cancle[position]) {
						cancle[position] = false;
						colors[position] = Color.TRANSPARENT;
						PQTY[position] = 0;
						MyAdapter.this.notifyDataSetChanged();
						scanInfo.remove(orderDetail.DATA.get(position).ITEMNAME);
						Toast.makeText(RecheckAct.this, "取消后请重新扫描",
								Toast.LENGTH_SHORT).show();
					} else {
						cancle[position] = true;
						// 长按以后变色
						colors[position] = Color.RED;
						// holder.itemView.setAnimation(shake());
						// 添加到扫描集合
						scanInfo.put(
								orderDetail.DATA.get(position).ITEMNAME,
								Integer.parseInt(holder.scanNum.getText()
										.toString())
										- orderDetail.DATA.get(position).QTY);
						LogUtil.i("商品数量", holder.scanNum.getText().toString());
						// System.out.println("错误"+wrong_items.get(position));

						MyAdapter.this.notifyDataSetChanged();
					}
				}

			});

		}

		@Override
		public int getItemCount() {
			// TODO Auto-generated method stub
			return orderDetail.DATA.size();
		}

	}

	public static class DividerItemDecoration extends
			RecyclerView.ItemDecoration {

		private static final int[] ATTRS = new int[] { android.R.attr.listDivider };

		public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;

		public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

		private Drawable mDivider;

		private int mOrientation;
		private int space;

		public DividerItemDecoration(Context context, int orientation) {
			final TypedArray a = context.obtainStyledAttributes(ATTRS);
			mDivider = a.getDrawable(0);
			a.recycle();
			setOrientation(orientation);
		}

		public void setOrientation(int orientation) {
			if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
				throw new IllegalArgumentException("invalid orientation");
			}
			mOrientation = orientation;
		}

		@Override
		public void onDraw(Canvas c, RecyclerView parent) {
			Log.v("recyclerview - itemdecoration", "onDraw()");

			if (mOrientation == VERTICAL_LIST) {
				drawVertical(c, parent);
			} else {
				drawHorizontal(c, parent);
			}

		}

		public void drawVertical(Canvas c, RecyclerView parent) {
			final int left = parent.getPaddingLeft();
			final int right = parent.getWidth() - parent.getPaddingRight();

			final int childCount = parent.getChildCount();
			for (int i = 0; i < childCount; i++) {
				final View child = parent.getChildAt(i);
				android.support.v7.widget.RecyclerView v = new android.support.v7.widget.RecyclerView(
						parent.getContext());
				final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
						.getLayoutParams();
				final int top = child.getBottom() + params.bottomMargin;
				final int bottom = top + mDivider.getIntrinsicHeight();
				mDivider.setBounds(left, top, right, bottom);
				mDivider.draw(c);
			}
		}

		public void drawHorizontal(Canvas c, RecyclerView parent) {
			final int top = parent.getPaddingTop();
			final int bottom = parent.getHeight() - parent.getPaddingBottom();

			final int childCount = parent.getChildCount();
			for (int i = 0; i < childCount; i++) {
				final View child = parent.getChildAt(i);
				final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
						.getLayoutParams();
				final int left = child.getRight() + params.rightMargin;
				final int right = left + mDivider.getIntrinsicHeight();
				mDivider.setBounds(left, top, right, bottom);
				mDivider.draw(c);
			}
		}

		@Override
		public void getItemOffsets(Rect outRect, int itemPosition,
				RecyclerView parent) {
			if (mOrientation == VERTICAL_LIST) {
				outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
			} else {
				outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
			}
		}
	}

	private String getUnscanInfo() {
		// 比较两个集合
		int count = 0;
		StringBuilder sb = new StringBuilder();

		for (int x = 0; x < orderDetail.DATA.size(); x++) {
			String itemname = orderDetail.DATA.get(x).ITEMNAME;
			if (!scanInfo.containsKey(itemname)) {
				count += 1;
			}
		}
		sb.append("未扫描商品数量:");
		sb.append(count);
		return sb.toString();
	}

	/**
	 * 返回true表示有错误商品
	 * 
	 * @return
	 */
	private boolean compair() {
		boolean flag = false;
		for (OrderInfo info : orderDetail.DATA) {
			if (scanInfo.containsKey(info.ITEMNAME)) {
				if (scanInfo.get(info.ITEMNAME) != 0) {
					// 取出有误的商品信息
					flag = true;
					wrongItems.put(info.ITEMNAME, scanInfo.get(info.ITEMNAME));
				}
			}
		}
		return flag;
	}

	/**
	 * 判断是否能发货，返回true表示可以发货
	 * 
	 * @return
	 */
	private boolean checkOrder() {
		boolean flag = true;
		for (OrderInfo info : orderDetail.DATA) {
			if (scanInfo.containsKey(info.ITEMNAME)) {
				if (scanInfo.get(info.ITEMNAME) != 0) {
					// 有一个商品对不上就是false
					flag = false;

				}
			} else {
				flag = false;
			}
		}

		return flag;

	}

	/**
	 * 将错误集合的信息转成字符串
	 * 
	 * @return
	 */
	private String mapToString() {
		StringBuilder sb = new StringBuilder();
		if (compair()) {

			for (String name : wrongItems.keySet()) {
				sb.append(name + "=");
				sb.append(wrongItems.get(name) + ",");
			}

			String mapString = sb.toString();
			if (mapString.endsWith(",")) {
				mapString = mapString.substring(0, mapString.length() - 1);
			}
			LogUtil.i("异常描述", mapString);
			return mapString;
		}
		return "此订单商品数量正确";
	}

	/**
	 * 提交需要的参数封装为json字符串
	 * 
	 * @return
	 */
	private String getRequestString() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("DETAILNO", orderDetail.DATA.get(0).DETAILNO);
			jsonObject.put("ORDERNO", orderDetail.DATA.get(0).ORDERNO);
			jsonObject.put("BOXNO", boxCode);
			jsonObject.put("EXCEPTYPE", 3);
			jsonObject.put("EXCEPDESC", mapToString());
			jsonObject.put("USERID", sp.getInt("userID", 0));
			jsonObject.put("USERNAME", sp.getString("user", "system"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LogUtil.i("错误描述", jsonObject.toString());
		return jsonObject.toString();
	}

	/**
	 * 检查发货箱是否可用
	 * 
	 * @param code
	 */
	private void checkSendBox(final String code) {
		// 请求网络检查是否为可用发货箱
		HttpUtils http = new HttpUtils();
		http.send(HttpMethod.GET,
				"http://192.168.34.220:8088/api/OrderPick/GetOrderUserInfo?FBoxNO="
						+ code, new RequestCallBack<String>() {

					@Override
					public void onSuccess(ResponseInfo<String> responseInfo) {
						// TODO Auto-generated method stub
						LogUtil.i("解绑结果", responseInfo.result);
						if (responseInfo.result.contains("当前泡沫箱未绑定发货订单")) {
							tv_sendBoxText.setText(tv_sendBoxText.getText().toString()+" "+code);
							sendBoxes.add(code);
							moreDialog();
							canSend = true;

						} else {
						showMyToast("此发货箱不可用", "此发货箱未解绑！");
						}
					}

					@Override
					public void onFailure(HttpException error, String msg) {
				showMyToast("请检查网络！", "服务器无响应!");

					}
				});

	}

	/**
	 * 提交发货
	 */
	private void comitSendItems() {
		RequestParams params = new RequestParams();
		params.setHeader("Content-Type", "application/json");
		String json = sendBoxJson();
		LogUtil.i("请求参数", json);
		try {
			params.setBodyEntity(new StringEntity(json, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpUtils http = new HttpUtils();
		http.send(HttpMethod.POST,
				"http://58.247.11.228:8088/WCS_TO_WMS/SendWMStoWCStask/",
				params, new RequestCallBack<String>() {

					@Override
					public void onSuccess(ResponseInfo<String> responseInfo) {
						LogUtil.i("网络响应", responseInfo.result);
						if (responseInfo.result.contains("success")) {
							Toast.makeText(getApplicationContext(), "提交成功",
									Toast.LENGTH_SHORT).show();
							canSend = false;
							tv_orderNum.setText("");
							orderDetail.DATA.clear();
							mAdapter.notifyDataSetChanged();

						} else {
						showMyToast("提交失败！", "未知异常，请重试");

						}

					}

					@Override
					public void onFailure(HttpException error, String msg) {
						// TODO Auto-generated method stub
						sendBoxes.remove(sendBoxes.size() - 1);
					}
				});
	}

	/**
	 * 发货参数封装
	 * 
	 * @return
	 */
	private String sendBoxJson() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("DETAILNO", orderDetail.DATA.get(0).DETAILNO);
			jsonObject.put("ORDERNO", orderDetail.DATA.get(0).ORDERNO);

			jsonObject.put("EXCEPTYPE", 1);
			jsonObject.put("USERID", userID);
			jsonObject.put("USERNAME", username);
			JSONArray jsonArray = new JSONArray();
			for (int x = 0; x < sendBoxes.size(); x++) {
				jsonArray.put(x, sendBoxes.get(x));
			}
			jsonObject.put("FBOXS", jsonArray);
		} catch (JSONException e) {

			e.printStackTrace();
		}

		return jsonObject.toString();
	}

	/**
	 * 提示是否强制发货
	 */
	private void showDilaog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("此订单商品数目异常");
		builder.setMessage("确定发货吗？");
		builder.setPositiveButton("发货", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Toast.makeText(getApplicationContext(), "请扫描发货箱", 0).show();

			}
		});
		builder.setNegativeButton("提交错误",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						comitData();
					}

				});
		builder.setCancelable(false);
		builder.create();
		builder.show();

	}

	/**
	 * 提示继续绑定发货箱还是直接提交
	 */
	private void moreDialog() {
		AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
		builder1.setTitle("添加发货箱");
		builder1.setPositiveButton("继续", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 让当前箱号置空

				et_getCode.setText("");
				Intent intent = new Intent(
						"android.intent.action.FUNCTION_BUTTON_DOWN");
				sendBroadcast(intent);

			}
		});
		builder1.setNegativeButton("完成", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				// 提交数据
				comitSendItems();

			}
		});
		builder1.setCancelable(false);
		builder1.create();
		builder1.show();
	}

	/**
	 * 晃动动画效果
	 * 
	 * @return
	 */
	private Animation shake() {
		Animation translateAnimation = new TranslateAnimation(0, 10, 0, 0);
		translateAnimation.setInterpolator(new CycleInterpolator(10));
		translateAnimation.setDuration(1000);
		return translateAnimation;

	}

	@Override
	public void equit() {
		start = true;
		hanlder.sendEmptyMessage(1);
		
	}
	@Override
	public void cancleEquit() {
		// TODO Auto-generated method stub
		start = false;
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		equit();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		cancleEquit();
	}
	
	/**
	 * 自定义toast
	 * @param title
	 * @param content
	 */
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
	
	
}