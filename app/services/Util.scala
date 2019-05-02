package services

import javax.inject.{Inject, Singleton}

import scala.collection.generic.CanBuildFrom
import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

@Singleton
class Util @Inject() (implicit executionContext: ExecutionContext) {

  def serializeFutures[A, B, C[A] <: Iterable[A]](collection: C[A])(fn: A => Future[B])(implicit cbf: CanBuildFrom[C[B], B, C[B]]): Future[C[B]] = {
    val builder = cbf()
    builder.sizeHint(collection.size)
    collection.foldLeft(Future(builder)) { (previousFuture, next) =>
      for {
        previousResults <- previousFuture
        next <- fn(next)
      } yield previousResults += next
    } map { builder => builder.result }
  }

}
