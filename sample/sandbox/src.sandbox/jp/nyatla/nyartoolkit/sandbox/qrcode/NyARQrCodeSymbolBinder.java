package jp.nyatla.nyartoolkit.sandbox.qrcode;

import jp.nyatla.nyartoolkit.core.*;
import jp.nyatla.nyartoolkit.core.squaredetect.NyARSquare;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.core.param.*;
/**
 * QRコードのシンボルを結びつける偉いクラス
 * アルゴリズムはこんな感じ。
 * 1.3シンボルの位置関係から中間のシンボルを探す。
 * 2.中間シンボルの内角点を探す
 * 3.残りの２シンボル間の最短距離の頂点セットを見つけて、それぞれの内角点を探す
 * 4.３個の内角点が決まったら、各シンボルごとに外角点（反対側の頂点）を特定する。
 * 5.対角のシンボルの外角頂点から伸びる線分を合成して、矩形を決める。
 * 6.矩形が決まったら、方程式を解いて交点を出して、頂点にする。
 * 7.交点と中央のシンボルの位置関係から、正しい計算が行われたかを判定（まだ実装してない）
 * 
 * 
 * この方法は浅い角度でシンボル集合を見たときに、1や3の手順が高い確率で失敗する。
 * その場合計算が途中で破綻するのでわかる（はず）
 * 他の方法もあるけど、それはまた今度。
 */
public class NyARQrCodeSymbolBinder
{
	private NyARCameraDistortionFactor _distfactor;

