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


interface NyARLabeling{
    /**
     * 検出したラベルの数を返す
     * @return
     */
    public int getLabelNum();
    /**
     * 
     * @return
     * @throws NyARException
     */
    public int[] getLabelRef() throws NyARException;
    /**
     * 検出したラベル配列
     * @return
     * @throws NyARException
     */
    public NyARLabel[] getLabel() throws NyARException;
    /**
     * ラベリング済みイメージを返す
     * @return
     * @throws NyARException
     */
    public int[][] getLabelImg() throws NyARException;
    /**
     * static ARInt16 *labeling2( ARUint8 *image, int thresh,int *label_num, int **area, double **pos, int **clip,int **label_ref, int LorR )
     * 関数の代替品
     * ラスタimageをラベリングして、結果を保存します。
     * Optimize:STEP[1514->1493]
     * @param image
     * @param thresh
     * @throws NyARException
     */
    public void labeling(NyARRaster image,int thresh) throws NyARException;
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

class NyARLabel
{
    public int area;
    public int clip0;
    public int clip1;
    public int clip2;
    public int clip3;
    public double pos_x;
    public double pos_y;
}


class NyARLabelHolder
{
    private final static int ARRAY_APPEND_STEP=128;
    public final NyARLabel[] labels;
    private int allocate_size;
    /**
     * 最大i_holder_size個の動的割り当てバッファを準備する。
     * @param i_holder_size
     */
    public NyARLabelHolder(int i_holder_size)
    {
	//ポインタだけははじめに確保しておく
	this.labels=new NyARLabel[i_holder_size];
	this.allocate_size=0;
    }
    /**
     * i_indexで指定した番号までのバッファを準備する。
     * @param i_index
     */
    private final void reserv(int i_index) throws NyARException
    {
	//アロケート済みなら即リターン
	if(this.allocate_size>i_index){
	    return;
	}
	//要求されたインデクスは範囲外
	if(i_index>=this.labels.length){
	    throw new NyARException();
	}	
	//追加アロケート範囲を計算
	int range=i_index+ARRAY_APPEND_STEP;
	if(range>=this.labels.length){
	    range=this.labels.length;
	}
	//アロケート
	for(int i=this.allocate_size;i<range;i++)
	{
	    this.labels[i]=new NyARLabel();
	}
	this.allocate_size=range;
    }
    /**
     * i_reserv_sizeまでのバッファを、初期条件i_lxsizeとi_lysizeで初期化する。
     * @param i_reserv_size
     * @param i_lxsize
     * @param i_lysize
     * @throws NyARException
     */
    public final void init(int i_reserv_size,int i_lxsize,int i_lysize) throws NyARException
    {
	reserv(i_reserv_size);
	NyARLabel l;
	for(int i=0;i<i_reserv_size;i++){
	    l=this.labels[i];
	    l.area=0;
	    l.pos_x=0;
	    l.pos_y=0;
	    l.clip0= i_lxsize;//wclip[i*4+0] = lxsize;
	    l.clip1= 0;//wclip[i*4+0] = lxsize;
	    l.clip2= i_lysize;//wclip[i*4+2] = lysize;
	    l.clip3= 0;//wclip[i*4+3] = 0;
	}	
    }
}


/**
 * ラベリングクラス。NyARRasterをラベリングして、結果値を保持します。
 * 構造を維持して最適化をしたバージョン
 *
 */
class NyARLabeling_O2 implements NyARLabeling
{
    private static final int WORK_SIZE=1024*32;//#define WORK_SIZE   1024*32
    private final int[][] glabel_img;//static ARInt16 l_imageL[HARDCODED_BUFFER_WIDTH*HARDCODED_BUFFER_HEIGHT];

    private final NyARWorkHolder work_holder=new NyARWorkHolder(WORK_SIZE);
    private final NyARLabelHolder label_holder=new NyARLabelHolder(WORK_SIZE);

