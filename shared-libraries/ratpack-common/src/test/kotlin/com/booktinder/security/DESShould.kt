package com.booktinder.security

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class DESShould : StringSpec(
  {

    val desCipher = DESCipher(DESConfig("123456789012345678901234"))

    "should encrypt and decrypt correctly" {
      val text = "Lorem Ipsum"
      val encrypt = desCipher.encrypt(text)
      encrypt shouldBe "9Mqb7SHTi5f4/KJfOtAe9A=="
      desCipher.decrypt(encrypt) shouldBe text
    }
  },
)
