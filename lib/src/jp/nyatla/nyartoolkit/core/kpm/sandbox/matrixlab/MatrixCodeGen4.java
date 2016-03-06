package jp.nyatla.nyartoolkit.core.kpm.sandbox.matrixlab;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatrixCodeGen4 extends MatTable{
	public MatrixCodeGen4(int i_size) {
		super(i_size);
	}
	public class Tag{
		public int ref_count;
		public int value_id;
		public int lv;
		public String getValueName(){
			return "v"+value_id;
		}
		public Tag(int i_ref_count,int i_value_id){
			this.ref_count=i_ref_count;
			this.value_id=i_value_id;
			this.lv=-1;
			return;
		}
	}
	public class ElementMap extends HashMap<String,Tag>
	{
		private int serial_number=0;
		private static final long serialVersionUID = -5465103849523547316L;
		public Tag register(String i_key)
		{
			Tag t=null;
			if(this.get(i_key)==null){
				t=new Tag(1,serial_number);
				this.put(i_key,t);
				serial_number++;
			}else{
				t=this.get(i_key);
				t.ref_count++;
			}
			return t;
		}
	};
	protected MatrixCodeGen4(int i_size,MatItem[][] t) {
		super(i_size,t);
	}
	public class Result{
		public int lv;
	}
	/**
	 * 絶対値の文字列を返す。
	 * @return
	 */
	public String absStr(ElementMap map,int flag,Result ret){
		if(this._size==2){
			String s11=this._table[0][0].getStr();
			String s12=this._table[0][1].getStr();
			String s21=this._table[1][0].getStr();
			String s22=this._table[1][1].getStr();
			String v1=String.format("%s*%s",s11,s22);
			String v2=String.format("%s*%s",s12,s21);
			String v3=String.format("(%s)-(%s)",v1,v2);
			Tag vt3=map.register(v3);
			ret.lv=2;
				return vt3.getValueName();
			
		}else{
			String s="";
			for(int i=0;i<this._size;i++){
				MatrixCodeGen4 c=new MatrixCodeGen4(this._size-1,this.getCofactor(0,i));
				String v1=this._table[0][i].getStr();
				String v2=c.absStr(map,1,ret);
				Tag vt1=map.register(String.format("(%s)*(%s)",v1,v2));
				s+=vt1.getValueName();
//				s+=String.format("%s*(%s)",v1,v2);
				if(i!=this._size-1){
					s+=this.getCofactorFlag(0,i+1)>0?"+":"-";
				}
			}
			s="("+s+")";
			Tag vt4=map.register(s);
			
			if(flag>0){
				return vt4.getValueName();
			}else{
				return "(-"+vt4.getValueName()+")";
			}
		}
	}
	public String inversMatrix()
	{
		Result ret=new Result();
		ElementMap map=new ElementMap();
		String matstr="double det="+this.absStr(map,1,ret)+";\n";
		for(int r=0;r<this._size;r++){
			for(int c=0;c<this._size;c++){
				int f=this.getCofactorFlag(r, c);
				String val=new MatrixCodeGen4(this._size-1,this.getCofactor(r, c)).absStr(map,f,ret);
				//名前は転値
				matstr+=String.format("double M%d%d=(%s)/det;\n",c,r,val);
			}				
		}
		//MAPの結果をListに転送
		List<Map.Entry<String, Tag>> l=new ArrayList<Map.Entry<String, Tag>>();
		for(Map.Entry<String, Tag> e : map.entrySet()){
			l.add(e);
		}
		//順番にソート
		Collections.sort(l, new Comparator<Map.Entry<String, Tag>>(){			 
            @Override
            public int compare(
            		Map.Entry<String, Tag> entry1, Map.Entry<String, Tag> entry2) {
                return entry1.getValue().value_id<entry2.getValue().value_id?-1:1;
            }
        });
		/*
		//参照カウント1の値を置き換え
		for(int i=0;i<l.size();){
			Map.Entry<String, Tag> e=l.get(i);
			if(e.getValue().ref_count>1){
				i++;
				continue;
			}
			String ek=e.getKey();
			String ev=e.getValue().getValueName();
			//参照カウント1を含む行を探す
			for(int i2=0;i2<l.size();i2++){
				String t1=l.get(i2).getKey();
				if(t1.contains(ev)){
					System.out.println();					
				}
			}
			i++;
		}*/
		String elemstr="";
		for(Map.Entry<String, Tag> e : l){
			String sk=e.getKey();
			elemstr+=("double "+map.get(sk).getValueName()+"="+sk+";//"+e.getValue().ref_count+"\n");
		}
		return elemstr+matstr;
	}
	public static void main(String[] args){
		MatrixCodeGen4 m33=new MatrixCodeGen4(8);
		String s=m33.inversMatrix();
//		System.out.println(m33.inversMatrix());
		try {
			File file = new File("d:\\mat.txt");
			FileWriter filewriter = new FileWriter(file);
			filewriter.write(s);
			filewriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}	
}
