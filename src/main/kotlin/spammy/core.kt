package spammy

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
  println(listOf(*args))
  //Application().main(args)
}
