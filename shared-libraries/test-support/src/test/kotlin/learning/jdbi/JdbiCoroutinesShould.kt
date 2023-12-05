package learning.jdbi

import com.cooltra.zeus.api.ratpack.blocking
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import learning.ratpack.transactions.RepositoryIntegrationTest
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.inTransactionUnchecked
import org.jdbi.v3.core.kotlin.mapTo
import org.jdbi.v3.core.kotlin.useHandleUnchecked
import org.jdbi.v3.core.kotlin.withHandleUnchecked

suspend fun <T> Jdbi.transactional(spec: suspend (Handle) -> T) = blocking {
  val cc = coroutineContext
  inTransactionUnchecked { handle ->
    runBlocking(cc) {
      println("${Thread.currentThread()} >> run blocking tx ${handle.isInTransaction}")
      spec(handle)
    }
  }
}

class JdbiCoroutinesShould : RepositoryIntegrationTest(
  { dataSource ->
    lateinit var repository: Repository
    val jdbi = Jdbi.create(dataSource)

    beforeAny {
      repository = Repository(jdbi)
      repository.createTable()
    }

    "execute multiple updates in a single transaction" {
      repository.jdbi.transactional {
        repository.addValue(1)
        repository.addValue(2)
      }

      val afterTransaction = repository.getValues()
      afterTransaction shouldBe listOf(1, 2)
    }

    "execute multiple updates with nested transactions" {
      jdbi.transactional {
        repository.addValueInTransaction(1)
        repository.addValueInTransaction(2)
      }

      val afterTransaction = repository.getValues()
      afterTransaction shouldBe listOf(1, 2)
    }

    "execute updates in different transactions" {
      jdbi.transactional {
        repository.addValue(it, 1)
      }
      jdbi.transactional {
        repository.addValue(it, 2)
      }

      val afterTransaction = repository.getValues()
      afterTransaction shouldBe listOf(1, 2)
    }

    "rollback previous updates when the transaction fails" {
      try {
        jdbi.transactional {
          repository.addValue(it, 1)
          throw IllegalStateException("some error")
        }
      } catch (error: Exception) {
        // do nothing
      }

      val afterTransaction = repository.getValues()
      afterTransaction shouldBe emptyList()
    }
  },
)

fun randomName(): String {
  return List(20) { ('A'..'Z').random() }.joinToString("")
}

class Repository(val jdbi: Jdbi) {

  private val table = randomName()

  suspend fun createTable() = blocking {
    jdbi.withHandleUnchecked { handle ->
      handle.createUpdate("CREATE TABLE $table (value INT)")
        .execute()
    }
  }

  suspend fun addValue(handle: Handle, value: Int) = blocking {
    println("${Thread.currentThread()} >>> in tx ${handle.isInTransaction}")
    handle.createUpdate("INSERT INTO $table (value) VALUES(:value)")
      .bind("value", value)
      .execute()
  }

  suspend fun addValue(value: Int) = blocking {
    jdbi.useHandleUnchecked { handle ->
      println("${Thread.currentThread()} >>> in tx ${handle.isInTransaction}")
      handle.createUpdate("INSERT INTO $table (value) VALUES(:value)")
        .bind("value", value)
        .execute()
    }
  }

  suspend fun addValueInTransaction(value: Int) = blocking {
    jdbi.withHandleUnchecked { h ->
      println("${Thread.currentThread()} >>> in tx ${h.isInTransaction}")
      h.useTransaction<Exception> { handle ->
        println("${Thread.currentThread()} >>> within tx ${handle.isInTransaction}")
        handle.createUpdate("INSERT INTO $table (value) VALUES(:value)")
          .bind("value", value)
          .execute()
      }
    }
  }

  suspend fun getValues() = blocking {
    jdbi.withHandleUnchecked { handle ->
      handle.createQuery("SELECT value FROM $table")
        .mapTo<Int>()
        .list()
    }
  }
}
