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

public class MatrixCodeGen2 extends MatTable{
	public MatrixCodeGen2(int i_size) {
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
	protected MatrixCodeGen2(int i_size,MatItem[][] t) {
		super(i_size,t);
	}
	public class Result{
		public int lv;
	}
	/**
	 * 絶対値の文字列を返す。
	 * @return
	 */
	public String absStr(int flag,ElementMap map,Result ret){
		if(this._size==2){
			String s11=this._table[0][0].getStr();
			String s12=this._table[0][1].getStr();
			String s21=this._table[1][0].getStr();
			String s22=this._table[1][1].getStr();
			String v1=s11+"*"+s22;
			String v2=s12+"*"+s21;
			//
			Tag vt1=map.register(v1);
			Tag vt2=map.register(v2);
			//
			String v3;
			if(flag>0){
				v3=String.format("%s-%s",vt1.getValueName(),vt2.getValueName());
			}else{
				v3=String.format("%s-%s",vt2.getValueName(),vt1.getValueName());
			}
			Tag vt3=map.register(v3);
			ret.lv=2;
			return vt3.getValueName();
			
		}else{
			String s="";
			for(int i=0;i<this._size;i++){
				MatrixCodeGen2 c=new MatrixCodeGen2(this._size-1,this.getCofactor(0,i));
				String v1=this._table[0][i].getStr();
				String v2=c.absStr(flag,map,ret);
				
//				Tag vt1=map.register(String.format("%s*(%s)",v1,v2));
//				s+=vt1.getValueName();
				s+=String.format("%s*(%s)",v1,v2);
				if(i!=this._size-1){
					s+=this.getCofactorFlag(0,i+1)>0?"+":"-";
				}
			}
			return s;
		}
	}
	public String inversMatrix()
	{
		Result ret=new Result();
		ElementMap map=new ElementMap();
		String s="double det="+this.absStr(1,map,ret)+";\n";
		for(int r=0;r<this._size;r++){
			for(int c=0;c<this._size;c++){
				String val=new MatrixCodeGen2(this._size-1,this.getCofactor(r, c)).absStr(this.getCofactorFlag(r, c),map,ret);
				//名前は転値
				s+=String.format("double M%d%d=(%s)/det;\n",c,r,val);
			}				
		}
		//Listに転送
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
		//参照カウント1の
		
		for(Map.Entry<String, Tag> e : l){
			String sk=e.getKey();
			s+=("double "+map.get(sk).getValueName()+"="+sk+";//"+e.getValue().ref_count+"\n");
		}
		return s;
	}
	public static void main(String[] args){
		MatrixCodeGen2 m33=new MatrixCodeGen2(8);
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
