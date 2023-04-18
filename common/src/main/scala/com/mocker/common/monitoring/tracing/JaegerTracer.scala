package com.mocker.common.monitoring.tracing

import com.mocker.common.utils.ServerAddress
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes
import zio._

object JaegerTracer {

  def live(serviceName: String, jaegerServerAddress: ServerAddress): ZLayer[Any, Throwable, Tracer] =
    ZLayer.fromZIO {
      for {
        spanExporter <- ZIO.attempt(
          JaegerGrpcSpanExporter
            .builder()
            .setEndpoint(s"http://$jaegerServerAddress")
            .build()
        )
        spanProcessor <- ZIO.succeed(SimpleSpanProcessor.create(spanExporter))
        tracerProvider <- ZIO
          .attempt(
            SdkTracerProvider
              .builder()
              .setResource(Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, serviceName)))
              .addSpanProcessor(spanProcessor)
              .build()
          )
        openTelemetry <- ZIO.succeed(OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build())
        tracer <- ZIO.succeed(openTelemetry.getTracer("com.mocker"))
      } yield tracer
    }

}
