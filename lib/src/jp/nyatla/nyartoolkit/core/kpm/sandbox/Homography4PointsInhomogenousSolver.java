/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2012 Ryo Iizuka
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.kpm.sandbox;

import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.matrix.*;

public class Homography4PointsInhomogenousSolver
{
	/**
	 * バグってる。0,0-10,10の同一変換でしくじる。
	 * 
	 */
	public Homography4PointsInhomogenousSolver()
	{
		return;
	}
	public boolean solve(NyARDoubleMatrix33 Hm, NyARDoublePoint2d x1,
			NyARDoublePoint2d x2, NyARDoublePoint2d x3, NyARDoublePoint2d x4, NyARDoublePoint2d xp1, NyARDoublePoint2d xp2,
			NyARDoublePoint2d xp3, NyARDoublePoint2d xp4)
	{		
		NyARDoubleMatrix44 mat_y=new NyARDoubleMatrix44();
		mat_y.m00=x1.x;	mat_y.m01=x1.y;	mat_y.m02=-xp1.y * x1.x;	mat_y.m03=-xp1.y * x1.y;
		mat_y.m10=x2.x;	mat_y.m11=x2.y;	mat_y.m12=-xp2.y * x2.x;	mat_y.m13=-xp2.y * x2.y;
		mat_y.m20=x3.x;	mat_y.m21=x3.y;	mat_y.m22=-xp3.y * x3.x;	mat_y.m23=-xp3.y * x3.y;
		mat_y.m30=x4.x;	mat_y.m31=x4.y;	mat_y.m32=-xp4.y * x4.x;	mat_y.m33=-xp4.y * x4.y;
		if(!mat_y.inverse(mat_y)){
			return false;
		}

		NyARDoubleMatrix44 mat_x=new NyARDoubleMatrix44();
		mat_x.m00=x1.x;	mat_x.m01=x1.y;	mat_x.m02=-xp1.x * x1.x;	mat_x.m03=-xp1.x * x1.y;
		mat_x.m10=x2.x;	mat_x.m11=x2.y;	mat_x.m12=-xp2.x * x2.x;	mat_x.m13=-xp2.x * x2.y;
		mat_x.m20=x3.x;	mat_x.m21=x3.y;	mat_x.m22=-xp3.x * x3.x;	mat_x.m23=-xp3.x * x3.y;
		mat_x.m30=x4.x;	mat_x.m31=x4.y;	mat_x.m32=-xp4.x * x4.x;	mat_x.m33=-xp4.x * x4.y;
		if(!mat_x.inverse(mat_x)){
			return false;
		}
		
		double gy1=mat_y.m20*xp1.y+mat_y.m21*xp2.y+mat_y.m22*xp3.y+mat_y.m23*xp4.y;
		double gy2=mat_y.m20+mat_y.m21+mat_y.m22+mat_y.m23;
		double hy1=mat_y.m30*xp1.y+mat_y.m31*xp2.y+mat_y.m32*xp3.y+mat_y.m33*xp4.y;
		double hy2=mat_y.m30+mat_y.m31+mat_y.m32+mat_y.m33;
		
//		G=gy1-F*gy2;
//		H=hy1-F*hy2;
		
		

		double gx1=mat_x.m20*xp1.x+mat_x.m21*xp2.x+mat_x.m22*xp3.x+mat_x.m23*xp4.x;
		double gx2=(mat_x.m20+mat_x.m21+mat_x.m22+mat_x.m23);
		double hx1=mat_x.m30*xp1.x+mat_x.m31*xp2.x+mat_x.m32*xp3.x+mat_x.m33*xp4.x;
		double hx2=(mat_x.m30+mat_x.m31+mat_x.m32+mat_x.m33);

//		G=gx1-C*gx2;
//		H=hx1-C*hx2;
		
//		F*gy2-C*gx2=gy1-gx1
//		F*hy2-C*hx2=hy1-hx1;
		NyARDoubleMatrix22 tm=new NyARDoubleMatrix22();
		tm.m00=gy2;tm.m01=-gx2;
		tm.m10=hy2;tm.m11=-hx2;
		tm.inverse(tm);
		Hm.m12=tm.m00*(gy1-gx1)+tm.m01*(hy1-hx1);
		Hm.m02=tm.m10*(gy1-gx1)+tm.m11*(hy1-hx1);
		Hm.m20=gx1-Hm.m02*gx2;
		Hm.m21=hx1-Hm.m02*hx2;
		Hm.m00=(mat_x.m00*xp1.x+mat_x.m01*xp2.x+mat_x.m02*xp3.x+mat_x.m03*xp4.x)-Hm.m02*(mat_x.m00+mat_x.m01+mat_x.m02+mat_x.m03);
		Hm.m01=(mat_x.m10*xp1.x+mat_x.m11*xp2.x+mat_x.m12*xp3.x+mat_x.m13*xp4.x)-Hm.m02*(mat_x.m10+mat_x.m11+mat_x.m12+mat_x.m13);
		Hm.m10=(mat_y.m00*xp1.y+mat_y.m01*xp2.y+mat_y.m02*xp3.y+mat_y.m03*xp4.y)-Hm.m12*(mat_y.m00+mat_y.m01+mat_y.m02+mat_y.m03);
		Hm.m11=(mat_y.m10*xp1.y+mat_y.m11*xp2.y+mat_y.m12*xp3.y+mat_y.m13*xp4.y)-Hm.m12*(mat_y.m10+mat_y.m11+mat_y.m12+mat_y.m13);
		Hm.m22=1;
		return true;


	}
}
