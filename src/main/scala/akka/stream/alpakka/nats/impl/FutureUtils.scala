package akka.stream.alpakka.nats.impl

import java.util.concurrent.CompletableFuture

import scala.concurrent.{Future, Promise}

object FutureUtils {

    implicit def completableFutureToFuture[T](cf: CompletableFuture[T]): Future[T] = {
      val p = Promise[T]()

      cf.whenComplete { (result, error) =>
        if (error ne null) p.failure(error)
        else p.success(result)
      }
      p.future
    }
  }
