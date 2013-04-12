package leon
package numerics

import Sat._

import ceres.common.{RationalInterval, Rational}
import Rational._

import purescala.Trees._
import purescala.Definitions._

import scala.collection.immutable.HashMap

/* TODO
  - bug in the affine arithmetic somewhere (not giving the correct initial bound)
  - what if the initial bounds are not available (-inf, inf)
  - one of the bounds is often tight. we should detect/check this up front
  - it may also be good to now have to extract the variable ranges from the
    tree each time

  - handle stopping in a more flexible way
*/

object BoundsIterator {
  val maxIterationsLinear = 100
  var verbose = false
  val precision = Rational.rationalFromReal(0.0001)
  val maxIterationsBinary = 20 

  // TODO: It may be easier just to assign a solver
  private var reporter: Reporter = null
 /* private var leonContext: LeonContext = null 
  private var program: Program = null
  def setReporter(rep: Reporter) = reporter = rep
  def setContext(ctx: LeonContext) = leonContext = ctx
  def setProgram(p: Program) = program = p
*/

 // TODO: Should choose the correct strategy (i.e. maybe first do a quick check
   // whether binary search makes sense
  def tightenRange(solver: NumericSolver, tree: Expr, initialBound: RationalInterval): RationalInterval = tree match {
    // Nothing to do for constants
    case IntLiteral(v) => initialBound
    case RationalLiteral(v) => initialBound
    case FloatLiteral(v) => initialBound

    // Also nothing to do for variables
    case Variable(id) => initialBound

    case _ =>
      val a = initialBound.xlo
      val b = initialBound.xhi

      // check that initial bounds are valid
      printBoundsResult(checkBounds(solver, tree, a, b), "initial")

      if (verbose) {
        println("\nComputing range for " + tree)
        println("starting from  [" + a + ", " + b + "]")
        println("\n============Looking for lowerbound")
      }
      val newLowerBound = getLowerBound(a, b, solver, tree, 0)
    
      if (verbose) {
        println("\n============Looking for upperbound")
      }
      val newUpperBound = getUpperBound(a, b, solver, tree, 0)
   
      // Remove this check once we're fairly sure it all works:
      printBoundsResult(checkBounds(solver, tree, newLowerBound, newUpperBound), "final")
      RationalInterval(newLowerBound, newUpperBound)
  }
  
  def checkBounds(solver: NumericSolver, tree: Expr,
    lowBound: Rational, upBound: Rational): (Sat, Sat, String) = {
    val resLow = solver.checkLowerBound(tree, lowBound)
    val resUp = solver.checkUpperBound(tree, upBound)
    val diagnoseString = resLow._2 + "\n" + resUp._2
    (resLow._1, resUp._1, diagnoseString)    
  }

  
  // start with b being the upperBound
  // Invariant: the lower bound is always sound, and the upper bound not
  def getLowerBound(a: Rational, b: Rational, solver: NumericSolver,
    tree: Expr, count: Int): Rational = {
    // Enclosure of bound is precise enough
    if (b-a < precision || count > maxIterationsBinary) {
      return a
    }
    else {
      if (verbose) {
        println(a + "      -    " +b)
        println("a: " + a.toFractionString)
        println("b: " + b.toFractionString)
      }
      val mid = a + (b - a) / Rational(2l)
      if (verbose) {
        println("mid: " + mid)
        println("mid: " + mid.toFractionString)
      }
      val res = solver.checkLowerBound(tree, mid)
      
      if (verbose) {
        println("checked lwr bound: " + mid + ", with result: " + res)
      }
        
      res._1 match {
        case SAT => getLowerBound(a, mid, solver, tree, count + 1)
        case UNSAT => getLowerBound(mid, b, solver, tree, count + 1)
        case Unknown => // Return safe answer
          return a
      }
    }
  }

  //TODO: Invariant the upper bound is always sound, the lower bnd not
  def getUpperBound(a: Rational, b: Rational, solver: NumericSolver,
    tree: Expr, count: Int): Rational = {

    // Enclosure of bound is precise enough
    if (b-a < precision || count > maxIterationsBinary) {
      return b
    }
    else {
      val mid = a + (b - a) / Rational(2l)
      val res = solver.checkUpperBound(tree, mid)

      if (verbose) {
        println("checked upp bound: " + mid + ", with result: " + res)
      }

      res._1 match {
        case SAT => getUpperBound(mid, b, solver, tree, count + 1)
        case UNSAT => getUpperBound(a, mid, solver, tree, count + 1)
        case Unknown => // Return safe answer
          return b
      }
    }
  }

  // which: initial or final
  def printBoundsResult(res: (Sat, Sat, String), which: String) = res match {
    case (SAT, SAT, msg) =>
      if (reporter != null)
        reporter.error("!!! ERROR: both " + which + " bounds are not sound!" +
          "\nmsg: " + msg + "\n ------------------")
    case (SAT, _, msg) =>
      if (reporter != null)
        reporter.error("!!! ERROR: " + which + " lower bound is not sound!" +
          "\nmsg: " + msg + "\n ------------------")
    case (_, SAT, msg) =>
      if (reporter != null)
        reporter.error("!!! ERROR: " + which + " upper bound is not sound!" +
          "\nmsg: " + msg + "\n ------------------")
    case (UNSAT, UNSAT, msg) =>
      if (verbose) {
        println(which + " bounds check successful.")
      }
    case _ =>
      println("WARNING: cannot check "+which+" bounds.")
  }

  /*
  def determineRangeLinear(tree: Expr, initialBound: RationalInterval): RationalInterval = {

    // extract variables and range constraints
    val varCons = getVariableConstraints(tree)
    //println("varCons: " + varCons)

    if (!varCons.isEmpty) {
      // get initial bounds from affine arithmetic
      //val initialBound = RationalInterval(s.qInterval._1, s.qInterval._2)

      val stepSize = precision
      println("stepsize: " + stepSize)
      var i = 0
      // currentBound is assumed to be always valid
      // also assume that the initial bound given is valid, we don't check again
      var currentBound = initialBound
      var continue = true

      //var newBound = RationalInterval(currentBound.xlo + stepSize,
      //        currentBound.xhi - stepSize)
      while (continue && i < maxIterationsLinear) {
        //println("\n ----------------- ")
        val newBound = RationalInterval(currentBound.xlo + stepSize,
              currentBound.xhi - stepSize)
        val solver = new NumericSolver

        if (i % 5 == 0) println("iteration: " + i + " checking bounds: " + newBound)
        val res = solver.checkBound(varCons, tree, newBound)
        
        //println("tested bound: " + newBound + "   " + res)
        res  match {
          case (false, false) =>
            continue = true
            currentBound = newBound
            //newBound = RationalInterval(currentBound.xlo + stepSize,
            //  currentBound.xhi - stepSize)

          case (true, false) =>
            continue = true
            currentBound = RationalInterval(currentBound.xlo,
              newBound.xhi)

          case (false, true) =>
            continue = true
            currentBound = RationalInterval(newBound.xlo,
              currentBound.xhi)

          case (true, true) =>
            continue = false
        }
        
        i = i + 1
      }
      //if (i >= MaxIterations) println("Exhausted iteration limit!")
      // iterate:
      // create Z3 query, and check if satisfiable.
      return currentBound
    }
    // This can happen for constants
    return initialBound
  }
*/

}