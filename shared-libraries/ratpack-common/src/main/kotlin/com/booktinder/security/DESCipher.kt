package com.booktinder.security

import java.security.MessageDigest
import java.util.Arrays
import java.util.Base64.getDecoder
import java.util.Base64.getEncoder
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class DESCipher(private val desConfig: DESConfig) {

  fun encrypt(message: String): String {
    val key: SecretKey = generateKey192()
    val iv = IvParameterSpec(ByteArray(8))
    val cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, key, iv)
    val plainTextBytes = message.toByteArray(charset("utf-8"))
    return getEncoder().encodeToString(cipher.doFinal(plainTextBytes))
  }

  fun decrypt(message: String): String {
    val key: SecretKey = generateKey192()
    val iv = IvParameterSpec(ByteArray(8))
    val decipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
    decipher.init(Cipher.DECRYPT_MODE, key, iv)

    val plainText = decipher.doFinal(getDecoder().decode(message))
    return String(plainText)
  }

  private fun generateKey192(): SecretKey {
    val md = MessageDigest.getInstance("md5")
    val digestOfPassword = md.digest(desConfig.password.toByteArray(charset("utf-8")))
    val keyBytes = Arrays.copyOf(digestOfPassword, 24)
    var j = 0
    var k = 16
    while (j < 8) {
      keyBytes[k++] = keyBytes[j++]
    }
    return SecretKeySpec(keyBytes, "DESede")
  }
}

class DESConfig(val password: String)
