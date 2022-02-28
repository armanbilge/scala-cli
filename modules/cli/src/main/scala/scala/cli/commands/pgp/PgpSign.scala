package scala.cli.commands.pgp

import caseapp.core.RemainingArgs

import java.io.InputStream

import scala.build.Os
import scala.cli.commands.ScalaCommand
import scala.cli.publish.BouncycastleSigner

object PgpSign extends ScalaCommand[PgpSignOptions] {
  def run(options: PgpSignOptions, args: RemainingArgs): Unit = {

    val privateKey = BouncycastleSigner.readSecretKey(os.read.inputStream(options.secretKeyPath))
    val signer     = BouncycastleSigner(privateKey, options.password.get())

    for (arg <- args.all) {
      val path = os.Path(arg, Os.pwd)
      val dest = (path / os.up) / s"${path.last}.asc"

      val res = signer.sign { f =>
        var is: InputStream = null
        try {
          is = os.read.inputStream(path)
          val b    = Array.ofDim[Byte](16 * 1024)
          var read = 0
          while ({
            read = is.read(b)
            read >= 0
          })
            if (read > 0)
              f(b, 0, read)
        }
        finally is.close()
      }

      res match {
        case Left(err) =>
          System.err.println(err)
          sys.exit(1)
        case Right(value) =>
          if (options.force)
            os.write.over(dest, value)
          else if (os.exists(dest)) {
            System.err.println(
              s"Error: ${arg + ".asc"} already exists. Pass --force to force overwriting it."
            )
            sys.exit(1)
          }
          else
            os.write(dest, value)
      }
    }
  }
}
