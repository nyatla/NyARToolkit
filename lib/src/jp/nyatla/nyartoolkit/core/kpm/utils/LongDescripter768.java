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
package jp.nyatla.nyartoolkit.core.kpm.utils;


/**
 * 768bitデスクプタ
 *
 */
public class LongDescripter768 extends LongDescripter
{
	public LongDescripter768()
	{
		super(96*8);
	}

	@Override
	final public int hammingDistance(LongDescripter i_value)
	{
		//assert i_value.bits==this.bits;
		long b;
		int c=0;
		for(int i=this._desc.length-1;i>=0;i--){
			b=this._desc[i]^i_value._desc[i];
	        b = (b & 0x5555555555555555L) + (b >> 1 & 0x5555555555555555L);
	        b = (b & 0x3333333333333333L) + (b >> 2 & 0x3333333333333333L);
	        b = (b & 0x0f0f0f0f0f0f0f0fL) + (b >> 4 & 0x0f0f0f0f0f0f0f0fL);
	        b = (b & 0x00ff00ff00ff00ffL) + (b >> 8 & 0x00ff00ff00ff00ffL);
	        b = (b & 0x0000ffff0000ffffL) + (b >> 16 & 0x0000ffff0000ffffL);
	        b = (b & 0x00000000ffffffffL) + (b >> 32 & 0x00000000ffffffffL);
	        c+=b;
		}
        return (int)c;
	}

}