	public NyARQrCodeSymbolBinder(NyARCameraDistortionFactor i_ref_distortion)
	{
		this._distfactor=i_ref_distortion;
		return;
	}
	/**
	 * 最小の三角形を構成する頂点セットを得る
	 * @param i_s0
	 * @param i_s1
	 * @param i_s2
	 * @param o_vertex
	 */
	private static void getMinimumTriangleVertex(NyARSquare[] i_sqare,int[] o_vertex_id)
	{
		//辺の長さが最小になる頂点の組合せを探す
		int d;
		int x,y;
		int dmax=0x7fffffff;
		final NyARIntPoint2d[] vertex0=i_sqare[0].imvertex;
		final NyARIntPoint2d[] vertex1=i_sqare[1].imvertex;
		final NyARIntPoint2d[] vertex2=i_sqare[2].imvertex;
		for(int i=0;i<4;i++)
		{
			for(int i2=0;i2<4;i2++)
			{
				for(int i3=0;i3<4;i3++){
					x=vertex0[i].x-vertex2[i3].x;
					y=vertex0[i].y-vertex2[i3].y;
					d=x*x+y*y;
					x=vertex1[i2].x-vertex2[i3].x;
					y=vertex1[i2].y-vertex2[i3].y;
					d+=x*x+y*y;
					x=vertex1[i2].x-vertex0[i].x;
					y=vertex1[i2].y-vertex0[i].y;
					d+=x*x+y*y;
					if(d<dmax){
						dmax=d;
						o_vertex_id[0]=i;					
						o_vertex_id[1]=i2;
						o_vertex_id[2]=i3;
					}
				}
			}
		}
		return;
	}
	/**
	 * 2矩形の頂点距離が最低の組合せを探す
	 * @param i_sqare
	 * @param o_vertex_id
	 */
	private static void getMinimumLineVertex(NyARIntPoint2d[] i_sqare0,NyARIntPoint2d[] i_sqare1,int[] o_vertex_id)
	{
		//辺の長さが最小になる頂点の組合せを探す
		int d;
		int x,y;
		int dmax=0x7fffffff;
		for(int i=0;i<4;i++)
		{
			for(int i2=0;i2<4;i2++)
			{
				x=i_sqare1[i2].x-i_sqare0[i].x;
				y=i_sqare1[i2].y-i_sqare0[i].y;
				d=x*x+y*y;
				if(d<dmax){
					dmax=d;
					o_vertex_id[0]=i;					
					o_vertex_id[1]=i2;
				}
			}
		}
		return;
	}
	/**
	 * シンボルグループの重心を計算する
	 * @param i_sqare
	 * @param i_center
	 */
	private void getSymbolGroupCenter(NyARSquare[] i_sqare,NyARIntPoint2d i_center)
	{
		//シンボルグループの重心を計算
		int cx,cy;
		cx=cy=0;
		for(int i=0;i<3;i++)
		{
			final NyARIntPoint2d[] sq_ptr=i_sqare[i].imvertex;
			cx+=sq_ptr[0].x;			
			cx+=sq_ptr[1].x;			
			cx+=sq_ptr[2].x;			
			cx+=sq_ptr[3].x;			
			cy+=sq_ptr[0].y;			
			cy+=sq_ptr[1].y;			
			cy+=sq_ptr[2].y;			
			cy+=sq_ptr[3].y;			
		}
		i_center.x=cx/12;
		i_center.y=cy/12;	
		return;
	}
	/**
	 * キーシンボルのインデックスを得る
	 * @param i_sqare
	 * @param i_vertex_id
	 * 最小三角形の頂点IDセット
	 * @return
	 */
	private static int getKeySymble(NyARSquare[] i_sqare,NyARIntPoint2d i_center,int[] i_vertex_id)
	{
		//シンボルグループの重心を計算
		final int cx=i_center.x;
		final int cy=i_center.y;	
		//前段で探した頂点候補のうち、最も重心に近いものが中心シンボルの内対角点
		int key_symble_idx=0;
		int x=i_sqare[0].imvertex[i_vertex_id[0]].x-cx;
		int y=i_sqare[0].imvertex[i_vertex_id[0]].y-cy;
		int dmax=x*x+y*y;
		for(int i=1;i<3;i++){
			x=i_sqare[i].imvertex[i_vertex_id[i]].x-cx;
			y=i_sqare[i].imvertex[i_vertex_id[i]].y-cy;
			final int d=x*x+y*y;
			if(d<dmax){
				dmax=d;
				key_symble_idx=i;
			}
		}
		return key_symble_idx;
	}
	private NyARDoublePoint2d __bindSquare_ideal_vertex=new NyARDoublePoint2d();
	/**
	 * 2つの対角にある矩形から、それらを対角とする矩形を作る。
	 * @param i_sq1
	 * @param i_lv1
	 * @param i_sq2
	 * @param i_lv2
	 */
	private void bindSquare(NyARSquare i_sq1,int i_lv1,NyARSquare i_sq2,int i_lv2,NyARSquare o_qr_square)
	{
		//4辺の式を計算
		o_qr_square.line[0].copyFrom(i_sq1.line[(i_lv1+3)%4]);
		o_qr_square.line[1].copyFrom(i_sq1.line[(i_lv1+0)%4]);
		o_qr_square.line[2].copyFrom(i_sq2.line[(i_lv2+3)%4]);
		o_qr_square.line[3].copyFrom(i_sq2.line[(i_lv2+0)%4]);
		//歪み無しの座標系を計算
		final NyARDoublePoint2d[] l_sqvertex = o_qr_square.sqvertex;
		final NyARIntPoint2d[] imvertex_ptr = o_qr_square.imvertex;

		final NyARLinear[] l_line = o_qr_square.line;
		final NyARDoublePoint2d ideal_vertex=this.__bindSquare_ideal_vertex;
		for (int i = 0; i < 4; i++) {
			final NyARLinear l_line_i = l_line[i];
			final NyARLinear l_line_2 = l_line[(i + 3) % 4];
			final double w1 = l_line_2.dy * l_line_i.dx - l_line_i.dy * l_line_2.dx;
			if (w1 == 0.0) {
				return;
			}
			l_sqvertex[i].x = (l_line_2.dx * l_line_i.c - l_line_i.dx * l_line_2.c) / w1;
			l_sqvertex[i].y = (l_line_i.dy * l_line_2.c - l_line_2.dy * l_line_i.c) / w1;
			_distfactor.ideal2Observ(l_sqvertex[i], ideal_vertex);
			//Ideal→observに変換して、画面上の座標とする。
			imvertex_ptr[i].x=(int)l_sqvertex[i].x;
			imvertex_ptr[i].y=(int)l_sqvertex[i].y;
		}	
//		Graphics g=this.bimg.getGraphics();
//		g.setColor(Color.red);
//		int[] x=new int[4];
//		int[] y=new int[4];
//		for(int i=0;i<4;i++){
//			x[i]=(int)l_sqvertex[i].x;
//			y[i]=(int)l_sqvertex[i].y;
//		}
//		g.drawPolygon(x,y,4);
		return;
		//基準点はVertexをそのまま採用
		//２個の想定点は座標を逆変換して設定
	}
	/**
	 * directionはキーシンボルのインデックスでARToolKitの頂点座標じゃないので注意すること。
	 * @param i_sq
	 * @param o_sq
	 * @return
	 */
	public boolean composeSquare(NyARSquare[] i_sq,NyARSquare o_sq)
	{
		int[] minimum_triangle_vertex=new int[3];
		int[] minimum_line_vertex=new int[2];
		
		NyARIntPoint2d center=new NyARIntPoint2d();

		//辺の長さが最小になる頂点の組合せを探す
		getMinimumTriangleVertex(i_sq,minimum_triangle_vertex);
		
		//中心位置を計算する。
		getSymbolGroupCenter(i_sq,center);
		
		//キーシンボルのインデクス番号を得る
		int key_simble_idx=getKeySymble(i_sq,center,minimum_triangle_vertex);
		
		//対角シンボルのインデックス番号を決める
		int symbol_e1_idx=(key_simble_idx+1)%3;
		int symbol_e2_idx=(key_simble_idx+2)%3;
		
		//対角シンボル間で最短距離を取る頂点ペアを取る
		//(角度を低くするとエラーが出やすい。対角線との類似性を確認する方法のほうがいい。多分)
		getMinimumLineVertex(i_sq[symbol_e1_idx].imvertex,i_sq[symbol_e2_idx].imvertex,minimum_line_vertex);
		
		//内対角を外対角に変換
		int lv1=(minimum_line_vertex[0]+2)%4;
		int lv2=(minimum_line_vertex[1]+2)%4;
		int kv =(minimum_triangle_vertex[key_simble_idx]+2)%4;
		//矩形の合成
		bindSquare(i_sq[symbol_e1_idx],lv1,i_sq[symbol_e2_idx],lv2,o_sq);
		
		//方位判定		
		int direction=getDirection(o_sq,i_sq[key_simble_idx].imvertex[kv],center);
		if(direction==-1){
			return false;
		}
		o_sq.direction=direction;
		
		return true;
	}	
	/**
	 * この関数はあんまり頂点ズレがひどいと失敗する
	 * @param i_square
	 * @param i_vertex
	 * @param i_center
	 * @return
	 */
	private int getDirection(NyARSquare i_square,NyARIntPoint2d i_vertex,NyARIntPoint2d i_center)
	{
		//開始点(中央シンボル)までの頂点のシフト数を決める
		int x,y;
		x=i_square.imvertex[0].x-i_vertex.x;
		y=i_square.imvertex[0].y-i_vertex.y;
		int v1=x*x+y*y;
		x=i_square.imvertex[2].x-i_vertex.x;
		y=i_square.imvertex[2].y-i_vertex.y;
		int v2=x*x+y*y;
		int shift;
		int v;
		if(v1<v2){
			shift=0;
			v=v1;
		}else{
			shift=2;
			v=v2;
		}
		//小さい方の対角線が64(8x8)より大きくずれてたら認識ミスとみなす
		if(v>64){
			return -1;
		}
		//シンボルがどの象限にあるか確認する
		x=i_vertex.x=i_center.x;
		y=i_vertex.y=i_center.y;
		int dir;
		if(x<0){
			dir=2;//dir=y<0?1:2;
		}else{
			dir=4;//dir=y<0?3:4;
		}
		return (dir+shift)%4;
	}
	
	
	
}