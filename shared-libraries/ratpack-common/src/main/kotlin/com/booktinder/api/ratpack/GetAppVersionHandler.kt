package com.booktinder.api.ratpack

import ratpack.handling.Context
import ratpack.handling.Handler

class GetAppVersionHandler : Handler {
  override fun handle(ctx: Context) {
    val appVersion = try {
      ctx.request.headers.get("User-Agent").toAppVersion()
    } catch (e: Exception) {
      AppVersion.unknown()
    }
    ctx.request.add(appVersion)
    ctx.next()
  }
}

private fun String?.toAppVersion(): AppVersion {
  if (this == null) {
    return AppVersion.unknown()
  }

  return AppVersion.from(this)
}

data class AppVersion(val platform: Platform, val semver: Semver) {
  companion object {
    fun from(userAgent: String): AppVersion {
      val userAgentProcessed = userAgent.split(" ").first().split("/")
      val platform = when (userAgentProcessed.first().first()) {
        'e' -> Platform.IOS
        'C' -> Platform.ANDROID
        else -> Platform.UNKNOWN
      }
      val semver = userAgentProcessed.last().split(".").map { it.toInt() }.let {
        Semver(
          it[0],
          it[1],
          it[2],
        )
      }

      return AppVersion(platform, semver)
    }

    fun unknown() = AppVersion(Platform.UNKNOWN, Semver(0, 0, 0))
  }

  enum class Platform {
    IOS, ANDROID, UNKNOWN
  }

  data class Semver(val major: Int, val minor: Int, val patch: Int) : Comparable<Semver> {
    override fun compareTo(other: Semver): Int {
      if (major > other.major) return 1
      if (major < other.major) return -1
      if (minor > other.minor) return 1
      if (minor < other.minor) return -1
      if (patch > other.patch) return 1
      if (patch < other.patch) return -1

      return 0
    }
  }
}
