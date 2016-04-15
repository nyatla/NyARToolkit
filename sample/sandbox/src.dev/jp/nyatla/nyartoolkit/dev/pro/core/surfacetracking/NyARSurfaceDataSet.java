package jp.nyatla.nyartoolkit.dev.pro.core.surfacetracking;

import java.io.InputStream;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.feature.NyARSurfaceFeatureSet;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.imageset.NyARSurfaceImageSet;


public class NyARSurfaceDataSet
{
	public NyARSurfaceImageSet iset;
	public NyARSurfaceFeatureSet fset;
	protected NyARSurfaceDataSet(NyARSurfaceImageSet i_iset,NyARSurfaceFeatureSet i_fset) throws NyARRuntimeException
	{
		this.iset=i_iset;
		this.fset=i_fset;
	}
	public static NyARSurfaceDataSet loadFromSurfaceFiles(InputStream i_iset_stream,InputStream i_fset_stream) throws NyARRuntimeException
	{
		NyARSurfaceImageSet iset=NyARSurfaceImageSet.loadFromIsetFile(i_iset_stream);
		NyARSurfaceFeatureSet fset=NyARSurfaceFeatureSet.loadFromFsetFile(i_fset_stream);
		return new NyARSurfaceDataSet(iset,fset);
	}
	
}



