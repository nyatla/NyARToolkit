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
import jp.nyatla.nyartoolkit.core.labeling.*;



/**
 * イメージからマーカー情報を検出するクラス。
 * このクラスは、arDetectMarker2.cとの置き換えになります。
 * ラベリング済みのラスタデータからマーカー位置を検出して、結果を保持します。
 *
 */
public class NyARDetectMarker {
    private static final int AR_AREA_MAX=100000;//#define   AR_AREA_MAX      100000
    private static final int AR_AREA_MIN=70;//#define   AR_AREA_MIN          70

//    private final NyARMarker[] marker_holder;	    //マーカーデータの保持配列
//    private final NyARMarker[] marker_info2_array;//マーカーデータのインデックス配列
//    private int marker_num;
    private int width,height;
    /**
     * 最大i_squre_max個のマーカーを検出するクラスを作成する。
     * @param i_width
     * @param i_height
     */
    public NyARDetectMarker(int i_width,int i_height)
    {
	this.width =i_width;
	this.height=i_height;
	
//	this.marker_holder=new NyARMarker[i_squre_max];
//	this.marker_info2_array=new NyARMarker[i_squre_max];
//	//先にマーカーホルダにオブジェクトを作っておく
//	for(int i=0;i<i_squre_max;i++){
//	    this.marker_holder[i]=new NyARMarker();
//	}
    }
    private static final int AR_CHAIN_MAX=10000;
    private final int[] wk_arGetContour_xdir={0,  1, 1, 1, 0,-1,-1,-1};
    private final int[] wk_arGetContour_ydir={-1,-1, 0, 1, 1, 1, 0,-1};
    private final int[] wk_arGetContour_xcoord=new int[AR_CHAIN_MAX];
    private final int[] wk_arGetContour_ycoord=new int[AR_CHAIN_MAX];
    /**
     * int arGetContour( ARInt16 *limage, int *label_ref,int label, int clip[4], ARMarkerInfo2 *marker_info2 )
     * 関数の代替品
     * detectMarker関数から使う関数です。o_markerにlabelとclipで示される1個のマーカーを格納します。
     * marker_holder[i_holder_num]にオブジェクトが無ければまず新規に作成し、もし
     * 既に存在すればそこにマーカー情報を上書きして記録します。
     * Optimize:STEP[369->336]
     * @param o_marker
     * @param limage
     * @param label_ref
     * @param label
     * @param clip
     * @throws NyARException
     */
    private final void arGetContour(NyARMarker o_marker,int[][] limage,int i_labelnum, NyLabelingLabel i_label) throws NyARException
    {
	final int[] xcoord=wk_arGetContour_xcoord;
	final int[] ycoord=wk_arGetContour_ycoord;
	final int[] xdir=wk_arGetContour_xdir; //static int      xdir[8] = { 0, 1, 1, 1, 0,-1,-1,-1};
	final int[] ydir=wk_arGetContour_ydir;//static int      ydir[8] = {-1,-1, 0, 1, 1, 1, 0,-1};
	//ShortPointer p1;//ARInt16         *p1;
	int coord_num;
	int             sx=0, sy=0, dir;
	int             dmax, d, v1=0;
	int             i, j,w;

	int[] limage_j;
	j = i_label.clip_t;
	limage_j=limage[j];
	final int clip1=i_label.clip_r;
	//p1=ShortPointer.wrap(limage,j*xsize+clip.get());//p1 = &(limage[j*xsize+clip[0]]);
	for( i = i_label.clip_l; i <= clip1; i++){//for( i = clip[0]; i <= clip[1]; i++, p1++ ) {
	    w=limage_j[i];
	    if(w > 0 && w == i_labelnum ) {
		sx = i;
		sy = j;
		break;
	    }
	}
	if(i>clip1){//if( i > clip[1] ) {
	    System.out.println("??? 1");//printf();
	    throw new NyARException();//return(-1);
	}

//	//マーカーホルダが既に確保済みかを調べる
//	if(marker_holder[i_holder_num]==null){
//	    //確保していなければ確保
//	    marker_holder[i_holder_num]=new NyARMarker();
//	}


	coord_num=1;//marker_info2->coord_num = 1;
	xcoord[0]=sx;//marker_info2->x_coord[0] = sx;
	ycoord[0]=sy;//marker_info2->y_coord[0] = sy;
	dir = 5;
	
	int r,c;
        c=xcoord[0];
        r=ycoord[0];
        dmax=0;
        //本家はdmaxの作成とxcoordの作成を別のループでやってるけど、非効率なので統合
	for(;;){
	    //xcoord[1]-xcoord[n]までのデータを作る。
	    
//	    1個前のxcoordとycoordはループ後半で格納される。
//	    c=xcoord[coord_num-1];
//	    r=ycoord[coord_num-1];
	    //p1 = &(limage[marker_info2->y_coord[marker_info2->coord_num-1] * xsize+ marker_info2->x_coord[marker_info2->coord_num-1]]);
	    dir = (dir+5)%8;
	    for(i=0;i<8;i++) {
		if(limage[r+ydir[dir]][c+xdir[dir]]>0){//if( p1[ydir[dir]*xsize+xdir[dir]] > 0 ){
		    break;
		}
		dir = (dir+1)%8;	
	    }
	    if( i == 8 ){
		System.out.println("??? 2");//printf("??? 2\n");
		throw new NyARException();//return(-1);
	    }
//	    xcoordとycoordをc,rにも保存
	    c= c + xdir[dir];//marker_info2->x_coord[marker_info2->coord_num]= marker_info2->x_coord[marker_info2->coord_num-1] + xdir[dir];
	    r= r + ydir[dir];//marker_info2->y_coord[marker_info2->coord_num]= marker_info2->y_coord[marker_info2->coord_num-1] + ydir[dir];
	    xcoord[coord_num]=c;//marker_info2->x_coord[marker_info2->coord_num]= marker_info2->x_coord[marker_info2->coord_num-1] + xdir[dir];
	    ycoord[coord_num]=r;//marker_info2->y_coord[marker_info2->coord_num]= marker_info2->y_coord[marker_info2->coord_num-1] + ydir[dir];
	    if(c == sx && r == sy ){
		break;
	    }
	    //dmaxの計算
	    d=(c-sx)*(c-sx)+(r-sy)*(r-sy);
	    if( d > dmax ) {
		dmax = d;
		v1 = coord_num;
	    }
	    //終了条件判定
	    coord_num++;
	    if(coord_num == AR_CHAIN_MAX-1){//if( marker_info2.coord_num == Config.AR_CHAIN_MAX-1 ){
		System.out.println("??? 3");//printf("??? 3\n");
		throw new NyARException();//return(-1);
	    }
	}
//
//	dmax = 0;
//	for(i=1;i<coord_num;i++) {//	for(i=1;i<marker_info2->coord_num;i++) {
//	    d = (xcoord[i]-sx)*(xcoord[i]-sx)+ (ycoord[i]-sy)*(ycoord[i]-sy);//	  d = (marker_info2->x_coord[i]-sx)*(marker_info2->x_coord[i]-sx)+ (marker_info2->y_coord[i]-sy)*(marker_info2->y_coord[i]-sy);
//	    if( d > dmax ) {
//		dmax = d;
//		v1 = i;
//	    }
//	}
	//NyARMarkerへcoord情報をセット
	//coordの並び替えと保存はNyARMarkerへ移動
	o_marker.setCoordXY(v1,coord_num,xcoord,ycoord);
	return;
    }

