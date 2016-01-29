package test

/**
 * Created by larcuser on 16/10/15.
 */
class ConstructorTest {
  var k =0
  def this(i: Int)={
    this()
    k = i
  }
  def printk() = println("k = "+ k)
}

object App{
  def main (args: Array[String]) {
    val c = new ConstructorTest(10)
    c.printk()
  }
}
