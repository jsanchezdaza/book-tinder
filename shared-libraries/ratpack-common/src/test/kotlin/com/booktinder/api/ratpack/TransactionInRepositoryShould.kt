package com.booktinder.api.ratpack

import io.kotest.matchers.shouldBe
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.mapTo
import org.jdbi.v3.core.kotlin.withHandleUnchecked
import ratpack.exec.Blocking
import ratpack.exec.Promise
import ratpack.test.exec.ExecHarness.yieldSingle

class TransactionInRepositoryShould : RepositoryIntegrationTest(
  { dataSource ->

    lateinit var repository: Repository

    val jdbi = Jdbi.create(dataSource)
    val inTransaction: (() -> Promise<Unit>) -> Promise<Unit> = wrapInTransaction(dataSource)

    beforeAny {
      repository = Repository(jdbi)
      yieldSingle { repository.createTable() }.isError shouldBe false
    }

    "execute multiple updates in a single transaction" {
      yieldSingle {
        inTransaction {
          repository.addValue(1)
            .flatMap {
              repository.addValue(2)
            }
        }
      }.isError shouldBe false

      val afterTransaction = yieldSingle { repository.getValues() }.valueOrThrow
      afterTransaction shouldBe listOf(1, 2)
    }

    "execute multiple updates with nested transactions" {
      yieldSingle {
        inTransaction {
          repository.addValueInTransaction(1)
            .flatMap {
              repository.addValueInTransaction(2)
            }
        }
      }.isError shouldBe false

      val afterTransaction = yieldSingle { repository.getValues() }.valueOrThrow
      afterTransaction shouldBe listOf(1, 2)
    }

    "execute updates in different transactions" {
      yieldSingle {
        inTransaction {
          repository.addValue(1)
        }.flatMap {
          inTransaction {
            repository.addValue(2)
          }
        }
      }.isError shouldBe false

      val afterTransaction = yieldSingle { repository.getValues() }.valueOrThrow
      afterTransaction shouldBe listOf(1, 2)
    }

    "rollback previous updates when the transaction fails" {
      yieldSingle {
        inTransaction {
          repository.addValue(1)
            .flatMap {
              Promise.error(IllegalStateException("some error"))
            }
        }
      }.isError shouldBe true

      val afterTransaction = yieldSingle { repository.getValues() }.valueOrThrow
      afterTransaction shouldBe emptyList()
    }

    "execute updates when the failure is not inside the transaction" {
      yieldSingle {
        inTransaction {
          repository.addValue(1)
        }.flatMap {
          Promise.error<Unit>(IllegalStateException("some error"))
        }
      }.isError shouldBe true

      val afterTransaction = yieldSingle { repository.getValues() }.valueOrThrow
      afterTransaction shouldBe listOf(1)
    }

    "read not committed changes while transaction is open" {
      yieldSingle {
        inTransaction {
          repository.addValue(1)
            .flatMap { repository.getValues() }
            .flatMap { values ->
              Promise.sync {
                if (values != listOf(1)) throw IllegalStateException()
              }
            }
        }
      }.isError shouldBe false
    }
  },
)

class Repository(private val jdbi: Jdbi) {

  private val table = randomName()

  fun createTable(): Promise<Unit> = Blocking.get {
    jdbi.withHandleUnchecked { handle ->
      handle.createUpdate("CREATE TABLE $table (value INT)")
        .execute()
    }
  }

  fun addValue(value: Int): Promise<Unit> = Blocking.get {
    jdbi.withHandleUnchecked { handle ->
      handle.createUpdate("INSERT INTO $table (value) VALUES(:value)")
        .bind("value", value)
        .execute()
    }
  }

  fun addValueInTransaction(value: Int): Promise<Unit> = Blocking.get {
    jdbi.withHandleUnchecked { h ->
      h.useTransaction<Exception> { handle ->
        handle.createUpdate("INSERT INTO $table (value) VALUES(:value)")
          .bind("value", value)
          .execute()
      }
    }
  }

  fun getValues(): Promise<MutableList<Int>> = Blocking.get {
    jdbi.withHandleUnchecked { handle ->
      handle.createQuery("SELECT value FROM $table")
        .mapTo<Int>()
        .list()
    }
  }
}

fun randomName(): String {
  return List(20) { ('A'..'Z').random() }.joinToString("")
}
