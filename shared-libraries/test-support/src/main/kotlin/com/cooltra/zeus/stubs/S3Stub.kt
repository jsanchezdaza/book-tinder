package com.cooltra.zeus.stubs

import com.cooltra.zeus.containers.initLocalStackS3
import com.google.common.io.Resources
import org.testcontainers.containers.localstack.LocalStackContainer
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.Tag
import software.amazon.awssdk.services.s3.model.Tagging
import java.util.concurrent.CompletionException

class S3Stub {

  private val container: LocalStackContainer = initLocalStackS3()
  private val documentImagesBucket = "apps-document-images"
  private val invoicesBucket = "cooltra-invoices"

  lateinit var s3Client: S3AsyncClient

  fun start() {
    s3Client = S3AsyncClient.builder()
      .endpointOverride(container.getEndpointOverride(LocalStackContainer.Service.S3))
      .region(Region.of(container.region))
      .credentialsProvider(
        StaticCredentialsProvider.create(
          AwsBasicCredentials.create(
            container.accessKey,
            container.secretKey,
          ),
        ),
      )
      .build()
    createBucket(documentImagesBucket)
    createBucket(invoicesBucket)
    uploadFiles()
  }

  private fun createBucket(bucket: String) {
    s3Client.createBucket(
      CreateBucketRequest.builder()
        .bucket(bucket)
        .build(),
    ).join()
  }

  private fun uploadFiles() {
    uploadFile("a1e1a132-82c0-4034-84ef-4dde73df6f08.jpeg")
    uploadFile("driver_license_front.jpg")
    uploadFile("too_small.jpg")
    uploadFile("selfie.jpg")
    uploadFile("driver_license_front.jpg")
    uploadFile("driver_license_back.jpg")
    uploadFile("id_card_front.jpg")
    uploadFile("id_card_back.jpg")
  }

  private fun uploadFile(file: String) {
    val bytes = Resources.getResource("s3/$file").readBytes()
    uploadToS3(file, bytes)
  }

  private fun uploadToS3(file: String, bytes: ByteArray) {
    s3Client.putObject(
      PutObjectRequest.builder()
        .bucket(documentImagesBucket)
        .key("uploads/$file")
        .contentType("image/jpeg")
        .build(),
      AsyncRequestBody.fromBytes(bytes),
    ).join()
  }

  fun uploadRandomFile(key: String) {
    uploadToS3(key, Resources.getResource("s3/driver_license_front.jpg").readBytes())
  }

  fun uploadInvoice(key: String, bytes: ByteArray) {
    s3Client.putObject(
      PutObjectRequest.builder()
        .bucket(invoicesBucket)
        .key(key)
        .contentType("application/pdf")
        .build(),
      AsyncRequestBody.fromBytes(bytes),
    ).join()
  }

  fun deleteInvoice(key: String) {
    s3Client.deleteObject(
      DeleteObjectRequest.builder()
        .bucket(invoicesBucket)
        .key(key)
        .build(),
    ).join()
  }

  fun invoiceSizeOf(invoicePath: String): Long = s3Client.headObject(
    HeadObjectRequest.builder()
      .bucket(invoicesBucket)
      .key(invoicePath)
      .build(),
  ).join().contentLength()

  fun invoiceExists(invoicePath: String): Boolean {
    return try {
      s3Client.headObject(
        HeadObjectRequest.builder()
          .bucket(invoicesBucket)
          .key(invoicePath)
          .build(),
      ).join()
      true
    } catch (ex: CompletionException) {
      ex.cause !is NoSuchKeyException
    }
  }

  fun documentExists(documentId: String): Boolean {
    return try {
      s3Client.headObject(
        HeadObjectRequest.builder()
          .bucket(documentImagesBucket)
          .key("uploads/$documentId.jpeg")
          .build(),
      ).join()
      true
    } catch (ex: CompletionException) {
      ex.cause !is NoSuchKeyException
    }
  }

  fun uploadToS3WithSyncedTag(file: String) {
    val content = Resources.getResource("s3/driver_license_front.jpg").readBytes()
    val efReplicatedTag = Tag.builder().key("ef-replicated").value("true").build()
    val tags = Tagging.builder().tagSet(efReplicatedTag).build()
    s3Client.putObject(
      PutObjectRequest.builder()
        .bucket(documentImagesBucket)
        .key("uploads/$file")
        .contentType("image/jpeg")
        .tagging(tags)
        .build(),
      AsyncRequestBody.fromBytes(content),
    ).join()
  }

  fun port(): Int = container.exposedPorts.first()
}
