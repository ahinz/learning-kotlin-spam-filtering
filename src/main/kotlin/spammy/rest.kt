package spammy

import java.util.concurrent.atomic.AtomicLong
import org.springframework.web.bind.annotation.*

val template = "Hello, %s!"

data class Greeting(val id: Long, val content: String)

@RestController
class GreetingController {
        // Can't be the same name?
        companion object {
                val counter = AtomicLong()
        }


        @RequestMapping("/greeting")
        fun greeting(@RequestParam(value="name", defaultValue="World") name: String): Greeting {
                return Greeting(GreetingController.counter.incrementAndGet(), String.format(template, name))
        }
}
