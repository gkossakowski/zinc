package sbt.inc

import java.io.File

import sbt.Relation

/**
 * Maps class-based dependencies to source dependencies using `declaredClasses` relation.
 *
 * The mapping is performed using two relations that track declared classes before
 * and after recompilation of sources. This way, we can properly map dependencies
 * on classes that have been moved between source files. In such case, a single
 * class can be mapped to two different source files.
 */
class ClassToSourceMapper(previousRelations: Relations, recompiledRelations: Relations) {

  def toSrcFile(className: String): Set[File] = {
    val srcs = previousRelations.declaredClasses.reverse(className) ++
      recompiledRelations.declaredClasses.reverse(className)
    if (srcs.isEmpty == 0)
      sys.error(s"No entry for class $className in declaredClasses relation.")
    else
      srcs
  }

  /**
   * Maps both forward and backward parts of passed relation using toSrcFile method.
   *
   * This method should be used to map internal (within single project) class
   * dependencies to source dependencies.
   */
  def convertToSrcDependency(classDependency: Relation[String, String]): Relation[File, File] = {
    def convertRelationMap(m: Map[String, Set[String]]): Map[File, Set[File]] = {
      val pairs = m.toSeq.flatMap {
        case (key, values) =>
          val keySrcs = toSrcFile(key)
          val valueSrcs = values.flatMap(toSrcFile)
          keySrcs.toSeq.flatMap(keySrc => valueSrcs.toSeq.map(keySrc -> _))
      }
      aggregateValues(pairs)
    }
    val forwardMap = convertRelationMap(classDependency.forwardMap)
    val reverseMap = convertRelationMap(classDependency.reverseMap)
    Relation.make(forwardMap, reverseMap)
  }

  /**
   * Converts class dependency into source-class dependency using toSrcFile method.
   *
   * This method should be used to convert internal class->external class dependencies into
   * internal source->external class dependencies.
   */
  def convertToExternalSrcDependency(classDependency: Relation[String, String]): Relation[File, String] = {
    def convertMapKeys(m: Map[String, Set[String]]): Map[File, Set[String]] = {
      val pairs = m.toSeq.flatMap {
        case (key, values) =>
          val keySrcs = toSrcFile(key)
          keySrcs.toSeq.flatMap(keySrc => values.toSeq.map(keySrc -> _))
      }
      aggregateValues(pairs)
    }
    def convertMapValues(m: Map[String, Set[String]]): Map[String, Set[File]] =
      m.mapValues(_.flatMap(toSrcFile))
    val forwardMap = convertMapKeys(classDependency.forwardMap)
    val reverseMap = convertMapValues(classDependency.reverseMap)
    Relation.make(forwardMap, reverseMap)
  }

  private def aggregateValues[T, U](s: Seq[(T, U)]): Map[T, Set[U]] = {
    s.foldLeft(Map.empty[T, Set[U]].withDefaultValue(Set.empty)) {
      case (acc, (k, v)) => acc.updated(k, acc(k) + v)
    }
  }

}