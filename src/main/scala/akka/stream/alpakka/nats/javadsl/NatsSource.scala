package akka.stream.alpakka.nats.javadsl

import akka.NotUsed
import akka.stream.alpakka.nats.NatsSubscriberSettings
import akka.stream.alpakka.nats.model.NatsSubscriptionResult
import akka.stream.javadsl.Source
import io.nats.client.Connection

object NatsSource {

  def subscribe(natsSubscriberSettings:NatsSubscriberSettings, connection:Connection): Source[NatsSubscriptionResult, NotUsed] = {
    Source.fromGraph(akka.stream.alpakka.nats.scaladsl.NatsSource.subscribe(natsSubscriberSettings,connection))
  }
}
