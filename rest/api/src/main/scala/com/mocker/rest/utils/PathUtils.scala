package com.mocker.rest.utils

import com.mocker.rest.model.{Mock, MockResponse}
import com.mocker.rest.request.KVPair

import scala.collection.mutable

object PathUtils {

  type Path = String

  def buildFullPath(mock: Mock, mockResponse: MockResponse): Path = {
    val urlWithPathParams = mockResponse.pathParams.foldLeft(mock.path) {
      case (path, param) => path.replace(s"{${param.name}}", s"${param.value}")
    }
    mockResponse.queryParams.toList match {
      case Nil         => urlWithPathParams
      case head :: Nil => urlWithPathParams + s"?${head.name}=${head.value}"
      case head :: tail =>
        val n = urlWithPathParams + s"?${head.name}=${head.value}"
        tail.foldLeft(n) {
          case (path, param) => path + s"&${param.name}=${param.value}"
        }
    }
  }

  def extractPathParams(path: Path, mock: Mock): Seq[KVPair] = {
    val mockPathSegments = mock.path.split("/").toList.filter(_.nonEmpty)
    val pathSegments = path.split("/").toList.filter(_.nonEmpty)

    val pairs: mutable.Buffer[KVPair] = mutable.Buffer.empty
    mockPathSegments.zip(pathSegments).foreach {
      case (mockSegment, pathSegment) =>
        (mockSegment, pathSegment) match {
          case (m, s) if m.startsWith("{") && m.endsWith("}") =>
            pairs += KVPair(name = m.substring(1, m.length - 1), value = s)
          case _ => ()
        }
    }
    pairs.toSeq
  }

  implicit class PathMatcher(private val path: Path) extends AnyVal {

    def matchesPattern(pattern: String): Boolean = {
      val pathSegments = path.split("/").toList.filter(_.nonEmpty)
      val patternSegments = pattern.split("/").toList.filter(_.nonEmpty)

      pathSegments.size == patternSegments.size && pathSegments.zip(patternSegments).forall {
        case (pathChunk, patternChunk) =>
          (pathChunk, patternChunk) match {
            case (_, s) if s.startsWith("{") && s.endsWith("}") => true
            case (p, s)                                         => p == s
          }
      }
    }

  }

}
