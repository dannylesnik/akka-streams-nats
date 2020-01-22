package akka.stream.alpakka.nats.javadsl

import akka.NotUsed
import akka.stream.alpakka.nats.model.{NatsPublish, NatsPublishResult}
import akka.stream.javadsl.Flow
import io.nats.streaming.StreamingConnection
import scala.concurrent.ExecutionContext

object NatsStreamingFlow {

  def publish(parallelism:Int, streamingConnection:StreamingConnection,executionContext: ExecutionContext):
  Flow[NatsPublish, NatsPublishResult[NatsPublish], NotUsed] =
    Flow.fromGraph(akka.stream.alpakka.nats.scaladsl.NatsStreamingFlow.publish(parallelism,streamingConnection)(executionContext))


  def publishWithAck(parallelism:Int, streamingConnection:StreamingConnection, executionContext: ExecutionContext):
  Flow[NatsPublish, NatsPublishResult[String], NotUsed] =
    Flow.fromGraph(akka.stream.alpakka.nats.scaladsl.NatsStreamingFlow.publishWithAck(parallelism,streamingConnection)(executionContext))

}
