package akka.stream.alpakka.nats.javadsl

import akka.NotUsed
import akka.stream.alpakka.nats.model
import akka.stream.alpakka.nats.model.{NatsPublish, NatsPublishResult}
import akka.stream.javadsl.Flow
import io.nats.client.Connection

import scala.concurrent.ExecutionContext

object NatsFlow {

  def publish(parallelism:Int, connection:Connection,executionContext: ExecutionContext):
  Flow[model.NatsPublish, NatsPublishResult[NatsPublish], NotUsed] ={
    Flow.fromGraph(akka.stream.alpakka.nats.scaladsl.NatsFlow.publish(parallelism,connection)(executionContext))
  }


  def publishWithReply(parallelism:Int, connection: Connection, executionContext: ExecutionContext):
  Flow[NatsPublish, NatsPublishResult[Array[Byte]], NotUsed] ={
      Flow.fromGraph(akka.stream.alpakka.nats.scaladsl.NatsFlow.publishWithReply(parallelism,connection)(executionContext))
    }

}
