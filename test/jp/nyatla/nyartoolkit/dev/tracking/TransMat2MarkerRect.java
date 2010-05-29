package jp.nyatla.nyartoolkit.dev.tracking;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.*;
import jp.nyatla.nyartoolkit.core.param.NyARPerspectiveProjectionMatrix;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;
import jp.nyatla.nyartoolkit.core.types.NyARIntRect;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;

/**
 * tableの内容から、矩形集合を計算する。
 *
 */
public class TransMat2MarkerRect
{
	private INyARCameraDistortionFactor _dist;
	private NyARPerspectiveProjectionMatrix _prjmat;
	private NyARDoubleMatrix33 _rot_temp=new NyARDoubleMatrix33();
	private NyARDoublePoint3d _pos_temp=new NyARDoublePoint3d();
	private NyARDoublePoint2d _pos2d_tmp=new NyARDoublePoint2d();
	public TransMat2MarkerRect(NyARParam i_param)
	{
		this._dist=i_param.getDistortionFactor();
		this._prjmat=i_param.getPerspectiveProjectionMatrix();
	}
	public int convert(MarkerPositionTable.Item[] i_positions,IntRectStack o_out) throws NyARException
	{
		NyARDoubleMatrix33 rot=this._rot_temp;
		NyARDoublePoint3d pos=this._pos_temp;
		NyARPerspectiveProjectionMatrix prjmat=this._prjmat;
		NyARDoublePoint2d pos2d=this._pos2d_tmp;
		INyARCameraDistortionFactor dist=_dist;
		for(int i=i_positions.length-1;i>=0;i--)
		{
			MarkerPositionTable.Item item=i_positions[i];
			//空の項目をパス
			if(item.is_empty){
				continue;
			}
			//変換行列を計算
			rot.setZXYAngle(item.angle);
			
			//4vertexの２次元座標を計算して、それらを内包する矩形を計算する。
			//[optimize]矩形予測と計算かぶってるよ。
			int n;
			int l,t,r,b;
			int i2=4-1;
			//[3]の座標変換と射影変換、歪み逆補正
			rot.transformVertex(item.offset.vertex[i2], pos);
			pos.x+=item.trans.x;
			pos.y+=item.trans.y;
			pos.z+=item.trans.z;
			prjmat.projectionConvert(pos,pos2d);
			dist.ideal2Observ(pos2d, pos2d);
			l=r=(int)pos2d.x;
			t=b=(int)pos2d.y;
			i2--;
			for(;i2>=0;i2--){
				//[i2]の座標変換と射影変換、歪み逆補正
				rot.transformVertex(item.offset.vertex[i2], pos);
				pos.x+=item.trans.x;
				pos.y+=item.trans.y;
				pos.z+=item.trans.z;
				prjmat.projectionConvert(pos,pos2d);
				dist.ideal2Observ(pos2d, pos2d);
				n=(int)pos2d.x;
				if(l>n){
					l=n;
				}else if(r<n){
					r=n;
				}
				n=(int)pos2d.y;
				if(t>n){
					t=n;
				}else if(b<n){
					b=n;
				}
			}			
			//RECT保存
			NyARIntRect rect=o_out.prePush();
			rect.x=l;
			rect.y=t;
			rect.w=r-l;
			rect.h=b-t;
		}
		return 0;
	}
	public void convert(MarkerPositionTable.Item i_positions,NyARIntRect o_rect) throws NyARException
	{
		NyARDoubleMatrix33 rot=this._rot_temp;
		NyARDoublePoint3d pos=this._pos_temp;
		NyARPerspectiveProjectionMatrix prjmat=this._prjmat;
		NyARDoublePoint2d pos2d=this._pos2d_tmp;
		INyARCameraDistortionFactor dist=_dist;

		//変換行列を計算
		rot.setZXYAngle(i_positions.angle);
		
		//4vertexの２次元座標を計算して、それらを内包する矩形を計算する。
		int n;
		int l,t,r,b;
		int i2=4-1;
		//[3]の座標変換と射影変換、歪み逆補正
		rot.transformVertex(i_positions.offset.vertex[i2], pos);
		pos.x+=i_positions.trans.x;
		pos.y+=i_positions.trans.y;
		pos.z+=i_positions.trans.z;
		prjmat.projectionConvert(pos,pos2d);
		dist.ideal2Observ(pos2d, pos2d);
		l=r=(int)pos2d.x;
		t=b=(int)pos2d.y;
		i2--;
		for(;i2>=0;i2--){
			//[i2]の座標変換と射影変換、歪み逆補正
			rot.transformVertex(i_positions.offset.vertex[i2], pos);
			pos.x+=i_positions.trans.x;
			pos.y+=i_positions.trans.y;
			pos.z+=i_positions.trans.z;
			prjmat.projectionConvert(pos,pos2d);
			dist.ideal2Observ(pos2d, pos2d);
			n=(int)pos2d.x;
			if(l>n){
				l=n;
			}else if(r<n){
				r=n;
			}
			n=(int)pos2d.y;
			if(t>n){
				t=n;
			}else if(b<n){
				b=n;
			}
		}			
		//RECT保存
		o_rect.x=l;
		o_rect.y=t;
		o_rect.w=r-l;
		o_rect.h=b-t;
	}	
}