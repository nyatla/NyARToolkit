package jp.nyatla.nyartoolkit.core.marker.nft;

import java.io.InputStream;





public class NyARNftDataSet
{
	final public NyARNftIsetFile iset;
	final public NyARNftFsetFile fset;
	/**
	 * This constructor creates NFT dataset.
	 * @param i_iset
	 * InputStream for reading Imageset source.
	 * @param i_fset
	 * InputStream for reading Featureset source.
	 */
	public NyARNftDataSet(NyARNftIsetFile i_iset,NyARNftFsetFile i_fset)
	{
		this.iset=i_iset;
		this.fset=i_fset;
	}
	/**
	 * NFTファイルセットを格納したストリームからデータを読み出します。
	 * @param i_iset_stream
	 * @param i_fset_stream
	 * @return
	 */
	public static NyARNftDataSet loadFromNftFiles(InputStream i_iset_stream,InputStream i_fset_stream)
	{
		NyARNftIsetFile iset=NyARNftIsetFile.loadFromIsetFile(i_iset_stream);
		NyARNftFsetFile fset=NyARNftFsetFile.loadFromFsetFile(i_fset_stream);
		return new NyARNftDataSet(iset,fset);
	}
	
}



