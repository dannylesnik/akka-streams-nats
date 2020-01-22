package docs.scaladsl

import java.nio.charset.StandardCharsets

import akka.Done
import akka.stream.alpakka.nats.{NatsSubscriberSettings, model}
import akka.stream.alpakka.nats.model.NatsPublish
import akka.stream.alpakka.nats.scaladsl.{NatsFlow, NatsSource}
import akka.stream.scaladsl.{Sink, Source}
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

class NatsFlowSpec extends AnyWordSpec
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with Matchers
  with ScalaFutures
  with NatsSupport {


  override def beforeAll(): Unit = super.beforeAll()

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(timeout = 2 seconds)
  implicit val timeout: Timeout = Timeout(patienceConfig.timeout)

  override def subjectName: String = "NatsFlowSpec"
  val natsSubscriberSettings: NatsSubscriberSettings = NatsSubscriberSettings(system.settings.config,subjectName)

  val clientId:String = new Random().alphanumeric.take(15).mkString("")


  "Nats Flow" should{

    "publish new message" in{

      val result: Future[model.NatsSubscriptionResult] = NatsSource.subscribe(natsSubscriberSettings,connection).runWith(Sink.head)

      Thread.sleep(1000)
      val publishResult: Future[Done] = Source.single(NatsPublish(subjectName,message.getBytes(StandardCharsets.UTF_8))).via(NatsFlow.publish(1,connection)).runWith(Sink.ignore)

      publishResult.futureValue(timeout)

      whenReady(result.map(message => new String(message.getBody,StandardCharsets.UTF_8))) { response =>
        response shouldEqual message
      }
    }

    "publish new message and wait for reply" in{

      val messageToPublish = NatsPublish(subjectName,message.getBytes(StandardCharsets.UTF_8))

      val subscriptionResult: Future[Unit] = NatsSource.subscribe(natsSubscriberSettings,connection).map(natsSubscriptionResult => connection.publish(natsSubscriptionResult
        .getReplyTo.get,responseMessage.getBytes(StandardCharsets.UTF_8))).runWith(Sink.head)

      val publishWithReplyResult: Future[model.NatsPublishResult[Array[Byte]]] = Source.single(messageToPublish).via(NatsFlow.publishWithReply(1,connection))
        .runWith(Sink.head)

      subscriptionResult.futureValue(timeout)

      whenReady(publishWithReplyResult.map(message => new String(message.getResult.get(),StandardCharsets.UTF_8))) { response =>
        response shouldEqual responseMessage
      }
    }

  }
  override protected def afterAll(): Unit = {
    //connection.close()
  }
}
