package scala.cli.internal

import caseapp.core.argparser.{ArgParser, SimpleArgParser}
import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._

import scala.build.options.Secret
import scala.io.Codec

sealed abstract class PasswordOption extends Product with Serializable {
  def get(): Secret[String]
}

abstract class LowPriorityPasswordOption {

  private lazy val commandCodec: JsonValueCodec[List[String]] =
    JsonCodecMaker.make

  implicit lazy val argParser: ArgParser[PasswordOption] =
    SimpleArgParser.from("password") { str =>
      if (str.startsWith("value:"))
        Right(PasswordOption.Value(Secret(str.stripPrefix("value:"))))
      else if (str.startsWith("command:["))
        try {
          val command = readFromString(str.stripPrefix("command:"))(commandCodec)
          Right(PasswordOption.Command(command))
        }
        catch {
          case e: JsonReaderException =>
            Left(caseapp.core.Error.Other(s"Error decoding password command: ${e.getMessage}"))
        }
      else if (str.startsWith("command:")) {
        val command = str.stripPrefix("command:").split("\\s+").toSeq
        Right(PasswordOption.Command(command))
      }
      else
        Left(caseapp.core.Error.Other("Malformed password value (expected \"value:...\")"))
    }

}

object PasswordOption extends LowPriorityPasswordOption {

  final case class Value(value: Secret[String]) extends PasswordOption {
    def get(): Secret[String] = value
  }
  final case class Command(command: Seq[String]) extends PasswordOption {
    def get(): Secret[String] = {
      // should we add a timeout?
      val res = os.proc(command).call(stdin = os.Inherit)
      Secret(res.out.text(Codec.default)) // should we trim that?
    }
  }

  implicit lazy val optionArgParser: ArgParser[Option[PasswordOption]] =
    SimpleArgParser.from("password") { str =>
      if (str.trim.isEmpty) Right(None)
      else argParser(None, -1, -1, str).map(Some(_))
    }
}
