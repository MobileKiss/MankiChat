package by.mobilekiss.mankichat;

public class ColorGenerator {

	private float pos;

	public void addLen(float len) {
		pos += len;
	}

	private int rgbToInt(float r, float g, float b)
	{
		int rr = (int)(r*255+0.5);
		rr <<= 16;
		int gg = (int)(g*255+0.5);
		gg <<= 8;
		int bb = (int)(b*255+0.5);
		return 0xff000000|rr|gg|bb;
	}
	
	public int getColor() {
		while (pos>6000.0f) pos-=6000.0f;
		float hue = pos/6000.0f;
		int h = (int)(hue * 6);
	    float f = hue * 6 - h;
		float q = 1.0f - f;
	    switch (h) {
	      case 0: return rgbToInt(1.0f, f, 0.0f);
	      case 1: return rgbToInt(q, 1.0f, 0.0f);
	      case 2: return rgbToInt(0.0f, 1.0f, f);
	      case 3: return rgbToInt(0.0f, q, 1.0f);
	      case 4: return rgbToInt(f, 0.0f, 1.0f);
	      case 5: return rgbToInt(1.0f, 0.0f, q);
	    }		
		return 0xFFFFFFFF;
	}

}
