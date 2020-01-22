package akka.stream.alpakka.nats

import java.util.Optional

import scala.util.{Failure, Success, Try}

object model {

  final class NatsSubscriptionResult private (val subject:String,replyTo:Option[String],body:Array[Byte] ){

    def getSubject: String = subject

    /** Java API */
    def getReplyTo: Optional[String] =
      replyTo match {
        case Some(repTo) => Optional.of(repTo)
        case None => Optional.empty()

      }
    def getBody: Array[Byte] = body
  }


  object NatsSubscriptionResult {

    def apply(subject:String, replyTo:Option[String], body:Array[Byte]): NatsSubscriptionResult = new NatsSubscriptionResult(subject,replyTo,body)
  }

  final class NatsPublish private (val subject:String, body:Array[Byte]){

    def getSubject: String = subject

    def getBody: Array[Byte] = body

  }

  object NatsPublish{

    /** Scala API */
    def apply(subject:String, body:Array[Byte]) = new NatsPublish(subject, body)

    /** Java API */
    def create(subject:String, body:Array[Byte]) = new NatsPublish(subject, body)
  }

  final case class NatsPublishResult[T] private (input: NatsPublish, result: Try[T]) {

    /** Java API */
    def getInput: NatsPublish = input

    /** Java API */
    def getException: Optional[Throwable] =
      result match {
        case Failure(ex) => Optional.of(ex)
        case Success(_) => Optional.empty()
      }

    /** Java API */
    def getResult: Optional[T] =
      result match {
        case Success(_) => Optional.of(result.get)
        case Failure(_) => Optional.empty()

      }
  }

  object NatsPublishResult{

    def apply[T](input:NatsPublish, exception: Try[T]): NatsPublishResult[T] = new NatsPublishResult[T](input,exception)
  }

}
