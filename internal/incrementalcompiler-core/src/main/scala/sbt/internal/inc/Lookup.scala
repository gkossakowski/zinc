package sbt.internal.inc

import java.io.File

import xsbti.compile.CompileAnalysis

trait Lookup {
  def lookupOnClasspath(binaryClassName: String): Option[File]
  def lookupAnalysis(classFile: File): Option[CompileAnalysis]
  def lookupAnalysis(binaryDependency: File, binaryClassName: String): Option[CompileAnalysis]
  def lookupAnalysis(binaryClassName: String): Option[CompileAnalysis]
}
