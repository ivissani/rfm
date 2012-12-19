/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import scala.io.Source
import scala.collection.Iterator

object csv {
  implicit def any2string(any: Any) = augmentString(any.asInstanceOf[String])
  
  /** Returns an iterator over the rows of given csv file. Each row is a tuple
    * of the same size as the number of columns.
    * 
    * @param filename
    * @param delimiter character separating fields
    * @param quotechar character that wraps a field
    * @param hasHeader if true the first row in the csv file will be excluded
    */
  def read(
    filename: String,
    delimiter: String = ",",
    quoteChar: String = "",
    hasHeader: Boolean = false): Iterator[Any] = {

    val fields = Source.fromFile(filename).getLines().next().split(quoteChar + delimiter + quoteChar).length
    
    val clazz = Class.forName("scala.Tuple" + fields).getConstructors.apply(0)

    val file = Source.fromFile(filename)
    
    val lines = file.getLines()
      .drop(if (hasHeader) 1 else 0)
      .filterNot(_.isEmpty())
    
    val lists = lines.map { l =>
      val arr = l.split(quoteChar + delimiter + quoteChar)
      arr(0) = arr(0).drop(quoteChar.length)
      arr(arr.length - 1) = arr(arr.length - 1).dropRight(quoteChar.length)
      arr
    }
    
    //Console print fields
    lists.map { t => clazz.newInstance(t: _*).asInstanceOf[Product] }
  }
}