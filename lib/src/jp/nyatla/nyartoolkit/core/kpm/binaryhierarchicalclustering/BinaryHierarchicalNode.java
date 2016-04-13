/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *  Copyright 2013-2015 Daqri, LLC.
 *  Author(s): Chris Broaddus
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 *  Copyright (C)2016 Ryo Iizuka
 * 
 * NyARToolkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * NyARToolkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and to
 * copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module
 * which is neither derived from nor based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you
 * are not obligated to do so. If you do not wish to do so, delete this exception
 * statement from your version.
 * 
 */
package jp.nyatla.nyartoolkit.core.kpm.binaryhierarchicalclustering;

import jp.nyatla.nyartoolkit.core.kpm.freak.FreakFeaturePoint;
import jp.nyatla.nyartoolkit.core.kpm.keyframe.FreakMatchPointSetStack;



public class BinaryHierarchicalNode
{

	// ID of the node
	final public int id;

	// Feature center
	final public FreakFeaturePoint center;

	// True if a leaf node
	final public boolean is_leaf;

	// Child nodes
	final public BinaryHierarchicalNode[] children;

	// Index of the features at this node
	final public FreakMatchPointSetStack.Item[] reserv_index;
	
	
	public BinaryHierarchicalNode(int id,FreakFeaturePoint i_feature,boolean i_is_leaf,FreakMatchPointSetStack.Item[] i_reserv_index,BinaryHierarchicalNode[] i_children)
	{
		this.is_leaf=i_is_leaf;
		this.id=id;
    	this.reserv_index=i_reserv_index;
		if(i_feature!=null){
			this.center=i_feature;
		}else{
			this.center=null;
		}
    	this.children=i_children;
	}
	
}
