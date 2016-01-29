package randomController

import java.util.Random

import org.apache.log4j.Logger

/**
 * Created by larcuser on 17/10/15.
 */
object RandomController {
  //private var rng: Random = null
  private val logger = Logger.getLogger(RandomController.getClass)

  /*def setRandomGenerator(rng: Random) ={
    if(this.rng == null){
      this.rng = rng
    }else{
      logger.error("Trying to reset random generator! Forbidden!")
    }
  }*/

  //def getRandomGenerator() = rng

  def randomPick[T](pick: Seq[T], rng: Random): T = return pick(rng.nextInt(pick.size))
}
