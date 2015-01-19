package com.garlini.gastosgames.view;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.widget.Toast;

public class ViewUtil {
	
	public static void showInfo(Context contect, String info)
	{
		Toast.makeText(contect, info, Toast.LENGTH_LONG).show();
	}
	
	public static void showValidationError(Context contect, String error)
	{
		Toast.makeText(contect, error, Toast.LENGTH_LONG).show();
	}
	
	public static void showError(Context contect, String error)
	{
		Toast.makeText(contect, error, Toast.LENGTH_LONG).show();
	}
	
	public static float convertDpToPixel(float dp, Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float px = dp * (metrics.densityDpi / 160f);
	    return px;
	}
	
	public static float convertPixelsToDp(float px, Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float dp = px / (metrics.densityDpi / 160f);
	    return dp;
	}

}
