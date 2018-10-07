package $organization$.util.logging

import java.util.UUID

import $organization$.test.BaseSpec

class TraceIdSpec extends BaseSpec {

  "TraceId" should {

    "TraceId" should {

      "generate a value uuid by default" in {
        val traceId = TraceId()

        noException shouldBe thrownBy(UUID.fromString(traceId.value))
      }

      "generate a correct sub id" in {
        val traceId = TraceId()

        val expectedTraceId = traceId.value + "#" + 1

        traceId.subId(1).value shouldBe expectedTraceId
      }

    }

  }

}