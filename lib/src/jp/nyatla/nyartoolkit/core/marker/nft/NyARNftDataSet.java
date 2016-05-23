/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *  Copyright 2015 Daqri, LLC.
 *  Copyright 2006-2015 ARToolworks, Inc.
 *
 *  Author(s): Hirokazu Kato, Philip Lamb
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2016 Ryo Iizuka
 * 
 * NyARToolkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * NyARToolkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and to
 * copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module
 * which is neither derived from nor based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you
 * are not obligated to do so. If you do not wish to do so, delete this exception
 * statement from your version.
 * 
 */
package jp.nyatla.nyartoolkit.core.marker.nft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.KeyframeMap;
import jp.nyatla.nyartoolkit.core.surfacetracking.NyARSurfaceDataSet;




/**
 * ARTOOLKIT NFTの特徴点データセットを格納するクラス
 */
public class NyARNftDataSet
{
	final public NyARSurfaceDataSet surface_dataset;
	final public KeyframeMap freak_fset;
	private static void scaling(NyARNftIsetFile i_iset,NyARNftFsetFile i_fset,NyARNftFreakFsetFile i_freak_fset,int i_freak_fset_page_id,double i_width_in_msec)
	{
		//比率計算
		double new_dpi=(i_iset.items[0].width/i_width_in_msec)*25.4;
		double dpi_rate=(new_dpi/i_iset.items[0].dpi);
		//isetの更新(dpiを)
		for(int i=0;i<i_iset.items.length;i++){
			i_iset.items[i].dpi*=dpi_rate;
		}
		//fsetの更新
		for(int i=0;i<i_fset.list.length;i++){
			i_fset.list[i].maxdpi*=dpi_rate;
			i_fset.list[i].mindpi*=dpi_rate;
			//ピクセル値に戻す係数		
			for(NyARNftFsetFile.NyAR2FeatureCoord j:i_fset.list[i].coord){
				j.mx=j.mx/dpi_rate;
				j.my=j.my/dpi_rate;
			}
		}
		//fset3の更新
		for(int i=0;i<i_freak_fset.ref_point.length;i++){
			i_freak_fset.ref_point[i].coord3D.x/=dpi_rate;
			i_freak_fset.ref_point[i].coord3D.y/=dpi_rate;
		}
	}	
	/**
	 * コンストラクタです。
	 * ファイルイメージからデータセットを生成します。
	 * @param i_iset
	 * @param i_fset
	 * @param i_freak_fset
	 * @param i_freak_fset_page_id
	 */
	public NyARNftDataSet(NyARNftIsetFile i_iset,NyARNftFsetFile i_fset,NyARNftFreakFsetFile i_freak_fset,int i_freak_fset_page_id,double i_width_in_msec)
	{
		if(!Double.isNaN(i_width_in_msec)){
			scaling(i_iset,i_fset,i_freak_fset,i_freak_fset_page_id,i_width_in_msec);
		}		
		this.surface_dataset=new NyARSurfaceDataSet(i_iset,i_fset);
		this.freak_fset = new KeyframeMap(i_freak_fset,i_freak_fset_page_id);
	}

	
	/**
	 * 3種類のファイルに対応した入力ストリームから、特徴データを読み出します。
	 * @param i_iset_stream
	 * @param i_fset_stream
	 * @param i_fset3_stream
	 * @param i_freak_fset_page_id
	 * @param i_width_in_msec
	 * NFTターゲット画像の横幅をmmで指定します。スケーリングが不要な場合はNaNを指定します。
	 * @return
	 */
	public static NyARNftDataSet loadFromNftFiles(InputStream i_iset_stream,InputStream i_fset_stream,InputStream i_fset3_stream,int i_freak_fset_page_id,double i_width_in_msec)
	{
		return new NyARNftDataSet(
			NyARNftIsetFile.loadFromIsetFile(i_iset_stream),
			NyARNftFsetFile.loadFromFsetFile(i_fset_stream),
			NyARNftFreakFsetFile.loadFromfset3File(i_fset3_stream),
			i_freak_fset_page_id,i_width_in_msec);
	}

	/**
	 * 拡張子の異なる3つの特徴量ファイル(iset,fset,fset3)から特徴量データを読みだして、インスタンスを作成します。
	 * @param i_fname_prefix
	 * @param i_freak_fset_page_id
	 * fset3のページIDです。
	 * @return
	 */
	public static NyARNftDataSet loadFromNftFiles(String i_fname_prefix,int i_freak_fset_page_id,double i_width_in_msec)
	{
		try {
			return loadFromNftFiles(
				new FileInputStream(new File(i_fname_prefix+".iset")),
				new FileInputStream(new File(i_fname_prefix+".fset")),
				new FileInputStream(new File(i_fname_prefix+".fset3")),i_freak_fset_page_id,i_width_in_msec);
		} catch (FileNotFoundException e) {
			throw new NyARRuntimeException(e);
		}
	}
	/**
	 * {@link #loadFromNftFiles(String,int)}の第二パラメータが0のものと同じです。
	 * @param i_fname_prefix
	 * @return
	 */
	public static NyARNftDataSet loadFromNftFiles(String i_fname_prefix)
	{
		return loadFromNftFiles(i_fname_prefix,Double.NaN);
	}
	public static NyARNftDataSet loadFromNftFiles(String i_fname_prefix,double i_width_in_msec)
	{
		return loadFromNftFiles(i_fname_prefix,0,i_width_in_msec);
	}
	public static NyARNftDataSet loadFromNftDataSet(InputStream i_stream,double i_width_in_msec)
	{		
		NyARNftDataSetFile nfp;
		nfp = NyARNftDataSetFile.loadFromNftFilePack(i_stream);
		return new NyARNftDataSet(nfp.iset,nfp.fset,nfp.fset3,0,i_width_in_msec);		
	}
	public static NyARNftDataSet loadFromNftDataSet(String i_fname,double i_width_in_msec)
	{		
		try {
			return loadFromNftDataSet(new FileInputStream(new File(i_fname)),i_width_in_msec);
		} catch (FileNotFoundException e) {
			throw new NyARRuntimeException(e);
		}		
	}
	public static NyARNftDataSet loadFromNftDataSet(String i_fname)
	{	
		return loadFromNftDataSet(i_fname,Double.NaN);
	}
	public static void main(String[] args){
	}
	
}



