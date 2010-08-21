package jp.nyatla.nyartoolkit.dev.hierarchicallabeling;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

/**
 * ピラミッド画像のための、階層構造のRECTを保持するクラスです。
 * 
 *
 */
public class QsHsHierachyRectMap
{
	public NyARIntSize window_size=new NyARIntSize();
	/**
	 * 解像度のインデクス(1/2^n,1/(n-1)^2,....1)の順で並んでる。
	 */
	public int[] depth_index;
	/**
	 * 解像度毎の先頭インデクス
	 */
	public int[] resolution;
	/**
	 * 解像度の深度(コンストラクタで指定した者と同じ)
	 */
	public int resulution_depth;
	/**
	 * ルートRECT
	 */
	public HierarchyRect top_image;
	
	/**
	 * 階層RECTの実体
	 */
	private HierarchyRect[] _buf;
	
	/**
	 * 
	 * @param i_target_w
	 * 画像サイズ
	 * @param i_target_h
	 * 画像サイズ
	 * @param i_depth
	 * 分解能値(2^n形式)
	 * @throws NyARException
	 */
	public QsHsHierachyRectMap(int i_target_w,int i_target_h,int i_depth) throws NyARException
	{
		initInstance(i_target_w,i_target_h,i_depth);
		return;
	}
	public HierarchyRect[] getRectBuf()
	{
		return this._buf;
	}

	/**
	 * i_rectに階層構造を設定します。この関数は、initInstanceの一部で働きます。
	 * @param i_rect
	 * @param i_index
	 * 求めるRECTのindex
	 * @param i_depth
	 * @param i_a1
	 * 現階層の開始インデクス
	 * @param i_a2
	 * 次の階層の開始点
	 * @param i_d1
	 */
	private void setHierarchy(HierarchyRect i_rect,int i_index,int i_a1,int i_depth)
	{
		if(i_rect.ref_children!=null){
			return;
		}
		if(i_depth<=1){
			i_rect.ref_children=null;
			return;
		}
		int a1=this.depth_index[i_a1];
		int a2=this.depth_index[i_a1+1];
		int d1=this.resolution[i_a1];
		int d2=this.resolution[i_a1+1];
		int this_index=i_index-a1;
		i_rect.ref_children=new HierarchyRect[9];
		for(int y=0;y<3;y++){
			for(int x=0;x<3;x++){
				int idx=a2+((this_index%d1)*2+x)+((this_index/d1)*2+y)*d2;
				i_rect.ref_children[x+y*3]=this._buf[idx];
				setHierarchy(i_rect.ref_children[x+y*3],idx,i_a1+1,i_depth-1);
			}
		}
		return;
	}
	/**
	 * 任意サイズの矩形から、QuadSizeHalfShiftの矩形ツリーを作ります。画像が2の階乗でない場合、
	 * 解像度の2^(depth-1)の余剰分は切り捨てられます。
	 * @param i_target_w
	 * @param i_target_h
	 * @param i_depth
	 * 矩形階層の深さを指定します。
	 */
	private void initInstance(int i_target_w,int i_target_h,int i_depth)
	{
		this.resulution_depth=i_depth;
		this.depth_index=new int[i_depth];
		this.resolution=new int[i_depth];

		//resolutionインデクスと矩形要素の合計値を計算
		//1+9+49+・・・
		int number_of_data=0;
		int ls=1;
		int c=1;
		int div_pow=i_depth-1;
		for(int i=0;i<=div_pow;i++){
			this.resolution[i]=ls;
			this.depth_index[i]=number_of_data;
			number_of_data+=ls*ls;
			c*=2;
			ls+=c;
		}

		HierarchyRect[] buf=new HierarchyRect[number_of_data];
		for(int i=0;i<number_of_data;i++){
			buf[i]=new HierarchyRect();
			buf[i].id=i;
		}
		
		int div=(int)Math.pow(2,div_pow);
		//ターゲット範囲を決める(端数は端に分散)
		int target_w=i_target_w-i_target_w%div;
		int target_h=i_target_h-i_target_h%div;
		int target_t=i_target_w%div/2;
		int target_l=i_target_h%div/2;
		
		int window_w=target_w/div;
		int window_h=target_h/div;

		//矩形のパラメータを定義
		int ptr;
		int lc=1;
		ls=1;
		ptr=0;
		int skip_bit=div;
		for(int i=0;i<=div_pow;i++){
			for(int y=0;y<lc;y++){
				for(int x=0;x<lc;x++){
					buf[ptr].dot_skip=skip_bit;
					buf[ptr].x=target_l+(window_w*skip_bit/2)*x;
					buf[ptr].y =target_t+(window_h*skip_bit/2)*y;
					buf[ptr].w=window_w*skip_bit;
					buf[ptr].h=window_h*skip_bit;
					ptr++;
				}
			}
			skip_bit/=2;
			ls*=2;
			lc+=ls;
		}
		this._buf=buf;
		setHierarchy(this._buf[0],0,0,i_depth);
		//結果を記録
		this.window_size.setValue(window_w,window_h);
		this.top_image=buf[0];
	}
}
