@file:Suppress("ktlint:enum-entry-name-case")

package com.booktinder.api.ratpack.config

// Ratpack env variables transform ANY_FEATURE_TOGGLE to anyFeatureToggle. Hence, we need camelCase
enum class ZeusFeature {
  paris_15,
  paris_30,
  turin_15,
  seville_15,
  barcelona_15,
  madrid_15,
  sixt_invoices,
  redeem_subscription_type,
  redeemed_promotions_used_state,
  booking_checkout,
  bloomreach_activate_user,
  bloomreach_corrections_requested,
}

data class ZeusFeatureToggles(val toggles: MutableMap<ZeusFeature, Boolean>) {
  fun isEnabled(toggle: ZeusFeature) = toggles.getOrDefault(toggle, false)
}
