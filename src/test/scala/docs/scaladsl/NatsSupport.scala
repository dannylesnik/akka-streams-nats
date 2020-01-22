package docs.scaladsl

import akka.actor.ActorSystem
import io.nats.client.{Connection, Nats}

trait NatsSupport {


  //#init-actor-system
  implicit val system: ActorSystem = ActorSystem()
  //#init-actor-system

  val connection: Connection = Nats.connect("nats://localhost:4223")

  def subjectName:String

  val expectedMessage:String = "Hello World!!!"

  val message:String = "Hello World!!!"

  val responseMessage = "Got Your Message"


}
