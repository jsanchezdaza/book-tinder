package com.booktinder.api.ratpack

import ratpack.exec.Promise
import ratpack.jdbctx.Transaction
import javax.sql.DataSource

// Translation of `(() -> Promise<Unit>) -> Promise<Unit>` so Guice can inject it as dependency
typealias InTransaction = Function1<@JvmSuppressWildcards Function0<@JvmSuppressWildcards Promise<Unit>>, @JvmSuppressWildcards Promise<Unit>>

fun wrapInTransaction(dataSource: DataSource): InTransaction = { action ->
  val tx = Transaction.get { dataSource.connection }
  tx.wrap(action())
}
