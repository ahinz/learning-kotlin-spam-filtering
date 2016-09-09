package spammy.bayes

import java.util.HashMap

// Very silly this is needed...
fun <K, V> Map<K, V>.toMutableMap(): MutableMap<K, V> {
  return HashMap(this)
}

fun <K, V, A, B> Map<K, V>.mapToMap(transform: (K, V) -> Pair<A,B>): Map<A, B> {
    return this
            .map { transform(it.key, it.value) }
            .toMap()
}

fun <K, V, A> Map<K, V>.mapToMapValue(transform: (K, V) -> A): Map<K, A> {
    return this.mapToMap { key, value -> Pair(key, transform(key, value)) }

}

data class Word(val hamMessages: Int, val spamMessages: Int) {
  fun update(spam: Boolean): Word {
    return Word(
      hamMessages + if (spam) 0 else 1,
      spamMessages + if (spam) 1 else 0)
  }
}


// Mutable classifier, immutable would be cool but kotlin doesn't appear to have
// efficient immutable collections
data class BayesClassifier(val probMsgIsSpam: Double, var nHam: Int, var nSpam: Int, val nSeen: MutableMap<String, Word>) {
  companion object {
    fun emptyClassifier(probMsgIsSpam: Double = 0.5): BayesClassifier {
      return BayesClassifier(probMsgIsSpam, 0, 0, mutableMapOf())
    }
  }

  val emptyWord = Word(0,0)

  fun toMap(): Map<String, Any> {
    return mapOf(
      "probMsgIsSpam" to probMsgIsSpam,
      "nHam" to nHam,
      "nSpam" to nSpam,
      "nSeen" to nSeen)
  }

  fun trainMessage(spam: Boolean, msg: Set<String>): BayesClassifier {
    msg.forEach { word ->
      val w = nSeen.getOrElse(word, { emptyWord })

      nSeen.put(word, w.update(spam))
    }

    if (spam)
      nSpam += 1
    else
      nHam += 1

    return this
  }

  fun computeProb(p: Int?, n: Int): Double? {
    if (n > 0 && p != null) {
      return (p.toDouble() / n.toDouble())
    } else {
      return null
    }
  }

  fun probWordIsSpam(w: String): Double? {
    return computeProb(nSeen.get(w)?.spamMessages, nSpam)
  }

  fun probWordIsHam(w: String): Double? {
    return computeProb(nSeen.get(w)?.hamMessages, nHam)
  }

  fun probWordIsSpamGivenMessage(w: String): Double? {
    val prWordIsSpam = probWordIsSpam(w)
    val prWordIsHam = probWordIsHam(w)

    // monad comprehensions would be nice here
    if (prWordIsSpam == null || prWordIsHam == null) {
      return null
    } else {
      val prMsgIsHam = 1.0 - probMsgIsSpam

      val num = prWordIsSpam * probMsgIsSpam
      val denom = (prWordIsSpam * probMsgIsSpam +
        prWordIsHam * prMsgIsHam)

      return num / denom
    }
  }

  // Really? These have got to be in a standard library somewhere
  // maybe these are infix defined (will check later)
  fun max(a: Double, b: Double): Double { return if (a > b) a else b }
  fun min(a: Double, b: Double): Double { return if (a < b) a else b }

  // Note that we can't do log(0) so we truncate the ends
  // of the probability range
  fun probInRange(p: Double): Double {
    return max(0.05, min(p, 0.95))
  }

  fun probMsgIsSpam(w: List<String>): Double? {
    val spamProbsLn = w
      .map { probWordIsSpamGivenMessage(it) }
      .filterNotNull()
      .map { probInRange(it) }
      .map { p -> Math.log(1 - p) - Math.log(p) }

    if (spamProbsLn.count() == 0)
      return null
    else {
      val spamProbLn = spamProbsLn.reduce { p1, p2 -> p1 + p2 }
      return 1.0 / (1.0 + Math.exp(spamProbLn))
    }
  }
}
