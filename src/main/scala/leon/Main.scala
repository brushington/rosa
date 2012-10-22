package leon

import scala.tools.nsc.{Global,Settings=>NSCSettings,SubComponent,CompilerCommand}

import purescala.Definitions.Program

object Main {
  import leon.{Reporter,DefaultReporter,Analysis}

  def main(args : Array[String]) : Unit = run(args)

  def runFromString(program : String, args : Array[String], reporter : Reporter = new DefaultReporter, classPath : Option[Seq[String]] = None) : Unit = {
    import java.io.{BufferedWriter,File,FileWriter,IOException}

    try {
      val file : File = File.createTempFile("leon", ".scala")
      file.deleteOnExit
      val out = new BufferedWriter(new FileWriter(file))
      out.write(program)
      out.close
      run(file.getPath.toString +: args, reporter, classPath)
    } catch {
      case e : IOException => reporter.error(e.getMessage)
    }
  }

  def run(args: Array[String], reporter: Reporter = new DefaultReporter, classPath : Option[Seq[String]] = None) : Unit = {
    val settings = new NSCSettings
    classPath.foreach(s => settings.classpath.tryToSet(s.toList))

    val (leonOptions, nonLeonOptions) = args.toList.partition(_.startsWith("--"))
    val command = new CompilerCommand(nonLeonOptions, settings) {
      override val cmdName = "leon"
    }

    if(command.ok) {
      if(settings.version.value) {
        println(command.cmdName + " beta.")
      } else {
        val runner = new PluginRunner(settings, reporter)
        runner.leonPlugin.processOptions(leonOptions.map(_.substring(2)), reporter.error(_))

        val run = new runner.Run
        run.compile(command.files)
      }
    }
  }
}

/** This class is a compiler that will be used for running the plugin in
 * standalone mode. Original version courtesy of D. Zufferey. */
class PluginRunner(settings : NSCSettings, reporter : Reporter) extends Global(settings, new plugin.SimpleReporter(settings, reporter)) {
  val leonPlugin = new plugin.LeonPlugin(this, reporter)

  protected def myAddToPhasesSet(sub : SubComponent, descr : String) : Unit = {
    phasesSet += sub
  }

  /** The phases to be run. */
  override protected def computeInternalPhases() : Unit = {
    val phs = List(
      syntaxAnalyzer          -> "parse source into ASTs, perform simple desugaring",
      analyzer.namerFactory   -> "resolve names, attach symbols to named trees",
      analyzer.packageObjects -> "load package objects",
      analyzer.typerFactory   -> "the meat and potatoes: type the trees",
      superAccessors          -> "add super accessors in traits and nested classes",
      pickler                 -> "serialize symbol tables",
      refchecks               -> "reference and override checking, translate nested objects"
    ) ::: {
      val zipped = leonPlugin.components zip leonPlugin.descriptions
      zipped
    }
    phs foreach (myAddToPhasesSet _).tupled
  }
}
