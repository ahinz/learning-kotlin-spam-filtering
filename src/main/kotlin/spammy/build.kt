package spammy.training

import java.util.regex.Pattern

fun parseMsg(s: String): Set<String> {
  val i = s.indexOf("\n\n")

  val trimmedString = if (i > 0)
    s.substring(i)
  else
    s

  return trimmedString
    .toLowerCase()
    .replace("[^a-z][^a-z0-9]*".toRegex(), " ")
    .split(regex=Pattern.compile("\\s+"))
    .filter { it.length >= 4 }
    .toSet()
}
