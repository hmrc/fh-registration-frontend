package uk.gov.hmrc.fhregistrationfrontend.testsupport

import play.api.Logging
import annotation.tailrec
import java.net.ServerSocket
import scala.language.postfixOps

object Port extends Logging {
  val rnd = new scala.util.Random
  val range = 8000 to 39999
  val usedPorts = List[Int]()

  @tailrec
  def randomAvailable: Int =
    range(rnd.nextInt(range length)) match {
      case 8080 => randomAvailable
      case 8090 => randomAvailable
      case p: Int =>
        available(p) match {
          case false =>
            logger.debug(s"Port $p is in use, trying another")
            randomAvailable
          case true =>
            logger.debug("Taking port : " + p)
            usedPorts :+ p
            p
        }
    }

  private def available(p: Int): Boolean = {
    var socket: ServerSocket = null
    try
      if (!usedPorts.contains(p)) {
        socket = new ServerSocket(p)
        socket.setReuseAddress(true)
        true
      } else {
        false
      }
    catch {
      case t: Throwable => false
    } finally
      if (socket != null) socket.close()
  }
}
