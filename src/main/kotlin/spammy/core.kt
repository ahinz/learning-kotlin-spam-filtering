package spammy

import spammy.training.*

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

// Open required for spring to work
@SpringBootApplication
open class Application {

  fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
  }

}


fun main(args: Array<String>) {
  if (args.count() == 1 && args.get(0) == "build-model")
    train()
  else
    Application().main(args)
}
