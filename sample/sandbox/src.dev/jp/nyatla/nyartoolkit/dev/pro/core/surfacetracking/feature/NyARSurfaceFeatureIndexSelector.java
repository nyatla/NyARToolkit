package jp.nyatla.nyartoolkit.dev.pro.core.surfacetracking.feature;

import jp.nyatla.nyartoolkit.core.math.NyARLCGsRandomizer;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.NyARFeatureCoordPtrList;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.feature.NyARSurfaceFeatureSet.NyAR2FeatureCoord;

//�?ンプレートを検�?�する�?
public class NyARSurfaceFeatureIndexSelector implements INyARSurfaceFeatureIndex
{
	private static class RepeatedRandomizer extends NyARLCGsRandomizer
	{
		private int _loop_max;
		private int _counter;

		public RepeatedRandomizer(int i_seed, int i_loop_max) {
			super(i_seed);
			this._counter = 0;
			this._loop_max = i_loop_max;

		}

		public int rand() {
			int ret = super.rand();
			this._counter++;
			if (this._counter >= this._loop_max) {
				this._rand_val = this._seed;
				this._counter = 0;
			}
			return ret;
		}
	}	

	private static class SinCos
	{
		public double sin;
		public double cos;
		public boolean ar2GetVectorAngle(NyARDoublePoint2d p1, NyARDoublePoint2d p2)
		{
			double l = Math.sqrt((p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y));
			if (l == 0.0f)
				return false;

			this.sin = (p2.y - p1.y) / l;
			this.cos = (p2.x - p1.x) / l;
			return true;
		}
		
	}

	private static double ar2GetTriangleArea(NyARDoublePoint2d p1, NyARDoublePoint2d p2, NyARDoublePoint2d p3)
	{
		double s = ((p2.x - p1.x) * (p3.y - p1.y) - (p3.x - p1.x) * (p2.y - p1.y)) / 2.0f;
		return s<0?-s:s;
	}
	private static double ar2GetRegionArea(NyARDoublePoint2d i_p0,NyARDoublePoint2d i_q1,NyARDoublePoint2d i_r1,NyARDoublePoint2d i_r2)
	{
		return ar2GetTriangleArea(i_p0, i_q1, i_r1)+ar2GetTriangleArea(i_p0, i_r1, i_r2);
	}		
	
	private static RepeatedRandomizer _rand = new RepeatedRandomizer(0, 128);

	public NyARSurfaceFeatureIndexSelector()
	{
	}

