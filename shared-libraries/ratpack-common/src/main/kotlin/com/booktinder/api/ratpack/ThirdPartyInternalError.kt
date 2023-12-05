package com.booktinder.api.ratpack

import ratpack.http.Status

class ThirdPartyInternalError(val body: String, val status: Status) : Exception("Body=$body, status code=$status")
