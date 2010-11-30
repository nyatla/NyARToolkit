/* 
 * PROJECT: NyARToolkit Java3D utilities.
 * --------------------------------------------------------------------------------
 * The MIT License
 * Copyright (c) 2008 nyatla
 * airmail(at)ebony.plala.or.jp
 * http://nyatla.jp/nyartoolkit/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */
package jp.nyatla.nyartoolkit.java3d.utils;

import javax.media.j3d.*;
/**
 * 
 * NyARToolkitのBehaviorのリスナ
 *
 */
public interface NyARSingleMarkerBehaviorListener
{
    /**
     * このリスナは、リスナにマーカーに連動してオブジェクトを操作するチャンスを与えます。
     * リスナはNyARSingleMarkerBehavior関数内のprocessStimulus関数から呼び出されます。
     * 
     * @param i_is_marker_exist
     * マーカーが存在する場合true、存在しない場合、falseです。
     * @param i_transform3d
     * マーカーが存在する場合、その変換行列が指定されます。
     * i_is_marker_existがtrueの時だけ有効です。
     */
    public void onUpdate(boolean i_is_marker_exist,Transform3D i_transform3d);
}
