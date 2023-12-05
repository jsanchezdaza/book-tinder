package learning.ratpack

import ratpack.exec.ExecController
import ratpack.server.RatpackServer
import ratpack.service.Service
import ratpack.service.StartEvent
import ratpack.service.StopEvent
import java.util.concurrent.TimeUnit

class ServiceSchedule : Service, Runnable {
  override fun getName(): String {
    return javaClass.simpleName
  }

  override fun onStart(event: StartEvent) {
    ExecController.require().executor
      .scheduleAtFixedRate(this, 0, 1, TimeUnit.SECONDS)
  }

  override fun onStop(event: StopEvent) {
    println("stop:$name")
  }

  override fun run() {
    ExecController.require().fork().start { println("running: $name") }
  }
}

fun main() {
  RatpackServer.start { it.registryOf { it.add(ServiceSchedule()) } }
}
