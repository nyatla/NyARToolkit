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
package jp.nyatla.nyartoolkit.core.rasterreader;

public interface INyARBufferReader
{
	public final static int BUFFERTYPE_NULL = 0x00000001;

	public final static int BUFFERTYPE_BYTE1D_R8G8B8 = 0x00010001;

	public final static int BUFFERTYPE_BYTE1D_B8G8R8X8 = 0x00010002;

	public final static int BUFFERTYPE_INT1D_G8 = 0x00020001;

	public final static int BUFFERTYPE_INT2D = 0x00030001;

	public final static int BUFFERTYPE_INT2D_G1 = 0x00030002;

	public Object getBuffer();

	public int getBufferType();
}
