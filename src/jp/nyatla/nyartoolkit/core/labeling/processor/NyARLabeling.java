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
package jp.nyatla.nyartoolkit.core.labeling.processor;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.labeling.*;
import jp.nyatla.nyartoolkit.core.types.*;









/**
 * ARToolKit互換のラベリングクラスです。
 * ARToolKitと同一な評価結果を返します。
 *
 */
public class NyARLabeling implements INyLabeling
{
    private static final int WORK_SIZE=1024*32;//#define WORK_SIZE   1024*32
    private final NyARWorkHolder work_holder=new NyARWorkHolder(WORK_SIZE);
    private int _thresh;
    private TNyIntSize _dest_size;
    private NyLabelingImage _out_image;
    public NyARLabeling()
    {
	this._thresh=110;
    }
    public void setThresh(int i_thresh)
    {
	this._thresh=i_thresh;	
    }
    //コンストラクタで作ること
    private int[] wk_reservLineBuffer_buf;
    public void attachDestination(NyLabelingImage i_destination_image) throws NyARException
    {
	//サイズチェック
	TNyIntSize size=i_destination_image.getSize();
	this._out_image=i_destination_image;
	
	//ラインバッファの準備
	if(this.wk_reservLineBuffer_buf==null){
	    this.wk_reservLineBuffer_buf=new int[size.w];
	}else if(this.wk_reservLineBuffer_buf.length<size.w){
	    this.wk_reservLineBuffer_buf=new int[size.w];	    
	}
	
	//NyLabelingImageのイメージ初期化(枠書き)
	int[][] img=i_destination_image.getImage();
	for(int i = 0; i < size.w; i++){
	    img[0][i]  =0;
	    img[size.h-1][i]=0;
	}
	for(int i = 0; i < size.h; i++) {
	    img[i][0]  =0;
	    img[i][size.w-1]=0;			    
	}
	
	//サイズ(参照値)を保存
	this._dest_size=size;	
    }


