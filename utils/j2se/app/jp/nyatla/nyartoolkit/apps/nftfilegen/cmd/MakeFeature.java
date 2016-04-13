package jp.nyatla.nyartoolkit.apps.nftfilegen.cmd;


import java.awt.image.BufferedImage;

import javax.swing.SwingUtilities;

import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFreakFsetFile;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFreakFsetFile.PageInfo;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFreakFsetFile.RefDataSet;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFsetFile;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFsetFile.NyAR2FeatureCoord;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFsetFile.NyAR2FeaturePoints;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftIsetFile;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.gs.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.rgb.INyARRgbRaster;
import jp.nyatla.nyartoolkit.core.rasterdriver.rgb2gs.INyARRgb2GsFilterRgbAve;
import jp.nyatla.nyartoolkit.j2se.NyARBufferedImageRaster;

public class MakeFeature {
	static public class Result
	{
		public Result(NyARNftIsetFile i_iset, NyARNftFsetFile i_fset, NyARNftFreakFsetFile i_fset3)
		{
			this.iset=i_iset;
			this.fset3=i_fset3;
			this.fset=i_fset;
			// TODO Auto-generated constructor stub
		}
		final public NyARNftIsetFile iset;
		final public NyARNftFsetFile fset;
		final public NyARNftFreakFsetFile fset3;
	}
	public interface LogOvserver{
		public void onLog(String i_string);
		public void onFinished(Result result);
	}
	public MakeFeature()
	{
	}
	private CalculateThread _last_ct;
	public void execute(BufferedImage i_src,double i_dpi,int i_lv,double[] i_sub_dpi,LogOvserver i_observer)
	{
		CalculateThread ct=new CalculateThread(i_src,i_dpi,i_lv,i_sub_dpi,i_observer);
		ct.start();
		this._last_ct=ct;
	}
	public static class CalculateThread extends Thread
	{
		public double dpi;
		public int lv;
		public BufferedImage bmp;
		public double[] dpis;
		public LogOvserver _observer;
		public CalculateThread(BufferedImage i_src,double i_dpi,int i_lv,double[] i_sub_dpi,LogOvserver i_ovserver)
		{
			this.dpi=i_dpi;
			this.bmp=i_src;
			this.lv=i_lv;
			this.dpis=i_sub_dpi;
			this._observer=i_ovserver;
		}
		public void run()
		{
			try{
				INyARRgbRaster rgb=new NyARBufferedImageRaster(this.bmp);
				//create same size grayscale raster
				INyARGrayscaleRaster gs=NyARGrayscaleRaster.createInstance(rgb.getSize());
				((INyARRgb2GsFilterRgbAve)rgb.createInterface(INyARRgb2GsFilterRgbAve.class)).convert(gs);
	
				final NyARNftIsetFile iset;
				if(this.dpis!=null){
					iset=NyARNftIsetFile.genImageSet(gs,this.dpi,dpis);
				}else{
					iset=NyARNftIsetFile.genImageSet(gs,this.dpi);
				}
				SwingUtilities.invokeLater(new Runnable() {
	                public void run() {
	        			_observer.onLog(isetReport(iset));
	                }});			
				final NyARNftFsetFile fset;
				fset=NyARNftFsetFile.genFeatureSet(iset,this.lv);

				SwingUtilities.invokeLater(new Runnable() {
	                public void run() {
	                	_observer.onLog(fsetReport(fset));
	                }});			
				final NyARNftFreakFsetFile fset3=NyARNftFreakFsetFile.genFeatureSet3(iset);
				SwingUtilities.invokeLater(new Runnable() {
	                public void run() {
	                	_observer.onLog(fset3Report(fset3));
	                	_observer.onFinished(new MakeFeature.Result(iset,fset,fset3));
	                }});
			}catch(InterruptedException e){
				_observer.onLog("Canceled!");				
			}
			return;
		}
		protected String fset3Report(NyARNftFreakFsetFile i_fset3) {
			StringBuffer b=new StringBuffer();
			b.append("Fset3(FREAK) Infomation\n");
			b.append(String.format("  [No]\tpage_no num=%d\n",i_fset3.page_info.length));
			for(int i=0;i<i_fset3.page_info.length;i++){
				PageInfo pi=i_fset3.page_info[i];
				b.append(String.format("  PageNo=%2d\n",i,pi.page_no));
				b.append(String.format("    (ImageNo)\twidth\theight num=%d\n",pi.image_info.length));
				for(int j=0;j<pi.image_info.length;j++){
					b.append(String.format("    (%2d)\t%d\t%d\n",pi.image_info[j].image_no,pi.image_info[j].w,pi.image_info[j].h));
				}
			}
			b.append(String.format("    [pageNo,ImageNo]\t2dx\t2dy\t3dx\t3dy num=%d\n",i_fset3.ref_point.length));
			for(int i=0;i<i_fset3.ref_point.length;i++){
				RefDataSet rp=i_fset3.ref_point[i];
				b.append(String.format("    [%2d,%2d]\t%f\t%f\t%f\t%f\n",rp.pageNo,rp.refImageNo,rp.coord2D.x,rp.coord2D.y,rp.coord3D.x,rp.coord3D.y));
			}
			return b.toString();
		}
		private String fsetReport(NyARNftFsetFile i_fset)
		{
			StringBuffer b=new StringBuffer();
			b.append(String.format("Fset Infomation num=%d\n",i_fset.list.length));
			b.append("  [No]\tscale\tmaxdpi\tmindpi\n");
			for(int i=0;i<i_fset.list.length;i++){
				NyAR2FeaturePoints fp=i_fset.list[i];
				b.append(String.format("  [%2d]\t%d\t%f\t%f\n",i,fp.scale,fp.maxdpi,fp.mindpi));
				b.append(String.format("    (No)\tsim\tx\ty\tmx\tmy num=%d\n",fp.coord.length));
				for(int j=0;j<fp.coord.length;j++){
					NyAR2FeatureCoord c=fp.coord[j];
					b.append(String.format("    (%2d)\t%.2f\t%d\t%d\t%.3f\t%.3f\n",j,c.maxSim,c.x,c.x,c.mx,c.my));
				}
			}
			return b.toString();
		}
		private static String isetReport(NyARNftIsetFile i_iset)
		{
			StringBuffer b=new StringBuffer();
			b.append("Iset Infomation\n");
			b.append(String.format("  [No]\tdpi\twidth\theight num=%d\n",i_iset.items.length));
			for(int i=0;i<i_iset.items.length;i++){
				NyARNftIsetFile.ReferenceImage ri=i_iset.items[i];
				b.append(String.format("  [%2d]\t%.2f\t%d\t%d\n",i,ri.dpi,ri.width,ri.height));
			}
			return b.toString();
		}
	}
	public void interrupt() {
		this._last_ct.interrupt();
	}

}
