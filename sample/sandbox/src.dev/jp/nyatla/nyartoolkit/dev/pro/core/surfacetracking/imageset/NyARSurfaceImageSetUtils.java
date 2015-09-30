package jp.nyatla.nyartoolkit.dev.pro.core.surfacetracking.imageset;
import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.raster.gs.NyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.rasterdriver.pixel.INyARGsPixelDriver;
import jp.nyatla.nyartoolkit.pro.core.rasterfilter.INyARDefocusFilter;
import jp.nyatla.nyartoolkit.pro.core.rasterfilter.NyARDefocusFilterFactory;

/**
 * ARToolkitNFTのNFTイメージセ�?トを生�?�します�??
 *
 */
public class NyARSurfaceImageSetUtils
{
	/**
	 * 標準的なdpiリス�?
	 */
	public static double[] DEFAULT_DPI_LIST={60,40,20};
	/**
	 * グレースケール画像からNFTイメージセ�?トを生�?�します�??
	 * @param i_src
	 * @param i_dpi
	 * @param i_dpi_list
	 * @return
	 * @throws NyARRuntimeException
	 */
	public static NyARSurfaceImageSet makeImageSet(INyARGrayscaleRaster i_src,double i_dpi,double[] i_dpi_list) throws NyARRuntimeException
	{
		//dpiにあわせた画像セ�?トを作る�?
		INyARGrayscaleRaster[] imgs=makeRasterSet(i_src,i_dpi,i_dpi_list);
		//�?フォーカスとNyARImageSet.ReferenceImageへの変換
		NyARSurfaceImageSet.ReferenceImage[] rimgs=new NyARSurfaceImageSet.ReferenceImage[imgs.length];
		for(int i=0;i<imgs.length;i++){
			int w=imgs[i].getWidth();
			int h=imgs[i].getHeight();
			//�?フォーカス
			INyARGrayscaleRaster tmp=new NyARGrayscaleRaster(w,h);
			INyARDefocusFilter filter=NyARDefocusFilterFactory.createDriver(imgs[i]);
			filter.doFilter(tmp,3);
			//変換
			INyARGsPixelDriver pxd=tmp.getGsPixelDriver();
			rimgs[i]=new NyARSurfaceImageSet.ReferenceImage(w,h,i_dpi_list[i]);
			for(int iy=0;iy<h;iy++){
				for(int ix=0;ix<w;ix++){
					rimgs[i].img[ix+iy*w]=(byte)pxd.getPixel(ix,iy);
				}
			}
		}
		//オブジェクト�?�構�?
		return new NyARSurfaceImageSet(rimgs);
	}
	/**
	 * グレースケール画像セ�?トから�?�NFTイメージセ�?トを生�?�します�??
	 * @param i_src
	 * 通常は{@link #makeRasterSet(INyARGrayscaleRaster, double, double[])}で生�?�した画像セ�?トを渡します�??
	 * @param i_dpi_list
	 * @return
	 * @throws NyARRuntimeException
	 */
	public static NyARSurfaceImageSet makeImageSet(INyARGrayscaleRaster[] i_src,double[] i_dpi_list) throws NyARRuntimeException
	{
		//�?フォーカスとNyARImageSet.ReferenceImageへの変換
		NyARSurfaceImageSet.ReferenceImage[] rimgs=new NyARSurfaceImageSet.ReferenceImage[i_src.length];
		for(int i=0;i<i_src.length;i++){
			int w=i_src[i].getWidth();
			int h=i_src[i].getHeight();
			//�?フォーカス
			INyARGrayscaleRaster tmp=new NyARGrayscaleRaster(w,h);
			INyARDefocusFilter filter=NyARDefocusFilterFactory.createDriver(i_src[i]);
			filter.doFilter(tmp,3);
			//変換
			INyARGsPixelDriver pxd=tmp.getGsPixelDriver();
			rimgs[i]=new NyARSurfaceImageSet.ReferenceImage(w,h,i_dpi_list[i]);
			for(int iy=0;iy<h;iy++){
				for(int ix=0;ix<w;ix++){
					rimgs[i].img[ix+iy*w]=(byte)pxd.getPixel(ix,iy);
				}
			}
		}
		//オブジェクト�?�構�?
		return new NyARSurfaceImageSet(rimgs);
	}
	/**
	 * �?定したdpiセ�?トに対応する画像セ�?トを返します�??
	 * @throws NyARRuntimeException 
	 */
	public static INyARGrayscaleRaster[] makeRasterSet(INyARGrayscaleRaster i_src,double i_dpi,double[] i_dpi_list) throws NyARRuntimeException
	{
		
		int dpi_num=i_dpi_list.length;
		//dpiの�?囲確�?
		if(dpi_num==0){
			throw new NyARRuntimeException();
		}
		for(int i=0;i<i_dpi_list.length;i++){
			if(i_dpi_list[i]>i_dpi){
				throw new NyARRuntimeException();
			}						
		}
		INyARGsPixelDriver src_pixdrv=i_src.getGsPixelDriver();

		INyARGrayscaleRaster[] imgs=new INyARGrayscaleRaster[dpi_num];
		int xsize=i_src.getWidth();
		int ysize=i_src.getHeight();
		for(int i = 0;i<dpi_num;i++){
			int wx = (int)(xsize * i_dpi_list[i] / i_dpi);
			int wy = (int)(ysize * i_dpi_list[i] / i_dpi);
			double wdpi = i_dpi_list[i];
			
			INyARGrayscaleRaster dest=new NyARGrayscaleRaster(wx,wy);
			INyARGsPixelDriver dd=dest.getGsPixelDriver();
			for(int jj = 0; jj < wy; jj++ ) {
				int sy = (int)(jj * i_dpi / wdpi);
				int ey = (int)((jj+1) * i_dpi / wdpi + 0.9999) - 1;
				if( ey >= ysize ){
					ey = ysize - 1;
				}
				for(int  ii = 0; ii < wx; ii++ ) {
					int sx = (int)(ii * i_dpi / wdpi);
					int ex = (int)((ii+1) * i_dpi / wdpi + 0.9999) - 1;
					if( ex >= xsize ){
						ex = xsize - 1;
					}
					int co = 0;
					int bw = 0;
					for(int jjj = sy; jjj <= ey; jjj++ ) {
						for(int iii = sx; iii <= ex; iii++ ) {
							bw+=src_pixdrv.getPixel(iii,jjj);
							co++;
						}
					}
					dd.setPixel(ii,jj,bw/co);
				}
			}
			imgs[i]=dest;
		}
		return imgs;
	}
}
