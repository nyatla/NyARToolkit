package jp.nyatla.nyartoolkit.dev.pro.core.surfacetracking.featuremap;

public class NyARSurfaceFeatureMap {
	public double[] fimage;
	public int width;
	public int height;
	public NyARSurfaceFeatureMap(int i_w,int i_h)
	{
		this.fimage=new double[i_w*i_h];
		this.width=i_w;
		this.height=i_h;
	}
}
