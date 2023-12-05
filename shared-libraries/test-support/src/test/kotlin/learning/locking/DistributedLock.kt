package learning.locking

import com.cooltra.zeus.containers.POSTGRES_VERSION
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.testcontainers.containers.PostgreSQLContainer
import java.sql.Connection
import java.sql.DriverManager

class DistributedLock : StringSpec({
  isolationMode = IsolationMode.SingleInstance

  val postgresSQLContainer = PostgreSQLContainer<Nothing>(POSTGRES_VERSION).apply {
    withDatabaseName("learning-tests")
    withReuse(false)
  }

  beforeSpec {
    postgresSQLContainer.start()
  }

  afterSpec {
    postgresSQLContainer.stop()
  }

  "distributed lock" {
    val conn1 = newConnection(postgresSQLContainer)
    val conn2 = newConnection(postgresSQLContainer)

    pg_lock(conn1) shouldBe true
    pg_lock(conn1) shouldBe true
    pg_lock(conn2) shouldBe false

    conn1.close()
    val conn3 = newConnection(postgresSQLContainer)

    pg_lock(conn2) shouldBe true
    pg_lock(conn3) shouldBe false
    conn2.close()
    conn3.close()
  }

  "lock is available after unlock" {
    val conn1 = newConnection(postgresSQLContainer)
    val conn2 = newConnection(postgresSQLContainer)

    pg_lock(conn1) shouldBe true
    pg_unlock(conn1) shouldBe true
    pg_lock(conn2) shouldBe true

    conn1.close()
    conn2.close()
  }

  "lock is cumulative" {
    val conn1 = newConnection(postgresSQLContainer)
    val conn2 = newConnection(postgresSQLContainer)

    pg_lock(conn1) shouldBe true
    pg_lock(conn1) shouldBe true
    pg_unlock(conn1) shouldBe true
    pg_lock(conn2) shouldBe false

    conn1.close()
    conn2.close()
  }

  "unlock with another connection" {
    val conn1 = newConnection(postgresSQLContainer)
    val conn2 = newConnection(postgresSQLContainer)

    pg_lock(conn1) shouldBe true
    pg_unlock(conn2) shouldBe false

    conn1.close()
    conn2.close()
  }
},)

private fun newConnection(postgresSQLContainer: PostgreSQLContainer<Nothing>) =
  DriverManager.getConnection(
    postgresSQLContainer.jdbcUrl,
    postgresSQLContainer.username,
    postgresSQLContainer.password,
  )

private fun pg_lock(connection: Connection): Boolean {
  val ps = connection.prepareStatement("SELECT pg_try_advisory_lock(1)")
  ps.executeQuery().use {
    it.next()
    return it.getBoolean(1)
  }
}

private fun pg_unlock(connection: Connection): Boolean {
  val ps = connection.prepareStatement("SELECT pg_advisory_unlock(1)")
  ps.executeQuery().use {
    it.next()
    return it.getBoolean(1)
  }
}
