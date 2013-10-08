package by.mobilekiss.mankichat;

import android.content.Context;
import android.graphics.Typeface;

public class FontFactory {

	private static Typeface t1;

	public static Typeface getUbuntuNorm(Context c) {

		t1 = Typeface.createFromAsset(c.getAssets(), "Ubuntu-L.ttf");

		return t1;
	}

	private static Typeface t2;

	public static Typeface getUbuntuBold(Context c) {
		if (t2 == null) {
			t2 = Typeface.createFromAsset(c.getAssets(), "Ubuntu-B.ttf");
		}
		return t2;
	}

	private static Typeface t3;

	public static Typeface getIntro(Context c) {
	    if (t3 == null) {
	        t3 = Typeface.createFromAsset(c.getAssets(), "Intro.ttf");
	    }
	    return t3;
	}
}