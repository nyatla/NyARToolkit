package jp.nyatla.nyartoolkit.core.kpm;
/*!
@typedef    KpmRefDataSet
@abstract   A loaded dataset for KPM tracking.
@discussion
    Key point matching takes as input a reference data set of points. This structure holds a set of points in memory prior to loading into the tracker.
@field		refPoint Tracking reference points.
@field		num Number of refPoints in the dataset.
@field		pageInfo Array of info about each page in the dataset. One entry per page.
@field		pageNum Number of pages in the dataset (i.e. a count, not an index).
*/
public class KpmRefDataSet {
    KpmRefData       refPoint;
    int               num;
    KpmPageInfo      pageInfo;
    int               pageNum;
}
