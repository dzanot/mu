/*
 * Copyright 2017-2019 47 Degrees, LLC. <http://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package higherkindness.mu.rpc
package internal.encoders

import java.io.{ByteArrayInputStream, InputStream}
import java.time.{Instant, LocalDate, LocalDateTime}

import cats.data.NonEmptyList
import cats.instances._
import com.google.protobuf.{CodedInputStream, CodedOutputStream}
import higherkindness.mu.rpc.internal.util.{BigDecimalUtil, EncoderUtil, JavaTimeUtil}
import io.grpc.MethodDescriptor.Marshaller
import pbdirect.LowPriorityPBWriterImplicits.SizeWithoutTag

object pbd
    extends OptionInstances
    with OptionInstancesBinCompat0
    with ListInstances
    with ListInstancesBinCompat0 {

  import pbdirect._

  implicit def defaultDirectPBMarshallers[A: PBWriter: PBReader]: Marshaller[A] =
    new Marshaller[A] {

      override def parse(stream: InputStream): A =
        Iterator.continually(stream.read).takeWhile(_ != -1).map(_.toByte).toArray.pbTo[A]

      override def stream(value: A): InputStream = new ByteArrayInputStream(value.toPB)

    }

  object bigDecimal {

    implicit object BigDecimalWriter extends PBWriter[BigDecimal] {
      override def writeTo(
          index: NonEmptyList[Int],
          value: BigDecimal,
          out: CodedOutputStream,
          sizes: SizeWithoutTag): Unit =
        out.writeByteArray(index.head, BigDecimalUtil.bigDecimalToByte(value))

      override def writtenBytesSize(
          index: NonEmptyList[Int],
          value: BigDecimal,
          sizes: SizeWithoutTag): Int =
        CodedOutputStream.computeByteArraySize(index.head, BigDecimalUtil.bigDecimalToByte(value))

    }

    implicit object BigDecimalReader extends PBReader[BigDecimal] {
      override def read(input: CodedInputStream): BigDecimal =
        BigDecimalUtil.byteToBigDecimal(input.readByteArray())
    }
  }

  object javatime {

    implicit object LocalDateWriter extends PBWriter[LocalDate] {
      override def writeTo(
          index: NonEmptyList[Int],
          value: LocalDate,
          out: CodedOutputStream,
          sizes: SizeWithoutTag): Unit =
        out.writeByteArray(
          index.head,
          EncoderUtil.intToByteArray(JavaTimeUtil.localDateToInt(value)))

      override def writtenBytesSize(
          index: NonEmptyList[Int],
          value: LocalDate,
          sizes: SizeWithoutTag): Int =
        CodedOutputStream.computeByteArraySize(
          index.head,
          EncoderUtil.intToByteArray(JavaTimeUtil.localDateToInt(value)))
    }

    implicit object LocalDateReader extends PBReader[LocalDate] {
      override def read(input: CodedInputStream): LocalDate =
        JavaTimeUtil.intToLocalDate(EncoderUtil.byteArrayToInt(input.readByteArray()))
    }

    implicit object LocalDateTimeWriter extends PBWriter[LocalDateTime] {
      override def writeTo(
          index: NonEmptyList[Int],
          value: LocalDateTime,
          out: CodedOutputStream,
          sizes: SizeWithoutTag): Unit =
        out.writeByteArray(
          index.head,
          EncoderUtil.longToByteArray(JavaTimeUtil.localDateTimeToLong(value)))

      override def writtenBytesSize(
          index: NonEmptyList[Int],
          value: LocalDateTime,
          sizes: SizeWithoutTag): Int =
        CodedOutputStream.computeByteArraySize(
          index.head,
          EncoderUtil.longToByteArray(JavaTimeUtil.localDateTimeToLong(value)))
    }

    implicit object LocalDateTimeReader extends PBReader[LocalDateTime] {
      override def read(input: CodedInputStream): LocalDateTime =
        JavaTimeUtil.longToLocalDateTime(EncoderUtil.byteArrayToLong(input.readByteArray()))
    }

    implicit object InstantWriter extends PBWriter[Instant] {
      override def writeTo(
          index: NonEmptyList[Int],
          value: Instant,
          out: CodedOutputStream,
          sizes: SizeWithoutTag): Unit =
        out.writeByteArray(
          index.head,
          EncoderUtil.longToByteArray(JavaTimeUtil.instantToLong(value)))

      override def writtenBytesSize(
          index: NonEmptyList[Int],
          value: Instant,
          sizes: SizeWithoutTag): Int =
        CodedOutputStream.computeByteArraySize(
          index.head,
          EncoderUtil.longToByteArray(JavaTimeUtil.instantToLong(value)))
    }

    implicit object InstantReader extends PBReader[Instant] {
      override def read(input: CodedInputStream): Instant =
        JavaTimeUtil.longToInstant(EncoderUtil.byteArrayToLong(input.readByteArray()))
    }

  }
}