    /**
     * ARMarkerInfo2 *arDetectMarker2( ARInt16 *limage, int label_num, int *label_ref,int *warea, double *wpos, int *wclip,int area_max, int area_min, double factor, int *marker_num )
     * 関数の代替品
     * ラベリング情報からマーカー一覧を作成してo_marker_listを更新します。
     * 関数はo_marker_listに重なりを除外したマーカーリストを作成します。
     * 
     * @param i_labeling
     * ラベリング済みの情報を持つラベリングオブジェクト
     * @param i_factor
     * 何かの閾値？
     * @param o_marker_list
     * 抽出したマーカーを格納するリスト
     * @throws NyARException
     */
    public final void detectMarker(NyLabelingImage i_labeling,double i_factor,NyARMarkerList o_marker_list) throws NyARException
    {
	int label_area;
	int i;
	int xsize, ysize;
	NyLabelingLabel[] labels=i_labeling.getLabelList().getArray();
//	int[] warea  	=i_labeling.getArea();
	int label_num	=i_labeling.getLabelList().getCount();
//	int[][] wclip	=i_labeling.getClip();
//	double[] wpos	=i_labeling.getPos();
	int[][] limage=i_labeling.getImage();

	//マーカーホルダをリセット
	o_marker_list.reset();
//	marker_num=0;
	xsize =width;
	ysize =height;
//	マーカーをmarker_holderに蓄積する。
	NyARMarker current_marker=o_marker_list.getCurrentHolder();
	NyLabelingLabel label_pt;
	for(i=0; i<label_num; i++ ){
	    label_pt=labels[i];
	    label_area=label_pt.area;
	    if(label_area < AR_AREA_MIN || label_area > AR_AREA_MAX ){
		continue;
	    }
	    if( label_pt.clip_l == 1 || label_pt.clip_r == xsize-2 ){//if( wclip[i*4+0] == 1 || wclip[i*4+1] == xsize-2 ){
		continue;
	    }
	    if( label_pt.clip_t == 1 || label_pt.clip_b == ysize-2 ){//if( wclip[i*4+2] == 1 || wclip[i*4+3] == ysize-2 ){
		continue;
	    }
	    //ret = arGetContour( limage, label_ref, i+1,&(wclip[i*4]), &(marker_info2[marker_num2]));
	    arGetContour(current_marker,limage, i+1,label_pt);

	    if(!current_marker.checkSquare(label_area,i_factor,label_pt.pos_x,label_pt.pos_y)){
		//後半で整理するからここはいらない。//        	marker_holder[marker_num2]=null;
		continue;
	    }
//	    この3行はcheckSquareの最終段に含める。
//	    marker_holder[marker_num2].area   = warea[i];
//	    marker_holder[marker_num2].pos[0] = wpos[i*2+0];
//	    marker_holder[marker_num2].pos[1] = wpos[i*2+1];
	    //マーカー検出→次のホルダを取得
	    current_marker=o_marker_list.getNextHolder();
	    //マーカーリストが上限に達したか確認
	    if(current_marker==null){
		break;
	    }
	}
	//マーカーリストを整理(重なり処理とかはマーカーリストに責務押し付け)
	o_marker_list.updateMarkerArray();
//	重なり処理かな？
//	double[] pos_j,pos_i;
//	for(i=0; i < marker_num2; i++ ){
//	    pos_i=marker_holder[i].pos;
//	    for(j=i+1; j < marker_num2; j++ ) {
//		pos_j=marker_holder[j].pos;
//		d = (pos_i[0] - pos_j[0])*(pos_i[0] - pos_j[0])+
//		(pos_i[1] - pos_j[1])*(pos_i[1] - pos_j[1]);
//		if(marker_holder[i].area >marker_holder[j].area ) {
//		    if( d <marker_holder[i].area / 4 ) {
//			marker_holder[j].area = 0;
//		    }
//		}else{
//		    if( d < marker_holder[j].area / 4 ) {
//			marker_holder[i].area = 0;
//		    }
//		}
//	    }
//	}
//	みつかったマーカーを整理する。
//	エリアが0のマーカーを外した配列を作って、その数もついでに計算
//	for(i=0;i<marker_num2;i++){
//	    if(marker_holder[i].area==0.0){
//		continue;
//	    }
//	    marker_info2_array[marker_num]=marker_holder[i];
//	    marker_num++;
//	}        
//	for( i=0; i < marker_num2; i++ ) {
//	if( marker_info2_array[i].area == 0.0 ) {
//	for( j=i+1; j < marker_num2; j++ ){
//	marker_info2_array[j-1] = marker_info2_array[j];
//	}
//	marker_num2--;
//	}
//	}
//	発見したマーカー数をセット
//	marker_num=marker_num2;//*marker_num = marker_num2;
//	return( &(marker_info2[0]) );
	return;
    }

}
