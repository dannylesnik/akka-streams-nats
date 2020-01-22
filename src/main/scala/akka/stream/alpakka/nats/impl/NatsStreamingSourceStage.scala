package akka.stream.alpakka.nats.impl

import java.util

import akka.stream.alpakka.nats.NatsSubscriberSettings
import akka.stream.{Attributes, BufferOverflowException, Outlet, SourceShape}
import akka.stream.stage.{AsyncCallback, GraphStage, GraphStageLogic, OutHandler}
import io.nats.streaming.{Message, MessageHandler, StreamingConnection, Subscription, SubscriptionOptions}

class NatsStreamingSourceStage(streamingConnection:StreamingConnection,natsSubscriberSettings: NatsSubscriberSettings) extends GraphStage[SourceShape[Message]] {

  val out: Outlet[Message] = Outlet("NutsIOSourceStage")

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {

      private val maxConcurrency: Int = natsSubscriberSettings.maxConcurrency

      private var currentRequests: Int = 0

      private val maxBufferSize: Int = natsSubscriberSettings.bufferSize

      private val successCallback: AsyncCallback[Message] = getAsyncCallback[Message](handleSuccess)

      private val failureCallback: AsyncCallback[Exception] = getAsyncCallback[Exception](handleFailure)

      private val buffer: util.ArrayDeque[Message] = new java.util.ArrayDeque[Message]()

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
        val msgHandler = new NatsStreamingMessageHandler(successCallback,checkConcurrency, failureCallback)
        streamingConnection.subscribe(natsSubscriberSettings.subject,msgHandler,new SubscriptionOptions.Builder().build())
      }

      def checkConcurrency(): Boolean = {
        currentRequests = currentRequests + 1
        maxBufferSize > buffer.size &&
          maxConcurrency > currentRequests
      }

      def handleFailure(ex: Exception): Unit = {
        ex.printStackTrace()
        failStage(ex)
      }

      def handleSuccess(msg: Message): Unit = {
        currentRequests = currentRequests - 1

        (buffer.isEmpty, isAvailable(out)) match {
          case (false, true) =>
            push(out, buffer.poll())
            buffer.offer(msg)
          case (true, true) =>
            push(out, msg)
          case (_, false) =>
            buffer.offer(msg)
        }
      }
    }


  override def shape: SourceShape[Message] = SourceShape.of(out)
}


private final class NatsStreamingMessageHandler(successListener: AsyncCallback[Message],
                                                checkConcurrency: () => Boolean,
                                                failureCallback: AsyncCallback[Exception]) extends MessageHandler{
  override def onMessage(msg: Message): Unit = {
    if (checkConcurrency.apply()) {
      successListener.invoke(msg)
    } else {
      failureCallback.invoke(BufferOverflowException("Nats Streaming Source buffer overflown"))
    }
  }
}