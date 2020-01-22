package akka.stream.alpakka.nats.scaladsl

import java.util.concurrent.CompletableFuture
import akka.NotUsed
import akka.stream.alpakka.nats.model.{NatsPublish, NatsPublishResult}
import akka.stream.scaladsl.Flow
import io.nats.client.{Connection, Message}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import akka.stream.alpakka.nats.impl.FutureUtils._

object NatsFlow {

  def publish(parallelism:Int, connection:Connection)(implicit executionContext: ExecutionContext): Flow[NatsPublish, NatsPublishResult[NatsPublish], NotUsed] ={

    Flow[NatsPublish].mapAsync(parallelism){
      publishObject => {
        Future{connection.publish(publishObject.subject,publishObject.getBody)}.map(_ => NatsPublishResult(publishObject,Success(publishObject)))
          .recover{case ex:Throwable =>  NatsPublishResult(publishObject,Failure(ex))}
      }
    }
  }

  def publishWithReply(parallelism:Int, connection: Connection)(implicit executionContext: ExecutionContext): Flow[NatsPublish, NatsPublishResult[Array[Byte]], NotUsed] = {
    Flow[NatsPublish].mapAsync(parallelism) {
      publishObject => {
        val requestFuture: CompletableFuture[Message] = connection.request(publishObject.subject, publishObject.getBody)
        val scalaFuture: Future[Message] = requestFuture
        scalaFuture.map(message => NatsPublishResult(publishObject,Success(message.getData)))
          .recover{case ex:Throwable =>  NatsPublishResult(publishObject,Failure(ex))}

      }
    }
  }

}