    private int label_num;
    //
    private final int width;
    private final int height;
    /**
     * @param i_width
     * ラベリング画像の幅。解析するラスタの幅より大きいこと。
     * @param i_height
     * ラベリング画像の高さ。解析するラスタの高さより大きいこと。
     */
    public NyARLabeling_O2(int i_width,int i_height)
    {
	width =i_width;
	height=i_height;
	glabel_img=new int[height][width];
	this.wk_reservLineBuffer_buf=new int[width];
	label_num=0;


	//ワークイメージに枠を書く
	int[][] label_img=this.glabel_img;
	for(int i = 0; i < i_width; i++){
	    label_img[0][i]=0;
	    label_img[i_height-1][i]=0;
	}
	//</Optimize>
	for(int i = 0; i < i_height; i++) {
	    label_img[i][0]=0;
	    label_img[i][i_width-1]=0;			    
	}
	
	
	
	
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
     * 検出したエリア配列？
     * @return
     * @throws NyARException
     */
    public NyARLabel[] getLabel() throws NyARException
    {
	if(label_num<1){
	    throw new NyARException();
	}
	return this.label_holder.labels;
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
	return work_holder.work;
    }
    /**
     * ラベリング済みイメージを返す
     * @return
     * @throws NyARException
     */
    public int[][] getLabelImg() throws NyARException
    {
	return glabel_img;
    }
    //コンストラクタで作ること
    private int[] wk_reservLineBuffer_buf=null;

    /**
     * static ARInt16 *labeling2( ARUint8 *image, int thresh,int *label_num, int **area, double **pos, int **clip,int **label_ref, int LorR )
     * 関数の代替品
     * ラスタimageをラベリングして、結果を保存します。
     * Optimize:STEP[1514->1493]
     * @param image
     * @param thresh
     * @throws NyARException
     */
    public void labeling(NyARRaster image,int thresh) throws NyARException
    {
	int wk_max;                   /*  work                */
	int m,n;                      /*  work                */
	int lxsize, lysize;
	int thresht3 = thresh * 3;
	int i,j,k;
	lxsize=image.getWidth();//lxsize = arUtil_c.arImXsize;
	lysize=image.getHeight();//lysize = arUtil_c.arImYsize;
	//画素数の一致チェック
	if(lxsize!=this.width || lysize!=this.height){
	    throw new NyARException();
	}	
	//ラベル数を0に初期化
	this.label_num=0;



	int[][] label_img=this.glabel_img;
	

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
            image.getPixelTotalRowLine(j,line_bufferr);

	    for(i = 1; i < lxsize-1; i++) {//for(int i = 1; i < lxsize-1; i++, pnt+=poff, pnt2++) {
		//RGBの合計値が閾値より大きいかな？
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
	j = 1;
	for(i = 0; i < wk_max; i++){//for(int i = 1; i <= wk_max; i++, wk++) {
	    work[i]=(work[i]==i+1)? j++: work[work[i]-1];//*wk = (*wk==i)? j++: work[(*wk)-1];
	}

	int wlabel_num=j - 1;//*label_num = *wlabel_num = j - 1;

	if(wlabel_num==0){//if( *label_num == 0 ) {
	    //発見数0
	    return;
	}

	
	
	//ラベルバッファを予約&初期化
	this.label_holder.init(wlabel_num, lxsize, lysize);
//	
//	putZero(warea,wlabel_num);//put_zero( (ARUint8 *)warea, *label_num *     sizeof(int) );
//	for(i=0;i<wlabel_num;i++){
//	    wpos[i*2+0]=0;
//	    wpos[i*2+1]=0;
//	}
//	for(i = 0; i < wlabel_num; i++) {//for(i = 0; i < *label_num; i++) {
//	    wclip[i][0] = lxsize;//wclip[i*4+0] = lxsize;
//	    wclip[i][1] = 0;//wclip[i*4+1] = 0;
//	    wclip[i][2] = lysize;//wclip[i*4+2] = lysize;
//	    wclip[i][3] = 0;//wclip[i*4+3] = 0;
//	}
	NyARLabel label_pt;
	NyARLabel[] labels=this.label_holder.labels;
	
	for(i = 0; i < wk_max; i++){
	    label_pt=labels[work[i] - 1];
	    work2_pt=work2[i];
	    label_pt.area  += work2_pt[0];
	    label_pt.pos_x += work2_pt[1];
	    label_pt.pos_y += work2_pt[2];
	    if( label_pt.clip0 > work2_pt[3] ){
		label_pt.clip0 = work2_pt[3];
	    }
	    if( label_pt.clip1 < work2_pt[4] ){
		label_pt.clip1 = work2_pt[4];
	    }
	    if(label_pt.clip2 > work2_pt[5] ){
		label_pt.clip2 = work2_pt[5];
	    }
	    if( label_pt.clip3 < work2_pt[6] ){
		label_pt.clip3 = work2_pt[6];
	    }
	}

	for(i = 0; i < wlabel_num; i++ ) {//for(int i = 0; i < *label_num; i++ ) {
	    label_pt=labels[i];
	    label_pt.pos_x /= label_pt.area;
	    label_pt.pos_y /= label_pt.area;
	}

	label_num=wlabel_num;
	return;
    }
}

