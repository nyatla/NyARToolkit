/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java version ARToolkit class library.
 * Copyright (C)2008 R.Iizuka
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this framework; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;




/**
 * ラベリングクラス。NyARRasterをラベリングして、結果値を保持します。
 *
 */
public class NyARLabeling{
    private final int WORK_SIZE=1024*32;//#define WORK_SIZE   1024*32
    private short[][] label_img;//static ARInt16 l_imageL[HARDCODED_BUFFER_WIDTH*HARDCODED_BUFFER_HEIGHT];
    private int[] work=new int[WORK_SIZE];//static int workL[WORK_SIZE];
    private int[] work2=new int[WORK_SIZE*7];//static int work2L[WORK_SIZE*7];
    private int[] area=new int[WORK_SIZE];//static int          wareaL[WORK_SIZE];
    private int[][] clip=new int[WORK_SIZE][4];//static int          wclipL[WORK_SIZE*4];
    private double[] pos=new double[WORK_SIZE*2];//static double       wposL[WORK_SIZE*2];
    private int label_num;
    //
    private int width;
    private int height;
    /**
     * @param i_width
     * ラベリング画像の幅。解析するラスタの幅より大きいこと。
     * @param i_height
     * ラベリング画像の高さ。解析するラスタの高さより大きいこと。
     */
    public NyARLabeling(int i_width,int i_height)
    {
	width =i_width;
	height=i_height;
	label_img=new short[height][width];
	label_num=0;
    }
    /**
     * 検出したラベルの数を返す
     * @return
     */
    public int getLabelNum()
    {
	return label_num;
    }
    /**
     * 
     * @return
     * @throws NyARException
     */
    public int[] getLabelRef() throws NyARException
    {
	if(label_num<1){
	    throw new NyARException();
	}
	return work;
    }
    /**
     * 検出したエリア配列？
     * @return
     * @throws NyARException
     */
    public int[] getArea() throws NyARException
    {
	if(label_num<1){
	    throw new NyARException();
	}
	return area;
    }
    /**
     * 検出したクリップ配列？
     * @return
     * @throws NyARException
     */
    public int[][] getClip() throws NyARException
    {
	if(label_num<1){
	    throw new NyARException();
	}
	return clip;
    }
    /**
     * 検出した位置配列？
     * @return
     * @throws NyARException
     */
    public double[] getPos() throws NyARException
    {
	if(label_num<1){
	    throw new NyARException();
	}
	return pos;
    }
    /**
     * ラベリング済みイメージを返す
     * @return
     * @throws NyARException
     */
    public short[][] getLabelImg() throws NyARException
    {
	if(label_num<1){
	    throw new NyARException();
	}
	return label_img;
    }
    /**
     * 配列の先頭からsize個をゼロクリアする
     * @param array
     * @param size
     */
    private void putZero(int[] array,int size)
    {
    	for(int i=0;i<size;i++){
            array[i]=0;
    	}
    }
    /**
     * 配列の先頭からsize個をゼロクリアする
     * @param array
     * @param size
     */
    private void putZero(double[] array,int size)
    {
    	for(int i=0;i<size;i++){
            array[i]=0;
    	}
    }
    /**
     * static ARInt16 *labeling2( ARUint8 *image, int thresh,int *label_num, int **area, double **pos, int **clip,int **label_ref, int LorR )
     * 関数の代替品
     * ラスタimageをラベリングして、結果を保存します。
     * @param image
     * @param thresh
     * @throws NyARException
     */
    public void labeling(NyARRaster image,int thresh) throws NyARException
    {
        int       wk_max;                   /*  work                */
        int       m,n;                      /*  work                */
        int       lxsize, lysize;
        int[] warea;//int       *warea;
        int[][] wclip;//int       *wclip;
        double[] wpos;//double    *wpos;
        int		  thresht3 = thresh * 3;
        
        //ラベル数を0に初期化
        label_num=0;
    
        warea=area;//warea   = &wareaL[0];
        wclip=clip;//wclip   = &wclipL[0];
        wpos=pos;//wpos    = &wposL[0];
    
        lxsize=image.getWidth();//lxsize = arUtil_c.arImXsize;
        lysize=image.getHeight();//lysize = arUtil_c.arImYsize;
    
        for(int i = 0; i < lxsize; i++){
            label_img[0][i]=0;
            label_img[lysize-1][i]=0;
        }
        for(int i = 0; i < lysize; i++) {
            label_img[i][0]=0;
            label_img[i][lxsize-1]=0;			    
        }
        int nya_pnt_start_x_start,nya_pnt_start_y_start;
        int nya_poff_step;//スキャンステップ

        wk_max = 0;
        nya_pnt_start_y_start=1;
        nya_pnt_start_x_start=1;
        nya_poff_step=1;//スキャンステップ
        int nya_pnt_start_y=nya_pnt_start_y_start;
        for (int j = 1; j < lysize - 1; j++, nya_pnt_start_y++) {//for (int j = 1; j < lysize - 1; j++, pnt += poff*2, pnt2 += 2) {
            int nya_pnt_start_x=nya_pnt_start_x_start;
            int p1=j-1;
            int p2=j;
	    for(int i = 1; i < lxsize-1; i++, nya_pnt_start_x+=nya_poff_step) {//for(int i = 1; i < lxsize-1; i++, pnt+=poff, pnt2++) {
		//RGBの合計値が閾値より大きいかな？
		if(image.getPixelTotal(nya_pnt_start_x,nya_pnt_start_y)<=thresht3){
		    //pnt1 = ShortPointer.wrap(pnt2, -lxsize);//pnt1 = &(pnt2[-lxsize]);
		    if(label_img[p1][i]>0){//if( *pnt1 > 0 ) {
			label_img[p2][i]=label_img[p1][i];//*pnt2 = *pnt1;


			int p2_index=(label_img[p2][i]-1)*7;
			work2[p2_index+0]++;//work2[((*pnt2)-1)*7+0] ++;
			work2[p2_index+1]+=i;//work2[((*pnt2)-1)*7+1] += i;
			work2[p2_index+2]+=j;//work2[((*pnt2)-1)*7+2] += j;
			work2[p2_index+6]=j;//work2[((*pnt2)-1)*7+6] = j;
		    }else if(label_img[p1][i+1]> 0 ) {//}else if( *(pnt1+1) > 0 ) {
			if(label_img[p1][i-1] > 0 ) {//if( *(pnt1-1) > 0 ) {
			    m = work[label_img[p1][i+1]-1];//m = work[*(pnt1+1)-1];
			    n = work[label_img[p1][i-1]-1];//n = work[*(pnt1-1)-1];
			    if( m > n ){
				//JartkException.trap("未チェックのパス");
				label_img[p2][i]=(short)n;//*pnt2 = n;
				//wk=IntPointer.wrap(work, 0);//wk = &(work[0]);
				for(int k = 0; k < wk_max; k++) {
				    //JartkException.trap("未チェックのパス");
				    if(work[k] == m ){//if( *wk == m ) 
					//JartkException.trap("未チェックのパス");
					work[k]=n;//*wk = n;
				    }
				}
			    }else if( m < n ) {
				//JartkException.trap("未チェックのパス");
				label_img[p2][i]=(short)m;//*pnt2 = m;
				//wk=IntPointer.wrap(work,0);//wk = &(work[0]);
				for(int k = 0; k < wk_max; k++){
				    //JartkException.trap("未チェックのパス");
				    if(work[k]==n){//if( *wk == n ){
					//JartkException.trap("未チェックのパス");
					work[k]=m;//*wk = m;
				    }
				}
			    }else{
				label_img[p2][i]=(short)m;//*pnt2 = m;
			    }

			    int p2_index=(label_img[p2][i]-1)*7;
			    work2[p2_index+0] ++;
			    work2[p2_index+1] += i;
			    work2[p2_index+2] += j;
			    work2[p2_index+6] = j;
			}else if( (label_img[p2][i-1]) > 0 ) {//}else if( *(pnt2-1) > 0 ) {
			    m = work[(label_img[p1][i+1])-1];//m = work[*(pnt1+1)-1];
			    n = work[(label_img[p2][i-1])-1];//n = work[*(pnt2-1)-1];
			    if( m > n ) {

				label_img[p2][i]=(short)n;//*pnt2 = n;
				for(int k = 0; k < wk_max; k++) {
				    if(work[k]==m){//if( *wk == m ){
					work[k]=n;//*wk = n;
				    }
				}
			    }else if( m < n ) {
				label_img[p2][i]=(short)m;//*pnt2 = m;
				for(int k = 0; k < wk_max; k++) {
				    if(work[k]==n){//if( *wk == n ){
					work[k]=m;//*wk = m;
				    }
				}
			    }else{
				label_img[p2][i]=(short)m;//*pnt2 = m;
			    }


			    int p2_index=((label_img[p2][i])-1)*7;
			    work2[p2_index+0] ++;//work2[((*pnt2)-1)*7+0] ++;
			    work2[p2_index+1] += i;//work2[((*pnt2)-1)*7+1] += i;
			    work2[p2_index+2] += j;//work2[((*pnt2)-1)*7+2] += j;
			}else{

			    label_img[p2][i]=label_img[p1][i+1];//*pnt2 = *(pnt1+1);
			    
			    int p2_index=((label_img[p2][i])-1)*7;
			    work2[p2_index+0] ++;//work2[((*pnt2)-1)*7+0] ++;
			    work2[p2_index+1] += i;//work2[((*pnt2)-1)*7+1] += i;
			    work2[p2_index+2] += j;//work2[((*pnt2)-1)*7+2] += j;
			    if( work2[p2_index+3] > i ){//if( work2[((*pnt2)-1)*7+3] > i ){		
				work2[p2_index+3] = i;//	work2[((*pnt2)-1)*7+3] = i;
			    }
			    work2[p2_index+6] = j;//work2[((*pnt2)-1)*7+6] = j;
			}
		    }else if( (label_img[p1][i-1]) > 0 ) {//}else if( *(pnt1-1) > 0 ) {
			label_img[p2][i]=label_img[p1][i-1];//*pnt2 = *(pnt1-1);

			int p2_index=((label_img[p2][i])-1)*7;
			work2[p2_index+0] ++;//work2[((*pnt2)-1)*7+0] ++;
			work2[p2_index+1] += i;//work2[((*pnt2)-1)*7+1] += i;
			work2[p2_index+2] += j;//work2[((*pnt2)-1)*7+2] += j;
			if( work2[p2_index+4] < i ){//if( work2[((*pnt2)-1)*7+4] < i ){
			    work2[p2_index+4] = i;//	work2[((*pnt2)-1)*7+4] = i;
			}
			work2[p2_index+6] = j;//work2[((*pnt2)-1)*7+6] = j;
		    }else if(label_img[p2][i-1] > 0) {//}else if( *(pnt2-1) > 0) {
			label_img[p2][i]=label_img[p2][i-1];//*pnt2 = *(pnt2-1);

			int p2_index=((label_img[p2][i])-1)*7;
			work2[p2_index+0] ++;//work2[((*pnt2)-1)*7+0] ++;
			work2[p2_index+1] += i;//work2[((*pnt2)-1)*7+1] += i;
			work2[p2_index+2] += j;//work2[((*pnt2)-1)*7+2] += j;
			if( work2[p2_index+4] < i ){//if( work2[((*pnt2)-1)*7+4] < i ){
			    work2[p2_index+4] = i;//	work2[((*pnt2)-1)*7+4] = i;
			}
		    }else{
			wk_max++;
			if( wk_max > WORK_SIZE ) {
			    throw new NyARException();//return (0);
			}
			work[wk_max-1] = wk_max;label_img[p2][i]=(short)wk_max;//work[wk_max-1] = *pnt2 = wk_max;
			work2[(wk_max-1)*7+0] = 1;
			work2[(wk_max-1)*7+1] = i;
			work2[(wk_max-1)*7+2] = j;
			work2[(wk_max-1)*7+3] = i;
			work2[(wk_max-1)*7+4] = i;
			work2[(wk_max-1)*7+5] = j;
			work2[(wk_max-1)*7+6] = j;
		    }
		}else {
		    label_img[p2][i]=0;//*pnt2 = 0;
		}
	    }
	}
	int j = 1;
	for(int i = 0; i < wk_max; i++){//for(int i = 1; i <= wk_max; i++, wk++) {
	    work[i]=(work[i]==i+1)? j++: work[work[i]-1];//*wk = (*wk==i)? j++: work[(*wk)-1];
	}
	
	int wlabel_num=j - 1;//*label_num = *wlabel_num = j - 1;

	if(wlabel_num==0){//if( *label_num == 0 ) {
	    //発見数0
	    return;
	}

	putZero(warea,wlabel_num);//put_zero( (ARUint8 *)warea, *label_num *     sizeof(int) );
	putZero(wpos,wlabel_num*2);//put_zero( (ARUint8 *)wpos,  *label_num * 2 * sizeof(double) );
	for(int i = 0; i < wlabel_num; i++) {//for(i = 0; i < *label_num; i++) {
	    wclip[i][0] = lxsize;//wclip[i*4+0] = lxsize;
	    wclip[i][1] = 0;//wclip[i*4+1] = 0;
	    wclip[i][2] = lysize;//wclip[i*4+2] = lysize;
	    wclip[i][3] = 0;//wclip[i*4+3] = 0;
	}
	for(int i = 0; i < wk_max; i++) {
	    j = work[i] - 1;
	    warea[j]    += work2[i*7+0];
	    wpos[j*2+0] += work2[i*7+1];
	    wpos[j*2+1] += work2[i*7+2];
	    if( wclip[j][0] > work2[i*7+3] ){
		wclip[j][0] = work2[i*7+3];
	    }
	    if( wclip[j][1] < work2[i*7+4] ){
		wclip[j][1] = work2[i*7+4];
	    }
	    if( wclip[j][2] > work2[i*7+5] ){
		wclip[j][2] = work2[i*7+5];
	    }
	    if( wclip[j][3] < work2[i*7+6] ){
		wclip[j][3] = work2[i*7+6];
	    }
	}

	for(int i = 0; i < wlabel_num; i++ ) {//for(int i = 0; i < *label_num; i++ ) {
	    wpos[i*2+0] /= warea[i];
	    wpos[i*2+1] /= warea[i];
	}
	
	label_num=wlabel_num;
	return;
    }
}

