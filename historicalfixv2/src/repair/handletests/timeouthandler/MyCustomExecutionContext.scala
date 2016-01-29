package repair.handletests.timeouthandler

/**
 * Created by xledinh on 9/4/15.
 */

import scala.concurrent._

import scala.concurrent.duration._


class MyCustomExecutionContext extends AnyRef with ExecutionContext {

    import ExecutionContext.Implicits.global

    @volatile var lastThread: Option[Thread] = None

    override def execute(runnable: Runnable): Unit = {

        ExecutionContext.Implicits.global.execute(new Runnable() {

             override def run() {

                lastThread = Some(Thread.currentThread)

                runnable.run()

              }

          })

      }

    override def reportFailure(t: Throwable): Unit = ???

  }



object MyCustomExecutionContext {

      def main(args: Array[String]) {

          implicit val exec = new MyCustomExecutionContext()

          val f = Future[Int] {

              do {/*println("Running...")*/} while (true); 1

            }

          var i = 0

          while(i< 100) {

             try {

                    println(i)

                    Await.result(f, 5 seconds) // 100% cpu here



                  } catch {

                  case e: TimeoutException =>

                      println("Stopping...")

                     exec.lastThread.getOrElse(throw new RuntimeException("Not started"))

                      .stop()// 0% cpu here

                    exec.lastThread

                }

              i += 1

            }

        }

}


