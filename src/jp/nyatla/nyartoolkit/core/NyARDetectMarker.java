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
import jp.nyatla.util.IntValue;
/**
 * イメージからマーカー情報を検出するクラス。
 * このクラスは、arDetectMarker2.cとの置き換えになります。
 * ラベリング済みのラスタデータからマーカー位置を検出して、結果を保持します。
 *
 */
public class NyARDetectMarker {
    private static final int AR_AREA_MAX=100000;//#define   AR_AREA_MAX      100000
    private static final int AR_AREA_MIN=70;//#define   AR_AREA_MIN          70

    private int area_max=AR_AREA_MAX;
    private int area_min=AR_AREA_MIN;
    private NyARMarker[] marker_holder;	//マーカーデータの保持配列
    private NyARMarker[] marker_info2_array;	//マーカーデータのインデックス配列
    private int marker_num;
    private int width,height;
    /**
     * 最大i_squre_max個のマーカーを検出するクラスを作成する。
     * @param i_width
     * @param i_height
     * @param i_squre_max
     */
    public NyARDetectMarker(int i_width,int i_height,int i_squre_max)
    {
	width =i_width;
	height=i_height;
	marker_holder=new NyARMarker[i_squre_max];
	marker_info2_array=new NyARMarker[i_squre_max];
    }
    public int getMarkerNum()
    {
	return marker_num;
    }
    public NyARMarker getMarker(int idx) throws NyARException
    {
	if(idx>=marker_num){
	    throw new NyARException();
	}
	return marker_info2_array[idx];
    }
    /**
     * static int get_vertex( int x_coord[], int y_coord[], int st,  int ed,double thresh, int vertex[], int *vnum)
     * 関数の代替関数
     * @param x_coord
     * @param y_coord
     * @param st
     * @param ed
     * @param thresh
     * @param vertex
     * @param vnum
     * @return
     */
    private static boolean get_vertex( int[] x_coord, int[] y_coord, int st,  int ed,double thresh, int vertex[],IntValue vnum)
    {
        double   d, dmax;
        double   a, b, c;
        int      i, v1=0;
        
        a = y_coord[ed] - y_coord[st];
        b = x_coord[st] - x_coord[ed];
        c = x_coord[ed]*y_coord[st] - y_coord[ed]*x_coord[st];
        dmax = 0;
        for(i=st+1;i<ed;i++) {
            d = a*x_coord[i] + b*y_coord[i] + c;
            if( d*d > dmax ) {
        	dmax = d*d;
        	v1 = i;
            }
        }
        if( dmax/(a*a+b*b) > thresh ) {
            if(!get_vertex(x_coord, y_coord, st,  v1, thresh, vertex, vnum)){
        	return false;
            }
            if( vnum.get() > 5 ){
        	return false;
            }
            vertex[vnum.get()] = v1;//vertex[(*vnum)] = v1;
            vnum.inc();//(*vnum)++;
        
            if(!get_vertex(x_coord, y_coord, v1,  ed, thresh, vertex, vnum)){
               	return false;
            }
        }
        return true;
    }
    /**
     * static int arDetectMarker2_check_square( int area, ARMarkerInfo2 *marker_info2, double factor )
     * 関数の代替関数
     * @param area
     * @param i_marker_info2
     * @param factor
     * @return
     */
    private static boolean check_square( int area, NyARMarker i_marker_info2, double factor )
	{
	    int             sx, sy;
	    int             dmax, d, v1;
	    int[] vertex=new int[10];//int             vertex[10]
	    int[] wv1=new int[10],wv2=new int[10];//int wv1[10],wv2[10];
	    int v2;//	    int   wvnum1,wvnum2,v2;
	    double          thresh;
	    int             i;
	    IntValue wvnum1=new IntValue(),wvnum2=new IntValue();


	    dmax = 0;
	    v1 = 0;
	    sx = i_marker_info2.x_coord[0];//sx = marker_info2->x_coord[0];
	    sy = i_marker_info2.y_coord[0];//sy = marker_info2->y_coord[0];
	    for(i=1;i<i_marker_info2.coord_num-1;i++){//for(i=1;i<marker_info2->coord_num-1;i++) {
	        d = (i_marker_info2.x_coord[i]-sx)*(i_marker_info2.x_coord[i]-sx)+ (i_marker_info2.y_coord[i]-sy)*(i_marker_info2.y_coord[i]-sy);
	        if( d > dmax ) {
	            dmax = d;
	            v1 = i;
	        }
	    }

	    thresh = (area/0.75) * 0.01 * factor;
//	    vnum = 1;
	    vertex[0] = 0;
	    wvnum1.set(0);//	    wvnum1 = 0;
	    wvnum2.set(0);//	    wvnum2 = 0;

	    if(!get_vertex(i_marker_info2.x_coord, i_marker_info2.y_coord, 0,  v1,thresh, wv1, wvnum1)){	    //if( get_vertex(marker_info2->x_coord, marker_info2->y_coord, 0,  v1,thresh, wv1, &wvnum1) < 0 ) {
	        return false;
	    }
	    if(!get_vertex(i_marker_info2.x_coord, i_marker_info2.y_coord,v1,  i_marker_info2.coord_num-1, thresh, wv2, wvnum2)) {//if(get_vertex(marker_info2->x_coord, marker_info2->y_coord,v1,  marker_info2->coord_num-1, thresh, wv2, &wvnum2) < 0 ) {
	        return false;
	    }

	    if( wvnum1.get() == 1 && wvnum2.get() == 1 ) {//if( wvnum1 == 1 && wvnum2 == 1 ) {
	        vertex[1] = wv1[0];
	        vertex[2] = v1;
	        vertex[3] = wv2[0];
	    }else if( wvnum1.get() > 1 && wvnum2.get() == 0 ) {//}else if( wvnum1 > 1 && wvnum2 == 0 ) {
	        v2 = v1 / 2;
	        wvnum1.set(0);wvnum2.set(0);//wvnum1 = wvnum2 = 0;
	        if(!get_vertex(i_marker_info2.x_coord, i_marker_info2.y_coord,0,  v2, thresh, wv1, wvnum1)) {
	            return false;
	        }
	        if(!get_vertex(i_marker_info2.x_coord, i_marker_info2.y_coord,v2,  v1, thresh, wv2, wvnum2)) {
	            return false;
	        }
	        if( wvnum1.get() == 1 && wvnum2.get() == 1 ) {
	            vertex[1] = wv1[0];
	            vertex[2] = wv2[0];
	            vertex[3] = v1;
	        }else{
	            return false;
	        }
	    }else if( wvnum1.get() == 0 && wvnum2.get() > 1 ) {
	        v2 = (v1 + i_marker_info2.coord_num-1) / 2;
	        
	        wvnum1.set(0);wvnum2.set(0);//wvnum1 = wvnum2 = 0;
	        if(!get_vertex(i_marker_info2.x_coord, i_marker_info2.y_coord,v1, v2, thresh, wv1,wvnum1)) {
	            return false;
	        }
	        if(!get_vertex(i_marker_info2.x_coord, i_marker_info2.y_coord,v2, i_marker_info2.coord_num-1, thresh, wv2, wvnum2)) {
	            return false;
	        }
	        if( wvnum1.get() == 1 && wvnum2.get() == 1 ) {
	            vertex[1] = v1;
	            vertex[2] = wv1[0];
	            vertex[3] = wv2[0];
	        }
	        else {
	            return false;
	        }
	    }
	    else {
	        return false;
	    }

	    i_marker_info2.vertex[0] = vertex[0];
	    i_marker_info2.vertex[1] = vertex[1];
	    i_marker_info2.vertex[2] = vertex[2];
	    i_marker_info2.vertex[3] = vertex[3];
	    i_marker_info2.vertex[4] = i_marker_info2.coord_num-1;

	    return true;
	}
    /**
     * int arGetContour( ARInt16 *limage, int *label_ref,int label, int clip[4], ARMarkerInfo2 *marker_info2 )
     * 関数の代替品
     * detectMarker関数から使う関数です。marker_holder[i_holder_num]にオブジェクトが無ければまず新規に作成し、もし
     * 既に存在すればそこにマーカー情報を上書きして記録します。
     * @param limage
     * @param label_ref
     * @param label
     * @param clip
     * @return
     * 	検出したマーカーからマーカーオブジェクトを生成して返す。
     * @throws NyARException
     */
    private NyARMarker arGetContour(int i_holder_num,short[][] limage, int[] label_ref,int label, int[] clip) throws NyARException
    {
        final int[] xdir={0,  1, 1, 1, 0,-1,-1,-1}; //static int      xdir[8] = { 0, 1, 1, 1, 0,-1,-1,-1};
        final int[] ydir={-1,-1, 0, 1, 1, 1, 0,-1};//static int      ydir[8] = {-1,-1, 0, 1, 1, 1, 0,-1};
        //ShortPointer p1;//ARInt16         *p1;
        int             sx=0, sy=0, dir;
        int             dmax, d, v1=0;
        int             i, j;
    
        j = clip[2];
        //p1=ShortPointer.wrap(limage,j*xsize+clip.get());//p1 = &(limage[j*xsize+clip[0]]);
        for( i = clip[0]; i <= clip[1]; i++){//for( i = clip[0]; i <= clip[1]; i++, p1++ ) {
            if(limage[j][i] > 0 && label_ref[(limage[j][i])-1] == label ) {//if( *p1 > 0 && label_ref[(*p1)-1] == label ) {
                sx = i; sy = j;
                break;
            }
        }
        if(i> clip[1]){//if( i > clip[1] ) {
            System.out.println("??? 1");//printf();
            throw new NyARException();//return(-1);
        }
        
        //マーカーホルダが既に確保済みかを調べる
        if(marker_holder[i_holder_num]==null){
            //確保していなければ確保
            marker_holder[i_holder_num]=new NyARMarker();
        }
        NyARMarker marker_ref=marker_holder[i_holder_num];

    
        marker_ref.coord_num=1;//marker_info2->coord_num = 1;
        marker_ref.x_coord[0]=sx;//marker_info2->x_coord[0] = sx;
        marker_ref.y_coord[0]=sy;//marker_info2->y_coord[0] = sy;
        dir = 5;
    
        for(;;){
            int r=marker_ref.y_coord[marker_ref.coord_num-1];
            int c=marker_ref.x_coord[marker_ref.coord_num-1];
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
            marker_ref.x_coord[marker_ref.coord_num]= marker_ref.x_coord[marker_ref.coord_num-1] + xdir[dir];//marker_info2->x_coord[marker_info2->coord_num]= marker_info2->x_coord[marker_info2->coord_num-1] + xdir[dir];
            marker_ref.y_coord[marker_ref.coord_num]= marker_ref.y_coord[marker_ref.coord_num-1] + ydir[dir];//marker_info2->y_coord[marker_info2->coord_num]= marker_info2->y_coord[marker_info2->coord_num-1] + ydir[dir];
            if( marker_ref.x_coord[marker_ref.coord_num] == sx && marker_ref.y_coord[marker_ref.coord_num] == sy ){
                break;
            }
            marker_ref.coord_num++;
            if( marker_ref.coord_num == marker_ref.x_coord.length-1){//if( marker_info2.coord_num == Config.AR_CHAIN_MAX-1 ){
                System.out.println("??? 3");//printf("??? 3\n");
                throw new NyARException();//return(-1);
            }
        }
    
        dmax = 0;
        for(i=1;i<marker_ref.coord_num;i++) {//	for(i=1;i<marker_info2->coord_num;i++) {
            d = (marker_ref.x_coord[i]-sx)*(marker_ref.x_coord[i]-sx)+ (marker_ref.y_coord[i]-sy)*(marker_ref.y_coord[i]-sy);//	  d = (marker_info2->x_coord[i]-sx)*(marker_info2->x_coord[i]-sx)+ (marker_info2->y_coord[i]-sy)*(marker_info2->y_coord[i]-sy);
            if( d > dmax ) {
    		dmax = d;
    		v1 = i;
            }
        }

        int[]      wx=new int[v1];//new int[Config.AR_CHAIN_MAX];
        int[]      wy=new int[v1]; //new int[Config.AR_CHAIN_MAX];   
        for(i=0;i<v1;i++) {
            wx[i] = marker_ref.x_coord[i];//wx[i] = marker_info2->x_coord[i];
            wy[i] = marker_ref.y_coord[i];//wy[i] = marker_info2->y_coord[i];
        }
        for(i=v1;i<marker_ref.coord_num;i++) {//for(i=v1;i<marker_info2->coord_num;i++) {
            marker_ref.x_coord[i-v1] = marker_ref.x_coord[i];//marker_info2->x_coord[i-v1] = marker_info2->x_coord[i];
            marker_ref.y_coord[i-v1] = marker_ref.y_coord[i];//marker_info2->y_coord[i-v1] = marker_info2->y_coord[i];
        }
        for(i=0;i<v1;i++) {
            marker_ref.x_coord[i-v1+marker_ref.coord_num] = wx[i];//marker_info2->x_coord[i-v1+marker_info2->coord_num] = wx[i];
            marker_ref.y_coord[i-v1+marker_ref.coord_num] = wy[i];//marker_info2->y_coord[i-v1+marker_info2->coord_num] = wy[i];
        }
        marker_ref.x_coord[marker_ref.coord_num] = marker_ref.x_coord[0];//marker_info2->x_coord[marker_info2->coord_num] = marker_info2->x_coord[0];
        marker_ref.y_coord[marker_ref.coord_num] = marker_ref.y_coord[0];//marker_info2->y_coord[marker_info2->coord_num] = marker_info2->y_coord[0];
        marker_ref.coord_num++;//marker_info2->coord_num++;
    
        return marker_ref;
    }
    	/**
    	 * ARMarkerInfo2 *arDetectMarker2( ARInt16 *limage, int label_num, int *label_ref,int *warea, double *wpos, int *wclip,int area_max, int area_min, double factor, int *marker_num )
    	 * 関数の代替品
    	 * ラベリング情報からマーカー一覧を作成して保持します。
    	 * この関数を実行すると、前回のdetectMarker関数で計算した保持値は破壊されます。
    	 * @param i_labeling
    	 * ラベリング済みの情報を持つラベリングオブジェクト
    	 * @param factor
    	 * 何かの閾値？
    	 * @return
    	 * @throws NyARException
    	 */
// 	public void detectMarker(short[][] limage,int label_num,int[] label_ref,int[] warea,double[] wpos,int[] wclip,int area_max, int area_min, double factor) throws JartkException
    public void detectMarker(NyARLabeling i_labeling,double factor) throws NyARException
    {
        int               xsize, ysize;
        int               marker_num2;
        double            d;
        int[] warea  	=i_labeling.getArea();
        int label_num	=i_labeling.getLabelNum();
        int[][] wclip	=i_labeling.getClip();
        double[] wpos	=i_labeling.getPos();
        short[][] limage	=i_labeling.getLabelImg();
        int[] label_ref	=i_labeling.getLabelRef();
        
        marker_num=0;
        xsize =width;
        ysize =height;
        
        marker_num2 = 0;
        for(int i=0; i<label_num; i++ ) {
            if( warea[i] < area_min || warea[i] > area_max ){
                continue;
            }
            if( wclip[i][0] == 1 || wclip[i][1] == xsize-2 ){//if( wclip[i*4+0] == 1 || wclip[i*4+1] == xsize-2 ){
                continue;
            }
            if( wclip[i][2] == 1 || wclip[i][3] == ysize-2 ){//if( wclip[i*4+2] == 1 || wclip[i*4+3] == ysize-2 ){
                continue;
            }
            //ret = arGetContour( limage, label_ref, i+1,&(wclip[i*4]), &(marker_info2[marker_num2]));
            arGetContour(marker_num2,limage, label_ref, i+1,wclip[i]);
            
            boolean ret = check_square( warea[i], marker_holder[marker_num2], factor );//ret = check_square( warea[i], &(marker_info2[marker_num2]), factor );
            if(!ret){
        	//後半で整理するからここはいらない。//        	marker_holder[marker_num2]=null;
                continue;
            }
            marker_holder[marker_num2].area   = warea[i];
            marker_holder[marker_num2].pos[0] = wpos[i*2+0];
            marker_holder[marker_num2].pos[1] = wpos[i*2+1];
            marker_num2++;
            //マーカーリストが上限に達した
            if( marker_num2 == marker_holder.length){
                break;
            }
        }
        for(int i=0; i < marker_num2; i++ ) {
            for(int j=i+1; j < marker_num2; j++ ) {
                d = (marker_holder[i].pos[0] - marker_holder[j].pos[0])*
                    (marker_holder[i].pos[0] - marker_holder[j].pos[0])+
                    (marker_holder[i].pos[1] - marker_holder[j].pos[1])*
                    (marker_holder[i].pos[1] - marker_holder[j].pos[1]);
                if(marker_holder[i].area >marker_holder[j].area ) {
                    if( d <marker_holder[i].area / 4 ) {
                	marker_holder[j].area = 0;
                    }
                }else{
                    if( d < marker_holder[j].area / 4 ) {
                	marker_holder[i].area = 0;
                    }
                }
            }
        }
        //みつかったマーカーを整理する。
        //エリアが0のマーカーを外した配列を作って、その数もついでに計算
        for(int i=0;i<marker_num2;i++){
            if(marker_holder[i].area==0.0){
        	continue;
            }
            marker_info2_array[marker_num]=marker_holder[i];
            marker_num++;
        }        
//        for( i=0; i < marker_num2; i++ ) {
//            if( marker_info2_array[i].area == 0.0 ) {
//                for( j=i+1; j < marker_num2; j++ ){
//                    marker_info2_array[j-1] = marker_info2_array[j];
//                }
//                marker_num2--;
//            }
//        }
        //発見したマーカー数をセット
//        marker_num=marker_num2;//*marker_num = marker_num2;
        //return( &(marker_info2[0]) );
    }

}