    /**
     * static ARInt16 *labeling2( ARUint8 *image, int thresh,int *label_num, int **area, double **pos, int **clip,int **label_ref, int LorR )
     * 関数の代替品
     * ラスタimageをラベリングして、結果を保存します。
     * Optimize:STEP[1514->1493]
     * @param i_image
     * @param thresh
     * @throws NyARException
     */
    public void labeling(INyARRaster i_input_raster) throws NyARException
    {
	int wk_max;                   /*  work                */
	int m,n;                      /*  work                */
	int thresht3 = this._thresh * 3;
	int i,j,k;
        NyLabelingImage out_image=this._out_image;	
	
        //サイズチェック
        TNyIntSize in_size=i_input_raster.getSize();
        this._dest_size.isEqualSize(in_size);
        
        int lxsize=in_size.w;//lxsize = arUtil_c.arImXsize;
	int lysize=in_size.h;//lysize = arUtil_c.arImYsize;
	int[][] label_img=out_image.getImage();
	

	//枠作成はインスタンスを作った直後にやってしまう。
	
	int[] work2_pt;
	wk_max = 0;

	int label_pixel;
	
	int[] work=this.work_holder.work;
	int[][] work2=this.work_holder.work2;
	int[] line_bufferr=this.wk_reservLineBuffer_buf;
	
	int[] label_img_pt0,label_img_pt1;
	for(j = 1; j < lysize - 1; j++) {//for (int j = 1; j < lysize - 1; j++, pnt += poff*2, pnt2 += 2) {
            label_img_pt0=label_img[j];
            label_img_pt1=label_img[j-1];
            i_input_raster.getPixelTotalRowLine(j,line_bufferr);

	    for(i = 1; i < lxsize-1; i++) {//for(int i = 1; i < lxsize-1; i++, pnt+=poff, pnt2++) {
		//RGBの合計値が閾値より小さいかな？
		if(line_bufferr[i]<=thresht3){
		    //pnt1 = ShortPointer.wrap(pnt2, -lxsize);//pnt1 = &(pnt2[-lxsize]);
		    if(label_img_pt1[i]>0){//if( *pnt1 > 0 ) {
			label_pixel=label_img_pt1[i];//*pnt2 = *pnt1;


			work2_pt=work2[label_pixel-1];
			work2_pt[0]++;//work2[((*pnt2)-1)*7+0] ++;
			work2_pt[1]+=i;//work2[((*pnt2)-1)*7+1] += i;
			work2_pt[2]+=j;//work2[((*pnt2)-1)*7+2] += j;
			work2_pt[6]=j;//work2[((*pnt2)-1)*7+6] = j;
		    }else if(label_img_pt1[i+1]> 0 ) {//}else if( *(pnt1+1) > 0 ) {
			if(label_img_pt1[i-1] > 0 ) {//if( *(pnt1-1) > 0 ) {
			    m = work[label_img_pt1[i+1]-1];//m = work[*(pnt1+1)-1];
			    n = work[label_img_pt1[i-1]-1];//n = work[*(pnt1-1)-1];
			    if( m > n ){
				label_pixel=n;//*pnt2 = n;
				//wk=IntPointer.wrap(work, 0);//wk = &(work[0]);
				for(k = 0; k < wk_max; k++) {
				    if(work[k] == m ){//if( *wk == m ) 
					work[k]=n;//*wk = n;
				    }
				}
			    }else if( m < n ) {
				label_pixel=m;//*pnt2 = m;
				//wk=IntPointer.wrap(work,0);//wk = &(work[0]);
				for(k = 0; k < wk_max; k++){
				    if(work[k]==n){//if( *wk == n ){
					work[k]=m;//*wk = m;
				    }
				}
			    }else{
				label_pixel=m;//*pnt2 = m;
			    }
			    work2_pt=work2[label_pixel-1];
			    work2_pt[0] ++;
			    work2_pt[1] += i;
			    work2_pt[2] += j;
			    work2_pt[6] = j;
			}else if( (label_img_pt0[i-1]) > 0 ) {//}else if( *(pnt2-1) > 0 ) {
			    m = work[(label_img_pt1[i+1])-1];//m = work[*(pnt1+1)-1];
			    n = work[label_img_pt0[i-1]-1];//n = work[*(pnt2-1)-1];
			    if( m > n ) {

				label_pixel=n;//*pnt2 = n;
				for(k = 0; k < wk_max; k++) {
				    if(work[k]==m){//if( *wk == m ){
					work[k]=n;//*wk = n;
				    }
				}
			    }else if( m < n ) {
				label_pixel=m;//*pnt2 = m;
				for(k = 0; k < wk_max; k++) {
				    if(work[k]==n){//if( *wk == n ){
					work[k]=m;//*wk = m;
				    }
				}
			    }else{
				label_pixel=m;//*pnt2 = m;
			    }
			    work2_pt=work2[label_pixel-1];
			    work2_pt[0] ++;//work2[((*pnt2)-1)*7+0] ++;
			    work2_pt[1] += i;//work2[((*pnt2)-1)*7+1] += i;
			    work2_pt[2] += j;//work2[((*pnt2)-1)*7+2] += j;
			}else{

			    label_pixel=label_img_pt1[i+1];//*pnt2 = *(pnt1+1);

			    work2_pt=work2[label_pixel-1];
			    work2_pt[0] ++;//work2[((*pnt2)-1)*7+0] ++;
			    work2_pt[1] += i;//work2[((*pnt2)-1)*7+1] += i;
			    work2_pt[2] += j;//work2[((*pnt2)-1)*7+2] += j;
			    if( work2_pt[3] > i ){//if( work2[((*pnt2)-1)*7+3] > i ){		
				work2_pt[3] = i;//	work2[((*pnt2)-1)*7+3] = i;
			    }
			    work2_pt[6] = j;//work2[((*pnt2)-1)*7+6] = j;
			}
		    }else if( (label_img_pt1[i-1]) > 0 ) {//}else if( *(pnt1-1) > 0 ) {
			label_pixel=label_img_pt1[i-1];//*pnt2 = *(pnt1-1);

			work2_pt=work2[label_pixel-1];
			work2_pt[0] ++;//work2[((*pnt2)-1)*7+0] ++;
			work2_pt[1] += i;//work2[((*pnt2)-1)*7+1] += i;
			work2_pt[2] += j;//work2[((*pnt2)-1)*7+2] += j;
			if( work2_pt[4] < i ){//if( work2[((*pnt2)-1)*7+4] < i ){
			    work2_pt[4] = i;//	work2[((*pnt2)-1)*7+4] = i;
			}
			work2_pt[6] = j;//work2[((*pnt2)-1)*7+6] = j;
		    }else if(label_img_pt0[i-1] > 0) {//}else if( *(pnt2-1) > 0) {
			label_pixel=label_img_pt0[i-1];//*pnt2 = *(pnt2-1);

			work2_pt=work2[label_pixel-1];
			work2_pt[0] ++;//work2[((*pnt2)-1)*7+0] ++;
			work2_pt[1] += i;//work2[((*pnt2)-1)*7+1] += i;
			work2_pt[2] += j;//work2[((*pnt2)-1)*7+2] += j;
			if(work2_pt[4] < i ){//if( work2[((*pnt2)-1)*7+4] < i ){
			    work2_pt[4] = i;//	work2[((*pnt2)-1)*7+4] = i;
			}
		    }else{
			//現在地までの領域を予約
			this.work_holder.reserv(wk_max);
			wk_max++;
			work[wk_max-1] = wk_max;
			label_pixel=wk_max;//work[wk_max-1] = *pnt2 = wk_max;
			work2_pt=work2[wk_max-1];
			work2_pt[0] = 1;
			work2_pt[1] = i;
			work2_pt[2] = j;
			work2_pt[3] = i;
			work2_pt[4] = i;
			work2_pt[5] = j;
			work2_pt[6] = j;
		    }
		    label_img_pt0[i]=label_pixel;
		}else {
		    label_img_pt0[i]=0;//*pnt2 = 0;
		}
	    }
	}
	//グループ化とラベル数の計算
	int wlabel_num=1;//*label_num = *wlabel_num = j - 1;
	
	for(i = 0; i < wk_max; i++){//for(int i = 1; i <= wk_max; i++, wk++) {
	    work[i]=(work[i]==i+1)? wlabel_num++: work[work[i]-1];//*wk = (*wk==i)? j++: work[(*wk)-1];
	}
	wlabel_num-=1;//*label_num = *wlabel_num = j - 1;
	if(wlabel_num==0){//if( *label_num == 0 ) {
	    //発見数0
	    out_image.getLabelList().setLength(0);
	    return;
	}
	
	//ラベル衝突の解消
	int[] line;
	int i2,l1;
	l1=lxsize & 0xfffffffc;
	for(i=lysize-1;i>=0;i--)
	{
	    line=label_img[i];
	    int pix;	    
	    for(i2=0;i2<l1;)
	    {
		pix=line[i2];
		if(pix!=0){
		    line[i2]=work[pix-1];
		}
		i2++;
		
		pix=line[i2];
		if(pix!=0){
		    line[i2]=work[pix-1];
		}
		i2++;

		pix=line[i2];
		if(pix!=0){
		    line[i2]=work[pix-1];
		}
		i2++;

		pix=line[i2];
		if(pix!=0){
		    line[i2]=work[pix-1];
		}
		i2++;
	    }
	    for(;i2<lxsize;i2++){
		pix=line[i2];
		if(pix==0){
		    continue;
		}
		line[i2]=work[pix-1];
		i2++;
	    }
	}

	
	//ラベル情報の保存等
	NyLabelingLabelList label_list=out_image.getLabelList();

	//ラベルバッファを予約
	label_list.reserv(wlabel_num);

	//エリアと重心、クリップ領域を計算
	NyLabelingLabel label_pt;
	NyLabelingLabel[] labels=label_list.getArray();
	for(i=0;i<wlabel_num;i++)
	{
	    label_pt=labels[i];
	    label_pt.area=0;
	    label_pt.pos_x=0;
	    label_pt.pos_y=0;
	    label_pt.clip_l= lxsize;//wclip[i*4+0] = lxsize;
	    label_pt.clip_r= 0;//wclip[i*4+0] = lxsize;
	    label_pt.clip_t= lysize;//wclip[i*4+2] = lysize;
	    label_pt.clip_b= 0;//wclip[i*4+3] = 0;	    
	}

	
	for(i = 0; i < wk_max; i++){
	    label_pt=labels[work[i] - 1];
	    work2_pt=work2[i];
	    label_pt.area  += work2_pt[0];
	    label_pt.pos_x += work2_pt[1];
	    label_pt.pos_y += work2_pt[2];
	    if( label_pt.clip_l > work2_pt[3] ){
		label_pt.clip_l = work2_pt[3];
	    }
	    if( label_pt.clip_r < work2_pt[4] ){
		label_pt.clip_r = work2_pt[4];
	    }
	    if(label_pt.clip_t > work2_pt[5] ){
		label_pt.clip_t = work2_pt[5];
	    }
	    if( label_pt.clip_b < work2_pt[6] ){
		label_pt.clip_b = work2_pt[6];
	    }
	}

	for(i = 0; i < wlabel_num; i++ ) {//for(int i = 0; i < *label_num; i++ ) {
	    label_pt=labels[i];
	    label_pt.pos_x /= label_pt.area;
	    label_pt.pos_y /= label_pt.area;
	}
	//ラベル個数を保存する
	label_list.setLength(wlabel_num);
	return;
    }
}

