package jp.nyatla.nyartoolkit.core.kpm.base;

/*!
@typedef    KpmInputDataSet
@abstract   Data describing the number and location of keypoints in an input image to be matched against a loaded data set.
@discussion
    Key point matching occurs between a loaded data set and a set of keypoints extracted from an input image. This structure
    holds the number and pixel location of keypoints in the input image. The keypoints themselves are an array of 'num'
    KpmRefData structures.
@field		coord Array of pixel locations of the keypoints in an input image.
@field		num Number of coords in the array.
*/
public class KpmInputDataSet {
    public KpmCoord2D[]      coord;
    public int               num;
}
