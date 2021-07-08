package scala.cli.integration

import java.io.IOException
import java.nio.charset.StandardCharsets

final case class TestInputs(
  files: Seq[(os.RelPath, String)]
) {
  def fromRoot[T](f: os.Path => T): T =
    TestInputs.withTmpDir("scala-cli-tests-") { tmpDir =>
      for ((relPath, content) <- files) {
        val path = tmpDir / relPath
        os.write(path, content.getBytes(StandardCharsets.UTF_8), createFolders = true)
      }

      f(tmpDir)
    }
}

object TestInputs {

  private def withTmpDir[T](prefix: String)(f: os.Path => T): T = {
    val tmpDir = os.temp.dir(prefix = prefix)
    try f(tmpDir)
    finally {
      try os.remove.all(tmpDir)
      catch {
        case ex: IOException =>
          System.err.println(s"Ignoring $ex while removing $tmpDir")
      }
    }
  }
}