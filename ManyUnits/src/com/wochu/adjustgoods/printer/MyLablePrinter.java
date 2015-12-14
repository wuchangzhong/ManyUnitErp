package com.wochu.adjustgoods.printer;



import zpSDK.zpSDK.zpSDK;
import zpSDK.zpSDK.zpSDK.BARCODE_TYPE;

public class MyLablePrinter {
	/**
	 * 设置间隙，单位为mm
	 * @param gap
	 */
public static void setGap(int gap){
	zpSDK.zp_goto_mark_label(gap);
}

public static void printText(String text){
	
}

public static void printLable(int gap,String[] text){
//	zpSDK.zp_page_clear();
	zpSDK.zp_page_create(90, 44);
	zpSDK.TextPosWinStyle = false;
	
	zpSDK.zp_page_clear();
//	zpSDK.zp_draw_text_ex(0,20,"有限公司","黑体",3,0,false,false,false);
//	zpSDK.zp_draw_text_ex(17,16.5," 车险小额损失核损单","黑体",3,0,false,false,false);
	if(text!=null){
	zpSDK.zp_draw_barcode(10,0,text[0], BARCODE_TYPE.BARCODE_CODE128, 7, 3, 0) ;
	zpSDK.zp_draw_text_ex(25, 10, text[0], "宋体", 2, 0, false, false, false);
	zpSDK.zp_draw_text_ex(6, 13.5, text[1]+"    "+text[4], "黑体", 2.5, 0, false, false, false);
	zpSDK.zp_draw_text_ex(6, 17, text[2], "黑体", 2.5, 0, false, false, false);
	zpSDK.zp_draw_text_ex(6, 20.5, text[5], "黑体", 2.5, 0, false, false, false);
	zpSDK.zp_draw_text_ex(6, 24, text[6], "黑体", 2.5, 0, false, false, false);
	zpSDK.zp_draw_text_ex(6, 27.5, text[7], "黑体", 2.5, 0, false, false, false);
//	zpSDK.zp_draw_barcode(6,7.4+5,"1234567890", BARCODE_TYPE.BARCODE_CODE128, 6, 3, 0) ;
//	///////////////////////下面全是横线///////////////////////////////////
//	zpSDK.zp_draw_line(6.4, 7.5+5, 82, 7.5+5, 2);  //第一边境线 ，上海上面的线横线
//	zpSDK.zp_draw_line(6.4, 16+5, 82, 16+5, 2);//上海下面的线209上面的线 横线
//	zpSDK.zp_draw_line(6.4, 25+5, 82, 25+5, 2);//目的站上面的线;;;;横线
//	zpSDK.zp_draw_line(6.4, 31+5, 82, 31+5, 2);//10上面的线;;;;   横线
//	zpSDK.zp_draw_line(6.4, 40+5, 82, 40+5, 2);//收货人上面的线;;;;横线
//	zpSDK.zp_draw_line(6.4, 46+5, 82, 46+5, 2);//边框最下面边框线 横线
//	///////////////////////下面全是竖线///////////////////////////////////
//	zpSDK.zp_draw_line(6.4, 7.5+5, 6.4, 46+5, 2);  //第一行竖线;;;;;;;
//	zpSDK.zp_page_print(true);
	}else{
		zpSDK.zp_draw_barcode(10, 0, "A201511051111",  BARCODE_TYPE.BARCODE_CODE128, 7, 2, 0);
		zpSDK.zp_draw_text_ex(20, 10, "A201511051111", "宋体", 2, 0, false, false, false);
		zpSDK.zp_draw_text_ex(6, 12, "测试数据", "黑体", 3, 0, false, false, false);
		zpSDK.zp_draw_text_ex(6, 16, "测试数据", "黑体", 3, 0, false, false, false);
		zpSDK.zp_draw_text_ex(6, 20, "测试数据", "黑体", 3, 0, false, false, false);
		zpSDK.zp_draw_text_ex(6, 24, "测试数据", "黑体", 3, 0, false, false, false);
		zpSDK.zp_draw_text_ex(6, 28, "测试数据", "黑体", 3, 0, false, false, false);
//		zpSDK.zp_draw_text_ex(6, 32, "测试数据", "黑体", 3, 0, false, false, false);
	}
	zpSDK.zp_page_print(false);
	zpSDK.zp_printer_status_detect();
	zpSDK.zp_goto_mark_label(44);
//	zpSDK.zp_goto_mark_right(0);
	zpSDK.zp_page_free();
//	zpSDK.zp_close();
//	PrintSettingActivity.connect = false;
}
}
