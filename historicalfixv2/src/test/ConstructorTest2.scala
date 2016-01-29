package test

/**
 * Created by larcuser on 16/10/15.
 */
class ConstructorTest2(i: Int) {
  var k = 0
  initblahblo()
  def initblahblo() : Unit = {
    k = i
  }
  def printk() = println("k = "+k)
}
object App2{
  def main (args: Array[String]) {
    val c = new ConstructorTest2(100)
    c.printk()
  }
}
