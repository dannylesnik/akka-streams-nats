package docs.scaladsl

import java.nio.charset.StandardCharsets

import akka.stream.alpakka.nats.NatsSubscriberSettings
import akka.stream.alpakka.nats.model.NatsPublish
import akka.stream.alpakka.nats.scaladsl.{NatsStreamingFlow, NatsStreamingSource}
import akka.stream.scaladsl.{Sink, Source}
import io.nats.streaming.{Message, Options, StreamingConnection, StreamingConnectionFactory}
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random

class NatsStreamingSourceSpec extends AnyWordSpec
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with Matchers
  with ScalaFutures
  with NatsSupport {


  override implicit val patienceConfig: PatienceConfig = PatienceConfig(timeout = 2.seconds)
  implicit val timeout: Timeout = Timeout(patienceConfig.timeout)

  override def subjectName: String = "NatsStreamingSourceSpec"

  val clientId:String = new Random().alphanumeric.take(15).mkString("")

  val options: Options = new Options.Builder().clusterId("test-cluster").clientId(clientId).natsConn(connection).build()

  val cf = new StreamingConnectionFactory(options)

  val streamingConnection: StreamingConnection = cf.createConnection

  val natsSubscriberSettings: NatsSubscriberSettings = NatsSubscriberSettings(system.settings.config,subjectName)


  "Nats Streaming Source" should {

    "subscribe to Nats Streaming Events" in {
      val result: Future[Message] = NatsStreamingSource.subscribe(natsSubscriberSettings, streamingConnection).runWith(Sink.head)

      Thread.sleep(1000)
      Source.single(NatsPublish(subjectName, message.getBytes(StandardCharsets.UTF_8)))
        .via(NatsStreamingFlow.publish(1, streamingConnection)).runWith(Sink.ignore)

      whenReady(result.map(f => new String(f.getData, StandardCharsets.UTF_8))) { message =>
        message shouldBe expectedMessage
      }

    }
  }

}
