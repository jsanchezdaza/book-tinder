package learning.ratpack

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import ratpack.exec.Blocking
import ratpack.exec.Promise
import ratpack.test.exec.ExecHarness
import java.lang.Thread.sleep
import java.time.Duration

class PromiseTimeShould : StringSpec({

  "track time of a promise" {
    var time: Duration = Duration.ZERO
    ExecHarness.yieldSingle {
      Promise.sync { sleep(500); 1 }.time { time = it }
    }

    time.toMillis() shouldBeGreaterThanOrEqual 500
  }

  "track time of a blocking promise" {
    var time: Duration = Duration.ZERO
    ExecHarness.yieldSingle {
      Blocking.get { sleep(500); 1 }.time { time = it }
    }

    time.toMillis() shouldBeGreaterThanOrEqual 500
  }
},)
