package jp.nyatla.nyartoolkit.core.kpm.matcher.findinliners;

import jp.nyatla.nyartoolkit.core.kpm.matcher.FeaturePairStack;
import jp.nyatla.nyartoolkit.core.kpm.matcher.HomographyMat;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;

/**
 * Find the inliers given a homography and a set of correspondences.
 */
public class FindInliers
{
	final protected double _threshold2;
	public FindInliers(double i_threshold){
		this._threshold2=i_threshold*i_threshold;
	}
	public void extructMatches(HomographyMat H, FeaturePairStack matches)
	{
		double threshold2 = this._threshold2;
		NyARDoublePoint2d xp = new NyARDoublePoint2d();// float xp[2];
		//前方詰め

		int pos=0;
		for (int i = 0; i < matches.getLength(); i++) {
			H.multiplyPointHomographyInhomogenous(matches.getItem(i).ref.x,matches.getItem(i).ref.y, xp);
			double t1=xp.x- matches.getItem(i).query.x;
			double t2=xp.y- matches.getItem(i).query.y;

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
