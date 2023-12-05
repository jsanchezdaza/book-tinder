package com.booktinder.client

import io.netty.handler.codec.PrematureChannelClosureException
import ratpack.exec.Promise
import ratpack.exec.util.retry.AttemptRetryPolicy
import ratpack.exec.util.retry.FixedDelay
import ratpack.func.Action
import ratpack.func.BiAction.noop
import ratpack.http.client.HttpClient
import ratpack.http.client.HttpClientSpec
import ratpack.http.client.ReceivedResponse
import ratpack.http.client.RequestSpec
import ratpack.http.client.StreamedResponse
import java.net.URI
import java.time.Duration

/**
 * This client should be used instead of the `ratpack.http.client.HttpClient` because it's implementing a retry
 * mechanism which prevents deployment downtime.
 * Server IPs are stored, and during deployment rollout, connexions pointing to old server replicas are breaking,
 * throwing `io.netty.handler.codec.PrematureChannelClosureException` errors. The native Ratpack http client doesn't
 * have any internal retry mechanism, as for e.g. OkHttpClient, and neither provides an easier way to implement it,
 * like interceptors, so this wrapper has been created to implement the retry logic.
 */
class RatpackHttpClient(private val delegate: HttpClient) : HttpClient by delegate {

  override fun get(uri: URI?, action: Action<in RequestSpec>?): Promise<ReceivedResponse> =
    delegate.get(uri, action).retryOnPrematureChannelClosureException()

  override fun post(uri: URI?, action: Action<in RequestSpec>?): Promise<ReceivedResponse> =
    delegate.post(uri, action).retryOnPrematureChannelClosureException()

  override fun request(uri: URI?, action: Action<in RequestSpec>?): Promise<ReceivedResponse> =
    delegate.request(uri, action).retryOnPrematureChannelClosureException()

  override fun requestStream(uri: URI?, requestConfigurer: Action<in RequestSpec>?): Promise<StreamedResponse> =
    delegate.requestStream(uri, requestConfigurer).retryOnPrematureChannelClosureException()

  private fun <T> Promise<T>.retryOnPrematureChannelClosureException(): Promise<T> =
    this.retryIf(
      { error -> error is PrematureChannelClosureException },
      AttemptRetryPolicy.of { policy ->
        policy
          .maxAttempts(10)
          .delay(FixedDelay.of(Duration.ofMillis(50)))
      },
      noop(),
    )

  override fun copyWith(action: Action<in HttpClientSpec>?): RatpackHttpClient =
    RatpackHttpClient(delegate.copyWith(action))
}
