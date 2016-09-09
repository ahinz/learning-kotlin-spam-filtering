package spammy.training

import spammy.bayes.*

import com.fasterxml.jackson.module.kotlin.*

import java.util.regex.Pattern
import java.io.File
import java.util.*

fun <T> List<T>.shuffle(): List<T> {
  val t = LinkedList(this)
  Collections.shuffle(t)

  return t
}

// Just need this for a class reference, sure there must be a better
// way...
private class Dummy

fun getMessagesFromDirectory(d: String): List<Set<String>> {
  return File(Dummy::class.java.getClassLoader().getResource(d).getFile())
    .listFiles()
    .filter { it.isFile() }
    .toList()
    .map { parseMsg(it.readText()) }
}

fun pct(x: Int, n: Int): Double {
  if (n == 0)
    return 0.0
  else
    return 100.0 * x / n
}

fun train() {
  println("Building a new training model")
  println("Loading HAM messages...")
  val ham = getMessagesFromDirectory("ham").shuffle()
  println("Loading SPAM messages...")
  val spam = getMessagesFromDirectory("spam").shuffle()
  println("Building model...")

  val b = BayesClassifier.emptyClassifier()

  val nDocsToTrainHam = (ham.count() * 0.80).toInt()
  val hamTraining = ham.take(nDocsToTrainHam)
  val hamTest = ham.drop(nDocsToTrainHam)

  val nDocsToTrainSpam = (spam.count() * 0.80).toInt()
  val spamTraining = spam.take(nDocsToTrainSpam)
  val spamTest = spam.drop(nDocsToTrainSpam)

  // Training Set

  hamTraining.forEach { b.trainMessage(false, it) }
  spamTraining.forEach { b.trainMessage(true, it) }

  println("Saving model...")
  val mapper = jacksonObjectMapper()
  val json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(b.toMap())

  File("src/main/resources/model.json").printWriter().use { out ->
    out.println(json)
  }

  println("Calculating Stats...")

  val positives = spamTest
    .map { b.probMsgIsSpam(it.toList()) }
    .map { it != null && it > 0.5 }

  val negatives = hamTest
    .map { b.probMsgIsSpam(it.toList()) }
    .map { it != null && it < 0.5 }

  val nPos = spamTest.count()
  val nNeg = hamTest.count()
  val nCorrectPos = positives.filter({it}).count()
  val nCorrectNeg = negatives.filter({it}).count()
  val nTotal = nPos + nNeg

  println(String.format("Stats: %d Correct Pos: %.2f%% (%d/%d) Correct Neg: %.2f%% (%d/%d)",
    nTotal,
    pct(nCorrectPos, nPos), nCorrectPos, nPos,
    pct(nCorrectNeg, nNeg), nCorrectNeg, nNeg))
}

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
