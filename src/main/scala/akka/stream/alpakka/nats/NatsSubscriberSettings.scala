package akka.stream.alpakka.nats

import java.time.{Duration, Instant}
import com.typesafe.config.Config
import io.nats.streaming.SubscriptionOptions


final class NatsSubscriberSettings private(val maxConcurrency: Int,
                                           val bufferSize: Int,
                                           val subject:String,
                                           val startAtTime:Instant = Instant.MIN,
                                           val waitAck:Duration= SubscriptionOptions.DEFAULT_ACK_WAIT,
                                           val deliverAllAvailable:Boolean = false,
                                           val manualAcks:Boolean = false,
                                           val maxInFlight:Int = SubscriptionOptions.DEFAULT_MAX_IN_FLIGHT,
                                           val startAtSequence:Long = Long.MinValue,
                                           val startAtTimeDelta:Option[Duration] = None,
                                           val startWithLastReceived:Boolean =false,
                                           val durableName:Option[String] = None,
                                           val subscriptionTimeout:Duration = SubscriptionOptions.DEFAULT_SUBSCRIPTION_TIMEOUT) {


  def withMaxConcurrency(value: Int):NatsSubscriberSettings = copy(maxConcurrency=value)


  def withBufferSize(value: Int):NatsSubscriberSettings = copy(bufferSize=value)


  def withSubject(value: String):NatsSubscriberSettings = copy(subject=value)

  def withDurableName(value: String):NatsSubscriberSettings = copy(durableName=Some(value))


  def withWaitAck(value:Duration):NatsSubscriberSettings = copy(waitAck=value)


  def withStartAtTime(value:Instant):NatsSubscriberSettings = copy(startAtTime=value)


  def withDeliverAllAvailable():NatsSubscriberSettings = copy(deliverAllAvailable=true)


  def withManualAck(value:Boolean):NatsSubscriberSettings = copy(manualAcks=value)


  def withMaxInFlight(value:Int):NatsSubscriberSettings = copy(maxInFlight=value)


  def withStartAtSequence(value:Long):NatsSubscriberSettings = copy(startAtSequence=value)


  def withStartAtTimeDelta(value:Duration):NatsSubscriberSettings = copy(startAtTimeDelta=Some(value))


  def withStartWithLastReceived(value:Boolean):NatsSubscriberSettings = copy(startWithLastReceived=value)


  def withSubscriptionTimeout(value:Duration):NatsSubscriberSettings = copy(subscriptionTimeout=value)


  private def copy(maxConcurrency: Int=maxConcurrency,bufferSize:Int=bufferSize,startAtTime:Instant = startAtTime,
                   subject:String=subject, waitAck:Duration = waitAck,deliverAllAvailable:Boolean=deliverAllAvailable,
                   manualAcks:Boolean = manualAcks, maxInFlight:Int = maxInFlight, startAtSequence:Long = startAtSequence,
                   startAtTimeDelta:Option[Duration] = startAtTimeDelta, startWithLastReceived:Boolean = startWithLastReceived,
                   subscriptionTimeout:Duration=subscriptionTimeout,durableName:Option[String] = durableName):NatsSubscriberSettings=
    new NatsSubscriberSettings(maxConcurrency= maxConcurrency,bufferSize=bufferSize,subject=subject,startAtTime = startAtTime,
      waitAck = waitAck,deliverAllAvailable=deliverAllAvailable, manualAcks = manualAcks,maxInFlight = maxInFlight,
      startAtSequence = startAtSequence, startAtTimeDelta = startAtTimeDelta, startWithLastReceived = startWithLastReceived,
      subscriptionTimeout = subscriptionTimeout, durableName = durableName)

  def createSubscriptionOptions: SubscriptionOptions ={

    val subscriptionOptions: SubscriptionOptions.Builder = new SubscriptionOptions.Builder()
    subscriptionOptions.ackWait(waitAck)

    if (deliverAllAvailable) {
      subscriptionOptions.deliverAllAvailable()
    }

    if (manualAcks) {
      subscriptionOptions.manualAcks()
    }

    subscriptionOptions.maxInFlight(maxInFlight)
    if (startAtSequence > Long.MinValue) {
      subscriptionOptions.startAtSequence(startAtSequence)
    }

    startAtTimeDelta.map(f => subscriptionOptions.startAtTimeDelta(f))

    if (startAtTime != Instant.MIN){
      subscriptionOptions.startAtTime(startAtTime)
    }

    if (startWithLastReceived){
      subscriptionOptions.startWithLastReceived()
    }

    subscriptionOptions.subscriptionTimeout(subscriptionTimeout)

    durableName.map(name => subscriptionOptions.durableName(name))

    subscriptionOptions.build()

  }

}

object NatsSubscriberSettings{

  val configPath = "alpakka.nats.subscriber"

  /** Scala API */
  def apply(c:Config, subject:String):NatsSubscriberSettings = createConfig(c,subject)

  /** Java API */
  def create(c:Config, subject: String):NatsSubscriberSettings = createConfig(c,subject)

  private def createConfig(c:Config, subject:String):NatsSubscriberSettings = {
    val config = c.getConfig(configPath)

    val maxConcurrency = config.getInt("max-conurrency")

    val bufferSize = config.getInt("buffer-size")

    new NatsSubscriberSettings(subject = subject,maxConcurrency = maxConcurrency,bufferSize = bufferSize)
  }


}