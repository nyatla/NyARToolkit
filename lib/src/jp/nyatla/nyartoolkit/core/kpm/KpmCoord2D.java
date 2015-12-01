package jp.nyatla.nyartoolkit.core.kpm;

public class KpmCoord2D {
    public float             x;
    public float             y;
	public static KpmCoord2D[] creaeArray(int num) {
		KpmCoord2D[] a=new KpmCoord2D[num];
		for(int i=0;i<a.length;i++){
			a[i]=new KpmCoord2D();
		}
		return a;
	}
}
