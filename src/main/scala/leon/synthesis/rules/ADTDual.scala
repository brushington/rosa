package leon
package synthesis
package rules

import purescala.Trees._
import purescala.TreeOps._
import purescala.Extractors._

class ADTDual(synth: Synthesizer) extends Rule("ADTDual", synth, 200) {
  def applyOn(task: Task): RuleResult = {
    val p = task.problem

    val xs = p.xs.toSet
    val as = p.as.toSet

    val TopLevelAnds(exprs) = p.phi


    val (toRemove, toAdd) = exprs.collect {
      case eq @ Equals(cc @ CaseClass(cd, args), e) if (variablesOf(e) -- as).isEmpty && (variablesOf(cc) -- xs).isEmpty =>
        (eq, CaseClassInstanceOf(cd, e) +: (cd.fieldsIds zip args).map{ case (id, ex) => Equals(ex, CaseClassSelector(cd, e, id)) } )
      case eq @ Equals(e, cc @ CaseClass(cd, args)) if (variablesOf(e) -- as).isEmpty && (variablesOf(cc) -- xs).isEmpty =>
        (eq, CaseClassInstanceOf(cd, e) +: (cd.fieldsIds zip args).map{ case (id, ex) => Equals(ex, CaseClassSelector(cd, e, id)) } )
    }.unzip

    if (!toRemove.isEmpty) {
      val sub = p.copy(phi = And((exprs.toSet -- toRemove ++ toAdd.flatten).toSeq))

      RuleStep(List(sub), forward)
    } else {
      RuleInapplicable
    }
  }
}
