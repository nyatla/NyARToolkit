package jp.nyatla.nyartoolkit.core.marker.nft;

import java.io.File;
import java.io.InputStream;

import jp.nyatla.nyartoolkit.core.kpm.keyframe.KeyframeMap;
import jp.nyatla.nyartoolkit.core.surfacetracking.NyARSurfaceDataSet;




/**
 * ARTOOLKIT NFTの特徴点データセットを格納するクラス
 */
public class NyARNftDataSet
{
	final public NyARSurfaceDataSet surface_dataset;
	final public KeyframeMap freak_fset;
	
	/**
	 * 
	 * @param i_iset
	 * @param i_fset
	 * @param i_freak_fset
	 * @param i_freak_fset_page_id
	 */
	public NyARNftDataSet(NyARNftIsetFile i_iset,NyARNftFsetFile i_fset,NyARNftFreakFsetFile i_freak_fset,int i_freak_fset_page_id)
	{
		this.surface_dataset=new NyARSurfaceDataSet(i_iset,i_fset);
		this.freak_fset = new KeyframeMap(i_freak_fset,i_freak_fset_page_id);
	}
	/**
	 * 3種類のファイルに対応した入力ストリームから、特徴データを読み出します。
	 * @param i_iset_stream
	 * @param i_fset_stream
	 * @param i_fset3_stream
	 * @return
	 */
	public static NyARNftDataSet loadFromNftFiles(InputStream i_iset_stream,InputStream i_fset_stream,InputStream i_fset3_stream,int i_freak_fset_page_id)
	{
		NyARNftIsetFile iset=NyARNftIsetFile.loadFromIsetFile(i_iset_stream);
		NyARNftFsetFile fset=NyARNftFsetFile.loadFromFsetFile(i_fset_stream);
		NyARNftFreakFsetFile fset3 = NyARNftFreakFsetFile.loadFromfset3File(i_fset3_stream);
		
		return new NyARNftDataSet(iset,fset,fset3,i_freak_fset_page_id);
	}
	/**
	 * 拡張子の異なる3つの特徴量ファイル(iset,fset,fset3)から特徴量データを読みだして、インスタンスを作成します。
	 * @param i_fname_prefix
	 * @param i_freak_fset_page_id
	 * fset3のページIDです。
	 * @return
	 */
	public static NyARNftDataSet loadFromNftFiles(String i_fname_prefix,int i_freak_fset_page_id)
	{
		NyARNftIsetFile iset=NyARNftIsetFile.loadFromIsetFile(new File(i_fname_prefix+".iset"));
		NyARNftFsetFile fset=NyARNftFsetFile.loadFromFsetFile(new File(i_fname_prefix+".fset"));
		NyARNftFreakFsetFile fset3 = NyARNftFreakFsetFile.loadFromfset3File(new File(i_fname_prefix+".fset3"));
		return new NyARNftDataSet(iset,fset,fset3,i_freak_fset_page_id);
	}
	/**
	 * {@link #loadFromNftFiles(String,int)}の第二パラメータが0のものと同じです。
	 * @param i_fname_prefix
	 * @return
	 */
	public static NyARNftDataSet loadFromNftFiles(String i_fname_prefix)
	{
		return loadFromNftFiles(i_fname_prefix,0);
	}
	
}



