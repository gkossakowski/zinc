/* sbt -- Simple Build Tool
 * Copyright 2008, 2009 Mark Harrah
 */
package xsbt

import scala.tools.nsc.symtab.Flags
import scala.tools.nsc.io.AbstractFile

import java.io.File

/**
 * Contains utility methods for looking up class files corresponding to Symbols.
 */
abstract class LocateClassFile extends ClassName {
  val global: CallbackGlobal
  import global._

  private[this] final val classSeparator = '.'

  // carries a (cached) binary class name along with a class symbol
  protected case class ClassSymbolWithBinaryName(symbol: Symbol) {
    assert(symbol.isClass, symbol)
    lazy val binaryClassName: String = flatname(symbol, classSeparator) + symbol.moduleSuffix
  }

  protected def classFile(wrapper: ClassSymbolWithBinaryName): Option[AbstractFile] = {
    val sym = wrapper.symbol
    // package can never have a corresponding class file; this test does not
    // catch package objects (that do not have this flag set)
    if (sym hasFlag scala.tools.nsc.symtab.Flags.PACKAGE) None
    else {
      findClass(wrapper.binaryClassName) orElse {
        if (isTopLevelModule(sym)) {
          val linked = sym.companionClass
          if (linked == NoSymbol)
            None
          else
            classFile(ClassSymbolWithBinaryName(linked))
        } else
          None
      }
    }
  }

  protected def fileForClass(outputDirectory: File, s: Symbol, separatorRequired: Boolean): File =
    new File(outputDirectory, flatclassName(s, File.separatorChar, separatorRequired) + ".class")
}
