package com.wochu.adjustgoods.ui;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import com.wochu.adjustgoods.R;
import com.wochu.adjustgoods.adapter.Act_main_function;
import com.wochu.adjustgoods.ui.PickingFinishedGoods.pickfinishedgoods_weijianqu;

public class MainAct extends Activity {
	private GridView gv_functions;
	private String[] functions;//功能的数组
	private Act_main_function act_main_function;
	private int[] imageResources;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		initData();
	}
	private void initView() {
		gv_functions = (GridView) findViewById(R.id.gv_functions);
		gv_functions.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				switch (position) {
				case 0:
					Intent intent1 = new Intent(MainAct.this,GoodsQueryAct.class);
					startActivity(intent1);
					break;
				case 1:
					Intent intent2 = new Intent(MainAct.this,AdjustGoodsAct.class);
					startActivity(intent2);
					break;
				case 2:
					Intent intent3 = new Intent(MainAct.this,PickingFinishedGoods.class);
					startActivity(intent3);
					break;
				case 3:
					Intent intent4 = new Intent(MainAct.this,SortingAct.class);
					startActivity(intent4);
					break;
				case 4:
					Intent intent5 = new Intent(MainAct.this,RecheckAct.class);
					startActivity(intent5);
					break;
				case 5:
					Intent intent6 = new Intent(MainAct.this,PrintAct.class);
					startActivity(intent6);
					break;
				case 6:
					Intent intent7 = new Intent(MainAct.this,DebindFboxAct.class);
					startActivity(intent7);
					break;
				case 7:
					Intent intent8 = new Intent(MainAct.this,WeightingAct.class);
					startActivity(intent8);
					break;
				default:
					break;
				}
			}
			
		});
	}
	private void initData() {
		functions = getResources().getStringArray(R.array.ERP_Function); 
		imageResources = new int[] {R.drawable.th_xplane,R.drawable.th_xplane_blue,R.drawable.th_xplane_green,
				R.drawable.th_xplane_red,R.drawable.th_xplane_bluealt,R.drawable.th_notesalt,R.drawable.th_xplane_blue,R.drawable.th_xplane};
		act_main_function = new Act_main_function(this,functions,imageResources); 
		gv_functions.setAdapter(act_main_function);
	}
}
