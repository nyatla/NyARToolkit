package jp.nyatla.nyartoolkit.core.surfacetracking;

import java.io.InputStream;

import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFsetFile;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftIsetFile;




public class NyARSurfaceDataSet
{
	final public NyARNftIsetFile iset;
	final public NyARNftFsetFile fset;
	public NyARSurfaceDataSet(NyARNftIsetFile i_iset,NyARNftFsetFile i_fset)
	{
		this.iset=i_iset;
		this.fset=i_fset;
	}
	public static NyARSurfaceDataSet loadFromSurfaceFiles(InputStream i_iset_stream,InputStream i_fset_stream)
	{
		NyARNftIsetFile iset=NyARNftIsetFile.loadFromIsetFile(i_iset_stream);
		NyARNftFsetFile fset=NyARNftFsetFile.loadFromFsetFile(i_fset_stream);
		return new NyARSurfaceDataSet(iset,fset);
	}
	
}



