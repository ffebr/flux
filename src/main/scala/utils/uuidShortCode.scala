package utils

import model.BoardId

import java.util.UUID
import scala.annotation.tailrec

object uuidShortCode:
  def gen(length: Int = 8, uuid: UUID): BoardId =
    val hex = uuid.toString.replaceAll("-", "")
    val num = BigInt(hex, 16)
    val chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val base = BigInt(chars.length)

    @tailrec
    def go(temp: BigInt, code: String): String =
      if code.length == length then code
      else
        val remainder = (temp % base).toInt
        go(temp / base, code + chars(remainder))

    BoardId(go(num, ""))
