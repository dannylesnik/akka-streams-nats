package docs.scaladsl

import java.nio.charset.StandardCharsets

import akka.Done
import akka.stream.alpakka.nats.NatsSubscriberSettings
import akka.stream.alpakka.nats.model.NatsPublish
import akka.stream.alpakka.nats.scaladsl.{NatsFlow, NatsSource}
import akka.stream.scaladsl.{Sink, Source}
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import scala.util.Random


class NatsSourceSpec extends AnyWordSpec
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with Matchers
  with ScalaFutures
  with NatsSupport {

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(timeout = 2 seconds)
  implicit val timeout: Timeout = Timeout(patienceConfig.timeout)

  override def subjectName: String = "NatsSourceSpec"
  val clientId:String = new Random().alphanumeric.take(15).mkString("")
  val natsSubscriberSettings: NatsSubscriberSettings = NatsSubscriberSettings(system.settings.config,subjectName)

  "Nats Source" should{

    "Receive published message" in{

      val result: Future[String] = NatsSource.subscribe(natsSubscriberSettings, connection).map(message => new String(message.getBody,StandardCharsets.UTF_8))
        .runWith(Sink.head)


      val publishResult: Future[Done] = Source.single(NatsPublish(subjectName,expectedMessage.getBytes(StandardCharsets.UTF_8))).via(NatsFlow.publish(1,connection)).runWith(Sink.ignore)

      publishResult.futureValue(timeout)



      whenReady(result){message =>
        message shouldBe expectedMessage
      }
    }

  }
}
