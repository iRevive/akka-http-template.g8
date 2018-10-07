package $organization$.util.logging

import java.util.UUID

import com.typesafe.scalalogging.CanLog

case class TraceId(value: String = UUID.randomUUID().toString) {

  def subId(id: String): TraceId = copy(value + "#" + id)

  def subId(id: Int): TraceId = subId(id.toString)

}

object TraceId {

  implicit object CanLogTraceId extends CanLog[TraceId] {
    override def logMessage(originalMsg: String, a: TraceId): String = s"[\${a.value}] \$originalMsg"
  }

}