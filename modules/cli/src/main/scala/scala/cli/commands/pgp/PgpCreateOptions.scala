package scala.cli.commands.pgp

import caseapp._

import scala.build.Os
import scala.cli.internal.PasswordOption

@HelpMessage("Create PGP key pair")
final case class PgpCreateOptions(
  email: String,
  password: PasswordOption,
  dest: Option[String] = None,
  pubDest: Option[String] = None,
  secretDest: Option[String] = None
) {
  def publicKeyPath: os.Path = {
    val str = pubDest.filter(_.trim.nonEmpty)
      .orElse(secretDest.filter(_.trim.nonEmpty).map(_.stripSuffix(".skr") + ".pub"))
      .orElse(dest.filter(_.trim.nonEmpty).map(_ + ".pub"))
      .getOrElse("key.pub")
    os.Path(str, Os.pwd)
  }
  def secretKeyPath: os.Path = {
    val str = secretDest.filter(_.trim.nonEmpty)
      .orElse(pubDest.filter(_.trim.nonEmpty).map(_.stripSuffix(".pub") + ".skr"))
      .orElse(dest.filter(_.trim.nonEmpty).map(_ + ".skr"))
      .getOrElse("key.skr")
    os.Path(str, Os.pwd)
  }
}

object PgpCreateOptions {
  implicit lazy val parser: Parser[PgpCreateOptions] = Parser.derive
  implicit lazy val help: Help[PgpCreateOptions]     = Help.derive
}