/**
 * NyARLabeling_O2のworkとwork2を可変長にするためのクラス
 * 
 *
 */
class NyARWorkHolder
{
    private final static int ARRAY_APPEND_STEP=256;
    public final int[] work;
    public final int[][] work2;
    private int allocate_size;
    /**
     * 最大i_holder_size個の動的割り当てバッファを準備する。
     * @param i_holder_size
     */
    public NyARWorkHolder(int i_holder_size)
    {
	//ポインタだけははじめに確保しておく
	this.work=new int[i_holder_size];
	this.work2=new int[i_holder_size][];
	this.allocate_size=0;
    }
    /**
     * i_indexで指定した番号までのバッファを準備する。
     * @param i_index
     */
    public final void reserv(int i_index) throws NyARException
    {
	//アロケート済みなら即リターン
	if(this.allocate_size>i_index){
	    return;
	}
	//要求されたインデクスは範囲外
	if(i_index>=this.work.length){
	    throw new NyARException();
	}	
	//追加アロケート範囲を計算
	int range=i_index+ARRAY_APPEND_STEP;
	if(range>=this.work.length){
	    range=this.work.length;
	}
	//アロケート
	for(int i=this.allocate_size;i<range;i++)
	{
	    this.work2[i]=new int[7];
	}
	this.allocate_size=range;
    }
}
