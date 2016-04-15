package jp.nyatla.nyartoolkit.dev.pro.core.surfacetracking;

import java.io.InputStream;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.feature.NyARSurfaceFeatureSet;
import jp.nyatla.nyartoolkit.pro.core.surfacetracking.imageset.NyARSurfaceImageSet;


public class NyARSurfaceDataSet
{
	public NyARNftIsetFile iset;
	public NyARNftFsetFile fset;
	protected NyARSurfaceDataSet(NyARNftIsetFile i_iset,NyARNftFsetFile i_fset) throws NyARRuntimeException
	{
		this.iset=i_iset;
		this.fset=i_fset;
	}
	public static NyARSurfaceDataSet loadFromSurfaceFiles(InputStream i_iset_stream,InputStream i_fset_stream) throws NyARRuntimeException
	{
		NyARNftIsetFile iset=NyARNftIsetFile.loadFromIsetFile(i_iset_stream);
		NyARNftFsetFile fset=NyARNftFsetFile.loadFromFsetFile(i_fset_stream);
		return new NyARSurfaceDataSet(iset,fset);
	}
	
}



