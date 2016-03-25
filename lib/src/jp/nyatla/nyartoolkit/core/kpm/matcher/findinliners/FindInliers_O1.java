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
