package com.cooltra.zeus.stubs

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetupTest
import jakarta.mail.internet.MimeMessage

class EmailStub {

  private val greenMail: GreenMail = GreenMail(ServerSetupTest.SMTP.dynamicPort())

  fun start() {
    greenMail.start()
    greenMail.setUser("zeus_bot@cooltra.com", "dummyPassword")
  }

  fun stop() {
    greenMail.stop()
  }

  fun port() = greenMail.smtp.port

  fun getMessageFrom(domain: String): Array<MimeMessage> = greenMail.getReceivedMessagesForDomain(domain)
}
