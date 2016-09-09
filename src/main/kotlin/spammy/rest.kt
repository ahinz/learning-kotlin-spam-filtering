package spammy.rest

import org.springframework.web.bind.annotation.*
import com.fasterxml.jackson.module.kotlin.*

import java.io.FileNotFoundException

import spammy.training.*
import spammy.bayes.*

data class MessageResult(val probSpam: Double)

fun loadModel(path: String = "model.json"): BayesClassifier {
  try {
    val json = SpamController::class.java.getClassLoader().getResourceAsStream(path).reader().readText()
    val mapper = jacksonObjectMapper()

    return mapper.readValue<BayesClassifier>(json)
  } catch (e: Exception) {
    println("ERROR: Model not found... check the readme for model generation instructions")
    throw e
  }
}


@RestController
class SpamController {
  companion object {
    val model: BayesClassifier by lazy { loadModel() }
  }


  @RequestMapping(value="/is-spam", method=arrayOf(RequestMethod.POST))
  fun isSpam(@RequestBody body: String): MessageResult {
    val p = SpamController.model.probMsgIsSpam(parseMsg(body).toList())
    return MessageResult(p ?: -1.0)
  }
}
