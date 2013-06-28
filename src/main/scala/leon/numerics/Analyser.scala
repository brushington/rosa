package leon
package numerics

import ceres.common._

import purescala.Trees._
import purescala.TypeTrees._
import purescala.TreeOps._
import purescala.Definitions._
import purescala.Common._
import Utils._
import ArithmeticOps._

class Analyser(reporter: Reporter) {

  val verbose = false

  def analyzeThis(funDef: FunDef): VerificationCondition = {
    if (verbose) reporter.info("")
    if (verbose) reporter.info("-----> Analysing function " + funDef.id.name + "...")

    if (verbose) println("pre: " + funDef.precondition)
    if (verbose) println("\nbody: " + funDef.body)
    if (verbose) println("\npost: " + funDef.postcondition)


    val vc = new VerificationCondition(funDef)
    funDef.precondition match {
      case Some(p) =>
        val collector = new VariableCollector
        collector.transform(p)
        vc.inputs = collector.recordMap
        if (verbose) reporter.info("inputs: " + vc.inputs)
        vc.precondition = Some(p)
      case None =>
        vc.precondition = Some(BooleanLiteral(true))
    }

    val bodyPreprocessed = funDef.body.get //collectPowers(funDef.body.get)

    funDef.postcondition match {
      //invariant
      case Some(ResultVariable()) =>
        val postConditions = getInvariantCondition(bodyPreprocessed)
        val bodyWOLets = convertLetsToEquals(bodyPreprocessed)
        vc.body = Some(replace(postConditions.map(p => (p, BooleanLiteral(true))).toMap, bodyWOLets))
        vc.allConstraints = List(Constraint(vc.precondition.get, vc.body.get, Or(postConditions), "wholebody"))
        vc.isInvariant = true
      case Some(post) =>
        vc.body = Some(convertLetsToEquals(addResult(bodyPreprocessed)))
        val specC = Constraint(vc.precondition.get, vc.body.get, post, "wholeBody")
        vc.allConstraints = List(specC)

      // Auxiliary function, nothing to prove
      case None =>
        vc.body = Some(convertLetsToEquals(addResult(bodyPreprocessed)))
    }



    if (containsFunctionCalls(vc.body.get)) {
      val noiseRemover = new NoiseRemover
      val paths = collectPaths(vc.body.get)

      for (path <- paths) {
        var i = 0
        while (i != -1) {
          val j = path.expression.indexWhere(e => containsFunctionCalls(e), i)
          if (j != -1) {
            i = j + 1
            val pathToFncCall = path.expression.take(j)
            val fncCalls = functionCallsOf(path.expression(j))
            for (fncCall <- fncCalls) {
              fncCall.funDef.precondition match {
                case Some(p) =>
                  val args: Map[Expr, Expr] = fncCall.funDef.args.map(decl => decl.toVariable).zip(fncCall.args).toMap
                  val postcondition = replace(args, noiseRemover.transform(p))
                  vc.allConstraints = vc.allConstraints :+ Constraint(
                      And(vc.precondition.get, path.condition), And(pathToFncCall), postcondition, "pre of call " + fncCall.toString)
                case None => ;
                  // TODO: if we have no precondition given, do we want to compute it?
              }
            }
          } else { i = -1}
        }
      }

    }
    //println("All constraints generated: ")
    //println(vc.allConstraints.mkString("\n -> ") )

    vc.funcArgs = vc.funDef.args.map(v => Variable(v.id).setType(RealType))
    vc.localVars = allLetDefinitions(bodyPreprocessed).map(letDef => Variable(letDef._1).setType(RealType))

    vc
  }

  // Has to run before we removed the lets!
  // Basically the first free expression that is not an if or a let is the result
  private def addResult(expr: Expr): Expr = expr match {
    case IfExpr(cond, then, elze) => IfExpr(cond, addResult(then), addResult(elze))
    case Let(binder, value, body) => Let(binder, value, addResult(body))
    case UMinus(_) | Plus(_, _) | Minus(_, _) | Times(_, _) | Division(_, _) | Sqrt(_) | FunctionInvocation(_, _) | Variable(_) =>
      Equals(ResultVariable(), expr)
    case _ => expr
  }

  // can return several, as we may have an if-statement
  private def getInvariantCondition(expr: Expr): List[Expr] = expr match {
    case IfExpr(cond, then, elze) => getInvariantCondition(then) ++ getInvariantCondition(elze)
    case Let(binder, value, body) => getInvariantCondition(body)
    case LessEquals(_, _) | LessThan(_, _) | GreaterThan(_, _) | GreaterEquals(_, _) | MorePrecise(_, _) =>
      List(expr)
    case _ =>
      reporter.error("Expected invariant, but found: " + expr.getClass)
      List(BooleanLiteral(false))
  }

  private def convertLetsToEquals(expr: Expr): Expr = expr match {
    case IfExpr(cond, then, elze) =>
      IfExpr(cond, convertLetsToEquals(then), convertLetsToEquals(elze))

    case Let(binder, value, body) =>
      And(Equals(Variable(binder), convertLetsToEquals(value)), convertLetsToEquals(body))

    case _ => expr

  }



  // It is complete, if the result is bounded below and above and the noise is specified.
/*  private def isComplete(post: Expr): Boolean = {
    post match {
      case and @ And(args) =>
        val variableBounds = Utils.getVariableBounds(and)
        val noise = TreeOps.contains(and, (
          a => a match {
            case Noise(ResultVariable()) => true
            case _ => false
          }))
        noise && variableBounds.contains(Variable(FreshIdentifier("#res")))

      case _ =>
        reporter.warning("Unsupported type of postcondition: " + post)
        false
    }
  }
*/

}
