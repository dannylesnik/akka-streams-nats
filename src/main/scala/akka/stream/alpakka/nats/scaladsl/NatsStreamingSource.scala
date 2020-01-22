package akka.stream.alpakka.nats.scaladsl

import akka.NotUsed
import akka.stream.alpakka.nats.NatsSubscriberSettings
import akka.stream.alpakka.nats.impl.NatsStreamingSourceStage
import akka.stream.scaladsl.Source
import io.nats.streaming.{Message, StreamingConnection}

object NatsStreamingSource {

  def subscribe(natsSubscriberSettings:NatsSubscriberSettings, streamingConnection:StreamingConnection): Source[Message, NotUsed] =
    Source.fromGraph(new NatsStreamingSourceStage(streamingConnection, natsSubscriberSettings))

}
