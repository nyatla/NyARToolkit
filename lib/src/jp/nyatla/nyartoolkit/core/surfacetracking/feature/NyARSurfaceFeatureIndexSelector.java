/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *  Copyright 2015 Daqri, LLC.
 *  Copyright 2006-2015 ARToolworks, Inc.
 *
 *  Author(s): Hirokazu Kato, Philip Lamb
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2016 Ryo Iizuka
 * 
 * NyARToolkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * NyARToolkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and to
 * copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module
 * which is neither derived from nor based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you
 * are not obligated to do so. If you do not wish to do so, delete this exception
 * statement from your version.
 * 
 */
package jp.nyatla.nyartoolkit.core.surfacetracking.feature;

import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFsetFile;
import jp.nyatla.nyartoolkit.core.math.NyARLCGsRandomizer;
import jp.nyatla.nyartoolkit.core.surfacetracking.NyARFeatureCoordPtrList;
import jp.nyatla.nyartoolkit.core.surfacetracking.NyARSurfaceFeatures;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;



//テンプレートを検出する。
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
			// スクリーンの場所でフィルター
			if (item.x < xsize / 8 || item.x > xsize * 7 / 8 || item.y < ysize / 8 || item.y > ysize * 7 / 8) {
				continue;
			}
			// 中心から一番距離のありそうなのを選択
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
			// スクリーンの場所でフィルタ
			if (item.x < xsize / 8 || item.x > xsize * 7 / 8 || item.y < ysize / 8 || item.y > ysize * 7 / 8) {
				continue;
			}
			// pos[0]との距離が市場の遠いのを選択
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
			// なんだこれ。距離は間違いないが・・・。pos[0]とpos[1]の両方から一番離れた奴？
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
			// スクリーンのｒｙ
			if (item.x < xsize / 8 || item.x > xsize * 7 / 8 || item.y < ysize / 8 || item.y > ysize * 7 / 8) {
				continue;
			}
			// 謎の選択ルール・・・。
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
	 * インスタンスの状態をリセットする。
	 */

	/**
	 * o_posの状況に対応して、candidateから候補IDを選択します。
	 * @param candidate
	 * @param prelog
	 * @param o_pos
	 * @param xsize
	 * @param ysize
	 * @return
	 */
	@Override
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
			NyARNftFsetFile.NyAR2FeatureCoord prev_item = i_prev_log.getItem(i);
			for (j = 0; j < candidate.getLength(); j++) {
				NyARSurfaceFeatureItem item = candidate.getItem(j);
				// 過去ログでも検出した形跡があったものを選択する。
				if (prev_item == item.ref_feature) {
					return j;
				}
			}
		}
		//残ってない
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