	private static int select0(NyARSurfaceFeatures candidate,int xsize, int ysize)
	{
		int j = -1;
		double dmax = 0.0f;
		for (int i = candidate.getLength()-1; i>=0 ; i--) {
			NyARSurfaceFeatureItem item = candidate.getItem(i);
			// スクリーンの場�?でフィルター
			if (item.x < xsize / 8 || item.x > xsize * 7 / 8 || item.y < ysize / 8 || item.y > ysize * 7 / 8) {
				continue;
			}
			// 中�?から�?番距離のありそうなのを選�?
			double d = (item.x - xsize / 2) * (item.x - xsize / 2) + (item.y - ysize / 2) * (item.y - ysize / 2);
			if (d > dmax) {
				dmax = d;
				j = i;
			}
		}
		return j;
	}
	private static int select1(NyARSurfaceFeatures candidate,int xsize, int ysize,NyARSurfaceFeatureItem i_pos0)
	{
		double dmax = 0;
		int j = -1;
		for (int i = candidate.getLength()-1; i >=0 ; i--) {
			NyARSurfaceFeatureItem item = candidate.getItem(i);
			// スクリーンの場�?でフィルタ
			if (item.x < xsize / 8 || item.x > xsize * 7 / 8 || item.y < ysize / 8 || item.y > ysize * 7 / 8) {
				continue;
			}
			// pos[0]との距離が市�?�の�?�?のを選�?
			double d = (item.x - i_pos0.x) * (item.x - i_pos0.x) + (item.y - i_pos0.y) * (item.y - i_pos0.y);
			if (d > dmax) {
				dmax = d;
				j = i;
			}
		}
		return j;		
	}
	private static int select2(NyARSurfaceFeatures candidate,int xsize, int ysize,NyARSurfaceFeatureItem i_pos0,NyARSurfaceFeatureItem i_pos1)
	{
		double dmax = 0.0f;
		int j = -1;
		for (int i = candidate.getLength()-1; i >=0 ; i--) {
			NyARSurfaceFeatureItem item = candidate.getItem(i);
			if (item.x < xsize / 8 || item.x > xsize * 7 / 8 || item.y < ysize / 8 || item.y > ysize * 7 / 8) {
				continue;
			}
			// なんだこれ。距離は間違�?な�?が�?�・・。pos[0]とpos[1]の両方から�?番離れた奴?�?
			double d = ((item.x - i_pos0.x) * (i_pos1.y - i_pos0.y) - (item.y - i_pos0.y) * (i_pos1.x - i_pos0.x));
			d = d * d;
			if (d > dmax) {
				dmax = d;
				j = i;
			}
		}
		return j;
	}
	private static int select3(NyARSurfaceFeatures candidate,int xsize, int ysize,NyARSurfaceFeatureItem i_pos0,NyARSurfaceFeatureItem i_pos1,NyARSurfaceFeatureItem i_pos2)
	{
		double smax, s;
		SinCos p2sincos = new SinCos();
		SinCos p3sincos = new SinCos();
		SinCos p4sincos = new SinCos();
		p2sincos.ar2GetVectorAngle(i_pos0, i_pos1);
		p3sincos.ar2GetVectorAngle(i_pos0, i_pos2);

		int j = -1;
		smax = 0.0f;
		for (int i = candidate.getLength()-1; i >=0 ; i--) {
			NyARSurfaceFeatureItem item = candidate.getItem(i);
			// スクリーンの?���?
			if (item.x < xsize / 8 || item.x > xsize * 7 / 8 || item.y < ysize / 8 || item.y > ysize * 7 / 8) {
				continue;
			}
			// 謎�?�選択ルール・・・�?
//			pos[3].x = item.x;
//			pos[3].y = item.y;
			p4sincos.ar2GetVectorAngle(i_pos0, item);
			if (((p3sincos.sin * p2sincos.cos - p3sincos.cos * p2sincos.sin) >= 0.0f) && ((p4sincos.sin * p2sincos.cos - p4sincos.cos * p2sincos.sin) >= 0.0f)) {
				if (p4sincos.sin * p3sincos.cos - p4sincos.cos * p3sincos.sin >= 0.0f) {
					s = ar2GetRegionArea(i_pos0,i_pos1,i_pos2,item);
				} else {
					s = ar2GetRegionArea(i_pos0,i_pos1,item,i_pos2);
				}
			} else if (((p4sincos.sin * p3sincos.cos - p4sincos.cos * p3sincos.sin) >= 0.0f)
					&& ((p2sincos.sin * p3sincos.cos - p2sincos.cos * p3sincos.sin) >= 0.0f)) {
				if (p4sincos.sin * p2sincos.cos - p4sincos.cos * p2sincos.sin >= 0.0f) {
					s = ar2GetRegionArea(i_pos0,i_pos2,i_pos1,item);
				}else {
					s = ar2GetRegionArea(i_pos0,i_pos2,item,i_pos1);
				}
			} else if (((p2sincos.sin * p4sincos.cos - p2sincos.cos * p4sincos.sin) >= 0.0f)
					&& ((p3sincos.sin * p4sincos.cos - p3sincos.cos * p4sincos.sin) >= 0.0f)) {
				if (p3sincos.sin * p2sincos.cos - p3sincos.cos * p2sincos.sin >= 0.0f) {
					s = ar2GetRegionArea(i_pos0,item,i_pos1,i_pos2);
				}
				else {
					s = ar2GetRegionArea(i_pos0,item,i_pos2,i_pos1);
				}
			} else {
				continue;
			}
			if (s > smax) {
				smax = s;
				j = i;

			}
		}
		return j;
	}
	/**
	 * インスタンスの状態をリセ�?トする�??
	 */

	/**
	 * o_posの状況に対応して、candidateから候補IDを選択します�??
	 * @param candidate
	 * @param prelog
	 * @param o_pos
	 * @param xsize
	 * @param ysize
	 * @return
	 */
	public int ar2SelectTemplate(NyARSurfaceFeatures candidate, NyARFeatureCoordPtrList prelog,NyARSurfaceFeaturesPtr o_pos,NyARIntSize i_screen_size)
	{
		switch(o_pos.getLength()){
		case 0:
			return select0(candidate,i_screen_size.w,i_screen_size.h);
		case 1:
			return select1(candidate,i_screen_size.w,i_screen_size.h,o_pos.getItem(0));
		case 2:
			return select2(candidate,i_screen_size.w,i_screen_size.h,o_pos.getItem(0),o_pos.getItem(1));
		case 3:
			return select3(candidate,i_screen_size.w,i_screen_size.h,o_pos.getItem(0),o_pos.getItem(1),o_pos.getItem(2));
		default:
			return selectHistory(candidate,prelog);
		}
	}
	
	private static int selectHistory(NyARSurfaceFeatures candidate, NyARFeatureCoordPtrList i_prev_log)
	{
		int j;
		for (int i = 0; i < i_prev_log.getLength(); i++) {
			NyARSurfaceFeatureSet.NyAR2FeatureCoord prev_item = i_prev_log.getItem(i);
			for (j = 0; j < candidate.getLength(); j++) {
				NyARSurfaceFeatureItem item = candidate.getItem(j);
				// 過去ログでも検�?�した形跡があったものを選択する�??
				if (prev_item == item.ref_feature) {
					return j;
				}
			}
		}
		//残ってな�?
		if( candidate.getLength()==0){
			return -1;
		}
		//適当に返す
		int k = (int) ((double) candidate.getLength() * _rand.rand() / (RepeatedRandomizer.RAND_MAX + 1.0f));
		for (int i = j = 0; i < candidate.getLength(); i++) {
			if (j == k) {
				return i;
			}
			j++;
		}
		return -1;
	}
	
}
