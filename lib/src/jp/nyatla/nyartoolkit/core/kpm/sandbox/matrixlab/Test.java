package jp.nyatla.nyartoolkit.core.kpm.sandbox.matrixlab;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Test {

		
	public static void main(String[] args){
		MatrixCodeGen2 m33=new MatrixCodeGen2(8);
		String s=m33.inversMatrix();
		System.out.println(m33.inversMatrix());
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
