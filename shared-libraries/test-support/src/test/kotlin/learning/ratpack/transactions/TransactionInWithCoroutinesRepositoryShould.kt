package learning.ratpack.transactions

import com.cooltra.zeus.api.ratpack.blocking
import com.cooltra.zeus.api.ratpack.suspendable
import io.kotest.core.annotation.Ignored
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldInclude
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.inTransactionUnchecked
import org.jdbi.v3.core.kotlin.mapTo
import org.jdbi.v3.core.kotlin.withHandleUnchecked
import ratpack.test.embed.EmbeddedApp

@Ignored
class TransactionInWithCoroutinesRepositoryShould : RepositoryIntegrationTest(
  { dataSource ->

    lateinit var repository: RepositoryWithCoroutines

    val jdbi = Jdbi.create(dataSource)
    val inTransaction: (suspend CoroutineScope.() -> Unit) -> Unit = inTransaction(jdbi)

    beforeAny {
      repository = RepositoryWithCoroutines(jdbi)
      repository.createTable()
    }

    "execute multiple updates in a single transaction" {
      val app = EmbeddedApp.of { spec ->
        spec.handlers { chain ->
          chain.get {
            suspendable {
              inTransaction {
                repository.addValue(1)
                repository.addValue(2)
              }
            }
          }
        }
      }

      app.test { it.get() }

      val afterTransaction = repository.getValues()
      afterTransaction shouldBe listOf(1, 2)
    }

    "execute multiple updates with nested transactions" {
      val app = EmbeddedApp.of { spec ->
        spec.handlers { chain ->
          chain.get {
            suspendable {
              inTransaction {
                repository.addValueInTransaction(1)
                repository.addValueInTransaction(2)
              }
            }
          }
        }
      }

      app.test { it.get() }

      val afterTransaction = repository.getValues()
      afterTransaction shouldBe listOf(1, 2)
    }

    "execute updates in different transactions" {

      val app = EmbeddedApp.of { spec ->
        spec.handlers { chain ->
          chain.get {
            suspendable {
              inTransaction {
                repository.addValue(1)
              }
              inTransaction {
                repository.addValue(2)
              }
            }
          }
        }
      }

      app.test { it.get() }

      val afterTransaction = repository.getValues()
      afterTransaction shouldBe listOf(1, 2)
    }

    "rollback previous updates when the transaction fails" {
      val app = EmbeddedApp.of { spec ->
        spec.handlers { chain ->
          chain.get {
            suspendable {
              inTransaction {
                repository.addValue(1)
                throw IllegalStateException("some error1")
              }
            }
          }
        }
      }

      app.test { it.text shouldInclude "IllegalStateException" }

      val afterTransaction = repository.getValues()
      afterTransaction shouldBe emptyList()
    }

    "execute updates when the failure is not inside the transaction" {
      val app = EmbeddedApp.of { spec ->
        spec.handlers { chain ->
          chain.get {
            suspendable {
              inTransaction {
                repository.addValue(1)
              }
              throw Exception("some error")
            }
          }
        }
      }

      app.test { it.text shouldInclude "IllegalStateException" }

      val afterTransaction = repository.getValues()
      afterTransaction shouldBe listOf(1)
    }

    "read not committed changes while transaction is open" {
      val app = EmbeddedApp.of { spec ->
        spec.handlers { chain ->
          chain.get {
            suspendable {
              inTransaction {
                repository.addValue(1)
                val values = repository.getValues()
                if (values != listOf(1)) throw IllegalStateException("some error")
              }
            }
          }
        }
      }

      app.test { it.text shouldInclude "IllegalStateException" }

      val afterTransaction = repository.getValues()
      afterTransaction shouldBe emptyList()
    }
  },
)

class RepositoryWithCoroutines(private val jdbi: Jdbi) {

  private val table = tableName()

  suspend fun createTable() = blocking {
    jdbi.withHandleUnchecked { handle ->
      handle.createUpdate("CREATE TABLE $table (value INT)")
        .execute()
    }
  }

  fun addValue(value: Int) {
    jdbi.withHandleUnchecked { handle ->
      println(handle)
      handle.createUpdate("INSERT INTO $table (value) VALUES(:value)")
        .bind("value", value)
        .execute()
    }
  }

  fun addBlockingValue(value: Int) {
    jdbi.useTransaction<Exception> {
      println(it)
      it.createUpdate("INSERT INTO $table (value) VALUES(:value)")
        .bind("value", value)
        .execute()
    }
  }

  suspend fun addValueInTransaction(value: Int): Unit = blocking {
    jdbi.withHandleUnchecked { h ->
      h.useTransaction<Exception> { handle ->
        handle.createUpdate("INSERT INTO $table (value) VALUES(:value)")
          .bind("value", value)
          .execute()
      }
    }
  }

  fun getValues(): MutableList<Int> =
    jdbi.withHandleUnchecked { handle ->
      handle.createQuery("SELECT value FROM $table")
        .mapTo<Int>()
        .list()
    }
}

fun tableName(): String {
  return List(20) { ('A'..'Z').random() }.joinToString("")
}

typealias InTransactionWithCoroutines = (action: suspend CoroutineScope.() -> Unit) -> Unit

fun inTransaction(jdbi: Jdbi): InTransactionWithCoroutines = { action ->
  jdbi.inTransactionUnchecked {
    runBlocking {
      action()
    }
  }
}
