package akka.stream.alpakka.nats.scaladsl

import akka.NotUsed
import akka.stream.alpakka.nats.model.{NatsPublish, NatsPublishResult}
import akka.stream.scaladsl.Flow
import io.nats.streaming.StreamingConnection

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

object NatsStreamingFlow {

  def publish(parallelism:Int, streamingConnection:StreamingConnection)(implicit executionContext: ExecutionContext):
  Flow[NatsPublish, NatsPublishResult[NatsPublish], NotUsed] =
    Flow[NatsPublish].mapAsync(parallelism){
      publishObject=>
        Future{streamingConnection.publish(publishObject.subject,publishObject.getBody)}
          .map(_ => NatsPublishResult(publishObject,Success(publishObject)))
          .recover{case ex:Throwable =>  NatsPublishResult(publishObject,Failure(ex))}
  }


  def publishWithAck(parallelism:Int, streamingConnection:StreamingConnection)(implicit executionContext: ExecutionContext):
  Flow[NatsPublish, NatsPublishResult[String], NotUsed] ={
    Flow[NatsPublish].mapAsync(parallelism){
      publishObject=>{
        val promise:Promise[String] = Promise[String]
        streamingConnection.publish(publishObject.getSubject,publishObject.getBody, (nuid: String, ex: Exception) => {
          if (ex != null) {
            promise.failure(ex)
          } else {
            promise.success(nuid)
          }
        })
          promise.future.map(nuid => NatsPublishResult(publishObject,Success(nuid)))
            .recover({case ex:Throwable =>NatsPublishResult(publishObject,Failure(ex))} )
      }
    }
  }


}
