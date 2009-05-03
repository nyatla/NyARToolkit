package jp.nyatla.nyartoolkit.dev;



public class Solver
{
	public String[][] _data=new String[4][6];
	public String[] _temp=new String[6];
	public Solver()
	{
		this._data[0][0]="a";
		this._data[0][1]="b";
		this._data[0][2]="c";
		this._data[0][3]="d";
		this._data[0][4]="e";
		this._data[0][5]="1";
		
		this._data[1][0]="f";
		this._data[1][1]="g";
		this._data[1][2]="h";
		this._data[1][3]="i";
		this._data[1][4]="j";
		this._data[1][5]="1";
		
		this._data[2][0]="k";
		this._data[2][1]="l";
		this._data[2][2]="m";
		this._data[2][3]="n";
		this._data[2][4]="o";
		this._data[2][5]="1";

		this._data[3][0]="p";
		this._data[3][1]="q";
		this._data[3][2]="r";
		this._data[3][3]="s";
		this._data[3][4]="t";
		this._data[3][5]="1";
	}
	public void dump()
	{
		for(int i=0;i<this._data.length;i++){
			for(int i2=0;i2<this._data[i].length;i2++){
				System.out.print(this._data[i][i2]+" , ");
			}			
			System.out.println(" ");
		}
	}
	public boolean isSingle(String i_str)
	{
		int c=0;
		for(int i=0;i<i_str.length();i++){
			if(i_str.charAt(i)=='('){
				c++;
			}
			if(i_str.charAt(i)==')'){
				c--;
				if(c==0 && i!=i_str.length()-1){
					return false;//括弧必要
				}
			}
		}
		return true;//括弧不要
	}
	
	public void div(int i_r,String i_e,String[] o_row) throws Exception
	{
		if(i_e=="0"){
			throw new Exception();			
		}else if(i_e=="1"){
			return;
		}
		String[] l=this._data[i_r];
		for(int i=0;i<o_row.length;i++){
			if(l[i]==i_e){
				o_row[i]="1";
			}else if(l[i]=="0"){
				o_row[i]="0";
			}else{
				String s1=isSingle(l[i])?l[i]:"("+l[i]+")";
				String s2=isSingle(i_e)?i_e:"("+i_e+")";
				o_row[i]="("+s1+"/"+s2+")";
			}
		}
	}
	public void mul(int i_r,String i_e,String[] o_row)
	{
		String[] l=this._data[i_r];
		if(i_e=="0"){
			for(int i=0;i<o_row.length;i++){
				o_row[i]="0";
			}
		}else if(i_e=="1"){
			return;
		}else{
			for(int i=0;i<o_row.length;i++){
				if(l[i]=="0"){
					o_row[i]="0";
				}else if(l[i]=="1"){
					o_row[i]=i_e;
				}else{
					String s1=isSingle(l[i])?l[i]:"("+l[i]+")";
					String s2=isSingle(i_e)?i_e:"("+i_e+")";
					o_row[i]="("+s1+"*"+s2+")";
				}
			}
		}
	}
	public void subRow(int i_r1,String[] i_r,String[] o_row)
	{
		String[] l1=this._data[i_r1];
		String[] l2=i_r;
		for(int i=0;i<o_row.length;i++){
			if(l1[i]=="0"){
				o_row[i]=l2[i];
			}else if(l2[i]=="0"){
				o_row[i]=l1[i];
			}else if(l2[i]==l1[i]){
				o_row[i]="0";
			}else{
				String s1=isSingle(l1[i])?l1[i]:"("+l1[i]+")";
				String s2=isSingle(l2[i])?l2[i]:"("+l2[i]+")";
				o_row[i]="("+s1+"-"+s2+")";
			}
		}
	}
	public static void main(String[] args)
	{
		try {
			Solver n=new Solver();

			for(int i=0;i<4;i++){
				for(int i2=0;i2<i;i2++){
					n.mul(i2,n._data[i][i2],n._temp);
					n.subRow(i,n._temp,n._data[i]);					
				}
				n.div(i,n._data[i][i],n._data[i]);
			}
			n.dump();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}	
}
