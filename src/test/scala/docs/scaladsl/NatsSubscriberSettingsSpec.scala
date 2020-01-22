package docs.scaladsl

import java.time.{Duration, Instant}

import akka.stream.alpakka.nats.NatsSubscriberSettings
import io.nats.streaming.SubscriptionOptions
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import scala.util.Random

class NatsSubscriberSettingsSpec  extends AnyWordSpec
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with Matchers
  with ScalaFutures
  with NatsSupport{

  val clientId:String = new Random().alphanumeric.take(15).mkString("")


  "Nats Subscriber Settings class" should{

    "return correct 'Subject' parameter" in{
      val settings = NatsSubscriberSettings(system.settings.config,"test-subject")
      settings.subject shouldEqual "test-subject"
    }

    "return 'Max Concurrency' parameter from config" in{
      val settings = NatsSubscriberSettings(system.settings.config,"test-subject")
      settings.maxConcurrency shouldEqual 3
    }

    "return 'Buffer Size' parameter from config" in{
      val settings = NatsSubscriberSettings(system.settings.config,"test-subject")
      settings.bufferSize shouldEqual 4
    }

    "return default false value for manual acks" in{
      val settings = NatsSubscriberSettings(system.settings.config,"test-subject")
      settings.manualAcks shouldEqual false

    }

    "return  true value for 'Manual Acks' parameters" in{
      val settings = NatsSubscriberSettings(system.settings.config,"test-subject").withManualAck(true)
      settings.manualAcks shouldEqual true
    }

    "return default value for 'Deliver all Available' parameter" in{
      val settings = NatsSubscriberSettings(system.settings.config,"test-subject")
      settings.deliverAllAvailable shouldEqual false
    }

    "return assigned value for 'Deliver all Available' parameter" in{
      val settings = NatsSubscriberSettings(system.settings.config,"test-subject").withDeliverAllAvailable()
      settings.deliverAllAvailable shouldEqual true
    }


    "return default value for 'Max in Flight' parameter" in{
      val settings = NatsSubscriberSettings(system.settings.config,"test-subject")
      settings.maxInFlight shouldEqual SubscriptionOptions.DEFAULT_MAX_IN_FLIGHT
    }

    "return assigned value for 'Max in Flight' parameter" in{
      val settings = NatsSubscriberSettings(system.settings.config,"test-subject").withMaxInFlight(10)
      settings.maxInFlight shouldEqual 10
    }

    "return default value for 'Start at Sequence' parameter" in{
      val settings = NatsSubscriberSettings(system.settings.config,"test-subject")
      settings.startAtSequence shouldEqual Long.MinValue
    }

    "return assigned value for 'Start at Sequence' parameter" in{
      val settings = NatsSubscriberSettings(system.settings.config,"test-subject").withStartAtSequence(1L)
      settings.startAtSequence shouldEqual 1L
    }

    "return default value for 'Start with Last Received' parameter" in{
      val settings = NatsSubscriberSettings(system.settings.config,"test-subject")
      settings.startWithLastReceived shouldEqual false
    }

    "return assigned value for 'Start with Last Received' parameter" in{
      val settings = NatsSubscriberSettings(system.settings.config,"test-subject").withStartWithLastReceived(true)
      settings.startWithLastReceived shouldEqual true
    }

    "return default value for 'Start at time Delta' parameter" in{
      val settings = NatsSubscriberSettings(system.settings.config,"test-subject")
      settings.startAtTimeDelta shouldEqual None
    }

    "return assigned value for 'Start at Time Delta' parameter" in{
      val settings = NatsSubscriberSettings(system.settings.config,"test-subject").withStartAtTimeDelta(Duration.ZERO)
      settings.startAtTimeDelta shouldEqual Some(Duration.ZERO)
    }

    "return default value for 'Subscription Timeout' parameter" in{
      val settings = NatsSubscriberSettings(system.settings.config,"test-subject")
      settings.subscriptionTimeout shouldEqual SubscriptionOptions.DEFAULT_SUBSCRIPTION_TIMEOUT
    }

    "return assigned value for 'Subscription Timeout' parameter" in {
      val settings = NatsSubscriberSettings(system.settings.config, "test-subject").withSubscriptionTimeout(Duration.ZERO)
      settings.subscriptionTimeout shouldEqual Duration.ZERO
    }

    "return default value for 'Start At Time' parameter" in{
      val settings = NatsSubscriberSettings(system.settings.config, "test-subject")
      settings.startAtTime shouldEqual Instant.MIN
    }

    "return assigned value for 'Start At Time' parameter" in{
      val settings = NatsSubscriberSettings(system.settings.config, "test-subject").withStartAtTime(Instant.MAX)
      settings.startAtTime shouldEqual Instant.MAX
    }

    "return default value for 'Durable Name' parameter" in{
      val settings = NatsSubscriberSettings(system.settings.config, "test-subject")
      settings.durableName shouldEqual None
    }

    "return assigned value for 'Durable Name' parameter" in{
      val settings = NatsSubscriberSettings(system.settings.config, "test-subject").withDurableName("some_name")
      settings.durableName shouldEqual Some("some_name")
    }

    "return default 'SubscriptionOptions' instance" in{
      val expected = new SubscriptionOptions.Builder().build()
      val natsSubscriberSettings = NatsSubscriberSettings(system.settings.config, "test-subject")
      val subscriptionOptions: SubscriptionOptions = natsSubscriberSettings.createSubscriptionOptions
      subscriptionOptions shouldEqual expected
    }


    "return updated 'Durable Name' in subscriptionOptions instance" in{
      val expected = new SubscriptionOptions.Builder().durableName("some_name").build()
      val natsSubscriberSettings = NatsSubscriberSettings(system.settings.config, "test-subject").withDurableName("some_name")
      val subscriptionOptions: SubscriptionOptions = natsSubscriberSettings.createSubscriptionOptions
      subscriptionOptions shouldEqual expected
    }


    "return updated 'Subscription Duration' in subscriptionOptions instance" in{
      val expected = new SubscriptionOptions.Builder().subscriptionTimeout(Duration.ZERO).build()
      val natsSubscriberSettings = NatsSubscriberSettings(system.settings.config, "test-subject").withSubscriptionTimeout(Duration.ZERO)
      val subscriptionOptions: SubscriptionOptions = natsSubscriberSettings.createSubscriptionOptions
      subscriptionOptions shouldEqual expected
    }

    "return updated 'Manual Ack' in subscriptionOptions instance" in{
      val expected = new SubscriptionOptions.Builder().manualAcks().build()
      val natsSubscriberSettings = NatsSubscriberSettings(system.settings.config, "test-subject").withManualAck(true)
      val subscriptionOptions: SubscriptionOptions = natsSubscriberSettings.createSubscriptionOptions
      subscriptionOptions shouldEqual expected
    }

    "return updated 'Wait Ack' in subscriptionOptions instance" in{
      val expected = new SubscriptionOptions.Builder().ackWait(Duration.ZERO).build()
      val natsSubscriberSettings = NatsSubscriberSettings(system.settings.config, "test-subject").withWaitAck(Duration.ZERO)
      val subscriptionOptions: SubscriptionOptions = natsSubscriberSettings.createSubscriptionOptions
      subscriptionOptions shouldEqual expected
    }


  }

  override def subjectName: String = Random.alphanumeric.take(10).mkString("")
}
