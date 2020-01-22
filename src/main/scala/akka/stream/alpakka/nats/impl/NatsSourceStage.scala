package akka.stream.alpakka.nats.impl

import java.util

import akka.stream._
import akka.stream.alpakka.nats.NatsSubscriberSettings
import akka.stream.alpakka.nats.model.NatsSubscriptionResult
import akka.stream.stage.{AsyncCallback, GraphStage, GraphStageLogic, OutHandler}
import io.nats.client.{Connection, Message, MessageHandler}

class NutsIOSourceStage(connection:Connection, natsSubscriberSettings: NatsSubscriberSettings) extends GraphStage[SourceShape[NatsSubscriptionResult]] {


  val out: Outlet[NatsSubscriptionResult] = Outlet("NutsIOSourceStage")

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

    private val maxConcurrency = natsSubscriberSettings.maxConcurrency

    private var currentRequests = 0

    private val maxBufferSize = natsSubscriberSettings.bufferSize

    private val successCallback: AsyncCallback[NatsSubscriptionResult] = getAsyncCallback[NatsSubscriptionResult](handleSuccess)

    private val failureCallback: AsyncCallback[Exception] = getAsyncCallback[Exception](handleFailure)

    private val buffer: util.ArrayDeque[NatsSubscriptionResult] = new java.util.ArrayDeque[NatsSubscriptionResult]()

    setHandler(
      out,
      new OutHandler {

        override def onDownstreamFinish(): Unit = {
          super.onDownstreamFinish()
        }
        override def onPull(): Unit =
          if (!buffer.isEmpty) {
            push(out, buffer.poll())
          }
      }
    )

    override def preStart(): Unit = {

      val dispatcher = connection.createDispatcher(new NatsMsgHandler(successCallback,checkConcurrency,failureCallback))
      dispatcher.subscribe(natsSubscriberSettings.subject)
    }

    def checkConcurrency(): Boolean = {
      currentRequests = currentRequests + 1
      maxBufferSize > buffer.size &&
        maxConcurrency > currentRequests
    }

    def handleFailure(ex: Exception): Unit = {
      failStage(ex)
    }

    def handleSuccess(result:NatsSubscriptionResult): Unit = {

      currentRequests = currentRequests - 1

      (buffer.isEmpty, isAvailable(out)) match {
        case (false, true) =>
          push(out, buffer.poll())
          buffer.offer(result)
        case (true, true) =>
          push(out, result)
        case (_, false) =>
          buffer.offer(result)
      }
    }
  }

  override def shape: SourceShape[NatsSubscriptionResult] = SourceShape.of(out)

  override protected def initialAttributes: Attributes =
    super.initialAttributes and ActorAttributes.IODispatcher
}


private final class NatsMsgHandler(successListener: AsyncCallback[NatsSubscriptionResult],
                                   checkConcurrency: () => Boolean,
                                   failureCallback: AsyncCallback[Exception]) extends MessageHandler{
  override def onMessage(msg: Message): Unit = {
   // println(new String(msg.getData, StandardCharsets.UTF_8))
    if (checkConcurrency.apply()) {
      successListener.invoke(NatsSubscriptionResult(msg.getSubject,Option(msg.getReplyTo),msg.getData))
    } else {
      failureCallback.invoke(BufferOverflowException("Nats.io Source buffer overflown"))
    }
  }


}

