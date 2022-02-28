package scala.cli.commands.pgp

import caseapp._

import scala.build.Os
import scala.cli.internal.PasswordOption

@HelpMessage("Sign files with PGP")
final case class PgpSignOptions(
  password: PasswordOption,
  secretKey: String,
  @ExtraName("f")
  force: Boolean = false
) {
  def secretKeyPath: os.Path =
    os.Path(secretKey, Os.pwd)
}

object PgpSignOptions {
  implicit lazy val parser: Parser[PgpSignOptions] = Parser.derive
  implicit lazy val help: Help[PgpSignOptions]     = Help.derive
}
