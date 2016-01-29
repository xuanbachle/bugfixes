package test

/**
 * Created by xuanbach32bit on 4/22/15.
 */
trait Bar[+T]{
  def  comp[B >: T](b: B):T
}
case class XBar() extends Bar[Int]{
  override def comp[B >: Int](b:B):Int = b.asInstanceOf[Int]
}

trait nonBar[T]{
  def  comp(b: T):T
}

case class nonXBar() extends nonBar[Int]{
  override def comp(b:Int):Int = b
}


abstract class FooScala[E <: Bar[Any]](i:Int) {
  def this(){
    this(1)
  }

}
