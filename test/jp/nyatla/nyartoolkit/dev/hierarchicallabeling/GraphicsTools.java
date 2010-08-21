package jp.nyatla.nyartoolkit.dev.hierarchicallabeling;

import java.awt.Graphics;

import jp.nyatla.nyartoolkit.core.types.*;

public class GraphicsTools
{
    public static void drawPolygon(Graphics i_g,NyARIntPoint2d[] i_pt,int i_number_of_pt)
    {
    	int[] x=new int[i_number_of_pt];
    	int[] y=new int[i_number_of_pt];
    	for(int i=0;i<i_number_of_pt;i++){
    		x[i]=i_pt[i].x;
    		y[i]=i_pt[i].y;
    	}
    	i_g.drawPolygon(x, y, i_number_of_pt);
    }
    public static void drawPolygon(Graphics i_g,NyARDoublePoint2d[] i_pt,int i_number_of_pt)
    {
    	int[] x=new int[i_number_of_pt];
    	int[] y=new int[i_number_of_pt];
    	for(int i=0;i<i_number_of_pt;i++){
    		x[i]=(int)i_pt[i].x;
    		y[i]=(int)i_pt[i].y;
    	}
    	i_g.drawPolygon(x, y, i_number_of_pt);
    }
}
