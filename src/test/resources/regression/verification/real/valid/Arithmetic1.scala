import leon.Real
import Real._

// Tests basic capabilities, like adding roundoffs
object Arithmetic1 {

  def f(x: Real): Real = {
    require(x >< (1.0, 2.0))
    x * x
  } ensuring (res => res <= 4)
}