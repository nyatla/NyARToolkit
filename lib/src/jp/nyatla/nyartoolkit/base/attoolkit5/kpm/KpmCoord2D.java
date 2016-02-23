package jp.nyatla.nyartoolkit.base.attoolkit5.kpm;

public class KpmCoord2D {
    public double             x;
    public double             y;
	public KpmCoord2D(double i_x, double i_y) {
		this.x=i_x;
		this.y=i_y;
	}
	public KpmCoord2D(){
		
	}
	public static KpmCoord2D[] creaeArray(int num) {
		KpmCoord2D[] a=new KpmCoord2D[num];
		for(int i=0;i<a.length;i++){
			a[i]=new KpmCoord2D();
		}
		return a;
	}
	public void set(double i_x, double i_y) 
	{
		this.x=i_x;
		this.y=i_y;
	}
}
