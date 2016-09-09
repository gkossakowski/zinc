package xsbt

import xsbti.{ F0, Maybe }

/**
 * Utilities that convert Scala basic structures like thunks or Option to their
 * xsbti-defined equivalents.
 */
object ConvertersToXsbti {

  def f0[A](a: => A): F0[A] = new ConcreteF0[A](a)
  private final class ConcreteF0[A](a: => A) extends F0[A] {
    def apply: A = a
  }

  def o2m[A](o: Option[A]): Maybe[A] =
    o match {
      case Some(v) => Maybe.just(v)
      case None    => Maybe.nothing()
    }

}
