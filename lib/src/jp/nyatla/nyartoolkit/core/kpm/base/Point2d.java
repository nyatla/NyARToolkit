package jp.nyatla.nyartoolkit.core.kpm.base;

public class Point2d {
	public double x;
	public double y;
	public Point2d(){
	}

	public Point2d(double i, double j) {
		this.x=i;
		this.y=j;
	}
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
