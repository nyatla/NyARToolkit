package jp.nyatla.nyartoolkit.core.rasteranalyzer;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.raster.*;
import jp.nyatla.nyartoolkit.core.types.stack.*;
import jp.nyatla.nyartoolkit.core.types.*;

public class NyARRasterDetector_QrCodeEdge
{
    private NyARIntPointStack _result;

    public NyARRasterDetector_QrCodeEdge(int i_result_max)
    {
	this._result=new NyARIntPointStack(i_result_max);

    }
    public NyARIntPointStack geResult()
    {
	return this._result;
    }
    public void analyzeRaster(INyARRaster i_input) throws NyARException
    {
	assert(i_input.getBufferType()==TNyRasterType.BUFFERFORMAT_INT2D_BIN_8);

	//結果をクリア
	this._result.clear();

	TNyARIntSize size=i_input.getSize();
	int th=128;
	int x=0;
	int w1,b1,w2,b2,w3,b3;
	w1=b1=w2=b2=w3=b3=0;

	int[] line;
	int s_pos,e_pos;
	for(int y=size.h-1;y>=0;y--){
	    line=((int[][])i_input.getBufferObject())[y];
	    x=size.w;
	    while(x>=0){
		//w1の特定
		for(;x>=0;x--){
		    if(line[x]<th){
			break;
		    }
		    w1++;
		}
		//w1は3以上欲しいな。
		if(w1<3){
		    w1=0;
		    continue;
		}
		s_pos=x;
		//b1の特定
		for(;x>=0;x--){
		    if(line[x]>=th){
			break;
		    }
		    w1++;
		}
		//b1は1以上欲しいな。
		if(b1<1){
		    w1=b1=0;
		    continue;
		}

		//w2の特定
		for(;x>=0;x--){
		    if(line[x]<th){
			break;
		    }
		    w2++;
		}
		//w2とb1は2倍以上違わないこと
		if((b1>>1) != (w2>>1)){
		    w1=b1=w2=0;
		    continue;
		}

		//b2の特定
		for(;x>=0;x--){
		    if(line[x]>=th){
			break;
		    }
		    b2++;
		}
		//b2はw2の2倍以上あること！
		if((b2>>1) > (w2>>1)){
		    w1=b1=w2=b2=0;
		    continue;
		}

		//w3の特定
		for(;x>=0;x--){
		    if(line[x]<th){
			break;
		    }
		    w3++;
		}
		//w3とb1は2倍以上違わないこと
		if((b1>>1) != (w3>>1)){
		    w1=b1=w2=b2=w3=0;
		    continue;
		}

		//b3の特定
		for(;x>=0;x--){
		    if(line[x]<th){
			break;
		    }
		    b3++;
		}
		//b4とb1は2倍以上違わないこと
		if((b3>>1) != (b1>>1)){
		    w1=b1=w2=b2=w3=b3=0;
		    continue;
		}
		e_pos=x;
		/*コード特定→保管*/	    
		TNyARIntPoint item=this._result.reserv();
		item.x=e_pos-s_pos;
		item.y=y;
		/*次のコードを探す*/
		w1=b1=w2=b2=w3=b3=0;
	    }
	}
	return;
    }


}
