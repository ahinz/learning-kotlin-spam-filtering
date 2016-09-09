package spammy

import spammy.training.*

import com.fasterxml.jackson.module.kotlin.*

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*

import kotlin.test.*

fun assertClose(a: Double, b: Double, ε: Double = 0.000001, message: String? = null) {
    val Δ = Math.abs(a - b)
    val actual = Δ < ε
    asserter.assertTrue(message ?: String.format("Expected |%f-%f| = %f < %f", a, b, Δ, ε), actual)
}

infix fun Double.shouldBeCloseTo(a: Double) {
    assertClose(this, a)
}

infix fun <T> Set<T>.shouldContain(t: T) {
  asserter.assertTrue(String.format("``%s'' not found", t), this.contains(t))
}

infix fun <T> Set<T>.shouldNotContain(t: T) {
  asserter.assertTrue(String.format("``%s'' not found", t), !this.contains(t))
}

class TrainingSpec: Spek({
  describe("training") {
    it("should parse messages") {
      //val string = File("unixdict.txt").readText(charset = Charsets.UTF_8)
      // new File(classLoader.getResource("file/test.xml").getFile());
      val exampleMsg = javaClass.getClassLoader().getResourceAsStream("example_msg.txt").reader().readText()

      val words = parseMsg(exampleMsg)

      // shouldn't have any header fields"
      words shouldNotContain "Discussion"

      // shouldn't contain short words
      words shouldNotContain "the"

      // Should contain some of the message words
      words shouldContain "reach"
      words shouldContain "repository"
    }
  }
})

class BayesSpec: Spek({
  describe("bayes") {
    val words = mapOf(
      "word1" to Word(3, 2),
      "word2" to Word(2, 5))

    val b = BayesClassifier(0.5, 10, 20, words.toMutableMap())

    it("should be json serializable") {
      val mapper = jacksonObjectMapper()

      // A bit silly that the library only supports reading
      // but not writing. For some reason it downcases all of the
      // keys?
      val json = mapper.writeValueAsString(b.toMap())

      val bayes = mapper.readValue<BayesClassifier>(json)

      assertEquals(bayes, b)
    }

    it("should record messages") {
      val z = BayesClassifier(0.5, 0, 0, mutableMapOf())
        .trainMessage(true, setOf("word1", "word2"))
        .trainMessage(true, setOf("word1", "word2"))
        .trainMessage(true, setOf("word2"))
        .trainMessage(true, setOf("word2"))
        .trainMessage(true, setOf("word2"))
        .trainMessage(false, setOf("word1", "word2"))
        .trainMessage(false, setOf("word1", "word2"))
        .trainMessage(false, setOf("word1"))

      val tgt = BayesClassifier(0.5, 3, 5, words.toMutableMap())
      assertEquals(z, tgt)
    }

    it("should calculate individual probs properly") {
      b.probWordIsSpam("word1")!! shouldBeCloseTo (2.0 / 20.0)
      b.probWordIsSpam("word2")!! shouldBeCloseTo (5.0 / 20.0)

      b.probWordIsHam("word1")!! shouldBeCloseTo (3.0 / 10.0)
      b.probWordIsHam("word2")!! shouldBeCloseTo (2.0 / 10.0)

      assertNull(b.probWordIsHam("wordX"))
    }

    it("should compute the join probability correctly") {
      val pWord1 = b.probWordIsSpamGivenMessage("word1")
      val pWord2 = b.probWordIsSpamGivenMessage("word2")

      val p = b.probMsgIsSpam(listOf("word1", "word2"))!!

      // p1 = p(word1) = (0.10 * 0.5)/((0.10*0.5) + (0.3*0.5))
      // p2 = p(word2) = (0.25 * 0.5)/((0.25*0.5) + (0.2*0.5))
      pWord1!! shouldBeCloseTo 0.25
      pWord2!! shouldBeCloseTo 0.55555555

      // p = (p1 * p2) / (p1*p2 + (1 - p1)*(1 - p2))
      p shouldBeCloseTo 0.29411764238
    }
  }
})
