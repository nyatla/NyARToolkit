/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *  Copyright 2013-2015 Daqri, LLC.
 *  Author(s): Chris Broaddus
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 *  Copyright (C)2016 Ryo Iizuka
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
package jp.nyatla.nyartoolkit.core.kpm.matcher.findinliners;

import jp.nyatla.nyartoolkit.core.kpm.matcher.FeaturePairStack;
import jp.nyatla.nyartoolkit.core.kpm.matcher.HomographyMat;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;

/**
 * Optimized {@link FindInliers}
 */
final public class FindInliers_O1 extends FindInliers
{
	public FindInliers_O1(double i_threshold){
		super(i_threshold);
	}
	@Override
	public void extructMatches(HomographyMat H, FeaturePairStack matches)
	{
		int l=matches.getLength();
		FeaturePairStack.Item[] b=matches.getArray();
		double threshold2 = this._threshold2;
		//前方詰め
		int pos=0;
		NyARDoublePoint2d tmp;
		for (int i = 0; i < l; i++) {
			tmp=b[i].ref;
			double w = H.m20 * tmp.x + H.m21 * tmp.y + H.m22;
			double t1 = ((H.m00 * tmp.x + H.m01 * tmp.y + H.m02) / w);// XP
			double t2 = ((H.m10 * tmp.x + H.m11 * tmp.y + H.m12) / w);// YP
			tmp=b[i].query;
			t1-=tmp.x;
			t2-=tmp.y;
			double d2 = (t1*t1)+ (t2*t2);
			if (d2 <= threshold2) {
				matches.swap(i,pos);
				pos++;
			
			}
		}
		matches.setLength(pos);
		return;			
	}
}
