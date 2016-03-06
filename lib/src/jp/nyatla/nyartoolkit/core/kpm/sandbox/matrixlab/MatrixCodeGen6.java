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

public class MatrixCodeGen6 extends MatTable{
	public MatrixCodeGen6(int i_size) {
		super(i_size);
	}
	abstract public class Section{
		public int flag;
		public Section(int i_flag){
			this.flag=i_flag;
		}
		abstract public String toString();
		public boolean equals(Object e)
		{
			Section te=((Section)e);
			return te.toString().compareTo(this.toString())==0;
		}
		public int hashCode()
		{
			return this.toString().hashCode() ^ this.flag;
		}		
	}
	public class ValueSection extends Section{
		final public String val;
		public ValueSection(String i_val,int i_flag){
			super(i_flag);
			this.val=i_val;
		}
		@Override
		public String toString()
		{
			return "("+val+")";
		}
	}	
	/** n項の和*/
	public class SumSection extends Section{
		final List<Section> l=new ArrayList<Section>();
		public SumSection(int i_flags)
		{
			super(i_flags);
		}
		@Override
		public String toString(){
			String s="";
			for(Section i:this.l){
				s+=(i.flag>0?"+":"-")+i.toString();
			}
			return "("+s.substring(1)+")";
		}
		public void add(Section s){
			this.l.add(s);
		}
	}
	/** 2項の積*/
	public class MulSection extends Section{
		final public Section first;
		final public Section second;
		public MulSection(Section f,Section s,int i_flags){
			super(i_flags);
			this.first=f;
			this.second=s;
		}
		@Override
		public String toString()
		{
			String f1=first.flag>0?"":"-";
			String f2=second.flag>0?"":"-";
			return "("+f1+first.toString()+")*("+f2+second.toString()+")";
		}
	}
	public class Tag{
		public int ref_count;
		public Section src_section;
		public Section dst_section;
		public int lv;
		public Tag(int i_ref_count,int i_value_id,Section s,int i_flag){
			this.ref_count=i_ref_count;
			this.lv=-1;
			this.src_section=s;
			this.dst_section=new ValueSection("v"+i_value_id,i_flag);
			return;
		}

	}
	public class ElementMap extends HashMap<Section,Tag>
	{
		private int serial_number=0;
		private static final long serialVersionUID = -5465103849523547316L;
		public Section register(Section i_key,int i_flag)
		{
			Tag t=null;
			if(this.get(i_key)==null){
				t=new Tag(1,serial_number,i_key,i_flag);
				this.put(i_key,t);
				serial_number++;
			}else{
				t=this.get(i_key);
				t.ref_count++;
			}
			return t.dst_section;
		}
	
	}
	protected MatrixCodeGen6(int i_size,MatItem[][] t) {
		super(i_size,t);
	}


	/**
	 * 絶対値の文字列を返す。
	 * @return
	 */
	public Section absStr(ElementMap map,int flag){
		if(this._size==2){
			String s11=this._table[0][0].getStr();
			String s12=this._table[0][1].getStr();
			String s21=this._table[1][0].getStr();
			String s22=this._table[1][1].getStr();
			String v3=String.format("(%s*%s-%s*%s)",s11,s22,s12,s21);
			Section r=new ValueSection(v3,flag);
			return map.register(r,flag);
		}else{
			SumSection ss=new SumSection(flag);
			for(int i=0;i<this._size;i++){
				MatrixCodeGen6 c=new MatrixCodeGen6(this._size-1,this.getCofactor(0,i));
				Section s=new MulSection(
					new ValueSection(this._table[0][i].getStr(),1),
					c.absStr(map,this.getCofactorFlag(0, i)),1);
				ss.add(s);
			}
			return map.register(ss,flag);
		}
	}
	public String inversMatrix()
	{
		ElementMap map=new ElementMap();
		String matstr="double det="+this.absStr(map,1).toString()+";\n";
		for(int r=0;r<this._size;r++){
			for(int c=0;c<this._size;c++){
				int f=this.getCofactorFlag(r, c);
				Section val=new MatrixCodeGen6(this._size-1,this.getCofactor(r, c)).absStr(map,f);
				//名前は転値
				matstr+=String.format("double M%d%d=(%s%s)/det;\n",c,r,val.flag>0?"":"-", val);
			}				
		}
		//MAPの結果をListに転送
		List<Map.Entry<Section, Tag>> l=new ArrayList<Map.Entry<Section, Tag>>();
		for(Map.Entry<Section, Tag> e : map.entrySet()){
			l.add(e);
		}
		//順番にソート
/*		Collections.sort(l, new Comparator<Map.Entry<String, Tag>>(){			 
            @Override
            public int compare(
            		Map.Entry<String, Tag> entry1, Map.Entry<String, Tag> entry2) {
                return entry1.getValue().value_id<entry2.getValue().value_id?-1:1;
            }
        });*/
		String elemstr="";
		for(Map.Entry<Section, Tag> e : l){
			Tag t=map.get(e.getKey());
			elemstr+=("double "+t.dst_section+"="+t.src_section+";//"+e.getValue().ref_count+"\n");
		}
		return elemstr+matstr;
	}
	public static void main(String[] args){
		MatrixCodeGen6 m33=new MatrixCodeGen6(8);
		String s=m33.inversMatrix();
		System.out.println(s);
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
