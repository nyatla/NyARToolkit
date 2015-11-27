package jp.nyatla.nyartoolkit.core.kpm;

public class Point2d {
	public float x;
	public float y;
	public static Point2d[] createArray(int i_size){
		Point2d[] r=new Point2d[i_size];
		for(int i=0;i<r.length;i++){
			r[i]=new Point2d();
		}
		return r;
	}
	public void set(Point2d first_xp1) {
		this.x=first_xp1.x;
		this.y=first_xp1.y;
		
	}
}
