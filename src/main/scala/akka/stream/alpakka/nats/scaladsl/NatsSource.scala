package akka.stream.alpakka.nats.scaladsl

import akka.NotUsed
import akka.stream.alpakka.nats.NatsSubscriberSettings
import akka.stream.alpakka.nats.impl.NutsIOSourceStage
import akka.stream.alpakka.nats.model.NatsSubscriptionResult
import akka.stream.scaladsl.Source
import io.nats.client.Connection

object NatsSource{
  def subscribe(natsSubscriberSettings:NatsSubscriberSettings, connection:Connection): Source[NatsSubscriptionResult, NotUsed] ={
    Source.fromGraph(new NutsIOSourceStage(connection, natsSubscriberSettings))
  }
}
