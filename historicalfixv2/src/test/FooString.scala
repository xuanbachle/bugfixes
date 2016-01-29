package test

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

/**
 * Created by xuanbach32bit on 4/22/15.
 */
/*
class FooString extends Foo[String] {
  override def bar(c: Class[String]): String = "hello, world";
}
*/
class FooString(i:Int) extends FooScala[XBar] {
  var k =0

  def funcGeneric(a:Int,b:Bar[Any]): Unit = ()
  def funcGenericNonVar(a:Int,b:nonBar[Any]): Unit = ()
  def callGeneric() = funcGeneric(k,new XBar)
  //def callGenericNonVar() = funcGenericNonVar(k,new nonXBar) // uncomment to see the effect on non covariant type
  def bar(c: Class[String]): String = c.newInstance().asInstanceOf[String];
}
