package jp.nyatla.nyartoolkit.dev.hierarchicallabeling;

import jp.nyatla.nyartoolkit.core.types.NyARIntRect;

public class TrackingUtils
{
	/**
	 * 矩形のsqNormを定義します。4頂点の平行移動距離^2の合計です。
	 * dist=(x1-x2)^2+(y1-y2)^2*+((x1+w1)-(x2+w2))^2+(y1-y2)^2+(x1-x2)^2+((y1+h1)-(y2+h2))^2+((x1+w1)-(x2+w2))^2+((y1+h1)-(y2+h2))^2
	 * @param i_rect1
	 * @param i_rect2
	 * @return
	 */
	public static int rectSqNorm(NyARIntRect i_rect1,NyARIntRect i_rect2)
	{
		//dist=Σ(p1-p2)^2
		int dx,dy,dw,dh;
		//dist=2*((x1-x2)^2+(y1-y2)^2+(((x1-x2)+(w1-w2))^2)+(((y1-y2)+(h1-h2))^2))
		dx=i_rect1.x-i_rect2.x;
		dy=i_rect1.y-i_rect2.y;
		dw=i_rect1.w-i_rect2.w;
		dh=i_rect1.h-i_rect2.h;				
		return 2*(dx*dx+dy*dy+((dx+dw)*(dx+dw))+((dy+dh)*(dy+dh)));
	}
}
