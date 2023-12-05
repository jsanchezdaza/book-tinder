package com.booktinder.api.ratpack

import io.kotest.core.spec.style.FreeSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import java.util.Locale

class RatpackExtensionsShould : FreeSpec({

  "return locale from context " - {
    forAll(
      table(
        headers("language", "locale"),
        row("en", Locale.ENGLISH),
        row("e", Locale.ENGLISH),
        row("", Locale.ENGLISH),
        row("es", Locale("es")),
        row("undefined", Locale.ENGLISH),
        row("it-IT", Locale.ITALIAN),
        row("wsdf", Locale.ENGLISH),
        row("123", Locale.ENGLISH),
      ),
    ) { language, locale ->
      "when language is $language then locale is $locale" {
        createLocaleFrom(language) shouldBe locale
      }
    }
  }
},)
