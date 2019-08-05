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

package higherkindness.mu.rpc.internal.encoders

import cats.instances._
import java.io.{ByteArrayInputStream, InputStream}
import java.time.{Instant, LocalDate, LocalDateTime}

import cats.instances.{
  ListInstances,
  ListInstancesBinCompat0,
  OptionInstances,
  OptionInstancesBinCompat0
}
import com.google.protobuf.{CodedInputStream, CodedOutputStream}
import higherkindness.mu.rpc.internal.util.{BigDecimalUtil, EncoderUtil, JavaTimeUtil}
import io.grpc.MethodDescriptor.Marshaller
import io.protoless.messages.{Decoder, Encoder}
import io.protoless._
import io.protoless.fields.{FieldDecoder, FieldEncoder}
import io.protoless.messages.Decoder.Result

object protoless
    extends OptionInstances
    with OptionInstancesBinCompat0
    with ListInstances
    with ListInstancesBinCompat0 {
  implicit def defaultProtolessMarshallers[A: Decoder: Encoder]: Marshaller[A] =
    new Marshaller[A] {
      override def stream(value: A): InputStream =
        new ByteArrayInputStream(Encoder[A].encodeAsBytes(value))

      override def parse(stream: InputStream): A =
        Decoder[A].decode(stream).fold(fa => throw fa, identity)
    }

  implicit object BooleanDecoder extends Decoder[Boolean] {
    override def decode(input: CodedInputStream): Result[Boolean] =
      Right(input.readBool())
  }
  implicit object BooleanEncoder extends Encoder[Boolean] {
    override def encode(value: Boolean, output: CodedOutputStream): Unit =
      //output.writeBoolNoTag(value)
      throw new com.google.protobuf.InvalidProtocolBufferException(
        "RPC argument must be a message type")
  }

  object bigDecimal {
    implicit object BigDecimalEncoder extends Encoder[BigDecimal] {
      override def encode(value: BigDecimal, output: CodedOutputStream): Unit =
        output.writeByteArrayNoTag(BigDecimalUtil.bigDecimalToByte(value))
    }
    implicit object BigDecimalDecoder extends Decoder[BigDecimal] {
      override def decode(input: CodedInputStream): Result[BigDecimal] =
        Right(BigDecimalUtil.byteToBigDecimal(input.readByteArray()))
    }
    object fields {
      implicit val bigDecimalDecoder: FieldDecoder[BigDecimal] = FieldDecoder[BigDecimal]
      implicit val bigDecimalEncoder: FieldEncoder[BigDecimal] = FieldEncoder[BigDecimal]
    }
  }

  object javatime {
    object fields {
      implicit val localDateEncoder: FieldEncoder[LocalDate] =
        FieldEncoder[Int].contramap(JavaTimeUtil.localDateToInt)
      implicit val localDateDecoder: FieldDecoder[LocalDate] =
        FieldDecoder[Int].map(JavaTimeUtil.intToLocalDate)

      implicit val localDateTimeEncoder: FieldEncoder[LocalDateTime] =
        FieldEncoder[Long].contramap(JavaTimeUtil.localDateTimeToLong)
      implicit val localDateTimeDecoder: FieldDecoder[LocalDateTime] =
        FieldDecoder[Long].map(JavaTimeUtil.longToLocalDateTime)

      implicit val instantEncoder: FieldEncoder[Instant] =
        FieldEncoder[Long].contramap(JavaTimeUtil.instantToLong)
      implicit val instantDecoder: FieldDecoder[Instant] =
        FieldDecoder[Long].map(JavaTimeUtil.longToInstant)
    }

    implicit object LocalDateEncoder extends Encoder[LocalDate] {
      override def encode(value: LocalDate, output: CodedOutputStream): Unit =
        output.writeByteArrayNoTag(EncoderUtil.intToByteArray(JavaTimeUtil.localDateToInt(value)))
    }

    implicit object LocalDateDecoder extends Decoder[LocalDate] {
      override def decode(input: CodedInputStream): Result[LocalDate] =
        Right(JavaTimeUtil.intToLocalDate(EncoderUtil.byteArrayToInt(input.readByteArray())))
    }

    implicit object LocalDateTimeEncoder extends Encoder[LocalDateTime] {
      override def encode(value: LocalDateTime, output: CodedOutputStream): Unit =
        output.writeByteArrayNoTag(
          EncoderUtil.longToByteArray(JavaTimeUtil.localDateTimeToLong(value)))
    }

    implicit object LocalDateTimeDecoder extends Decoder[LocalDateTime] {
      override def decode(input: CodedInputStream): Result[LocalDateTime] =
        Right(JavaTimeUtil.longToLocalDateTime(EncoderUtil.byteArrayToLong(input.readByteArray())))
    }

    implicit object InstantEncoder extends Encoder[Instant] {
      override def encode(value: Instant, output: CodedOutputStream): Unit =
        output.writeByteArrayNoTag(EncoderUtil.longToByteArray(JavaTimeUtil.instantToLong(value)))
    }

    implicit object InstantDecoder extends Decoder[Instant] {
      override def decode(input: CodedInputStream): Result[Instant] =
        Right(JavaTimeUtil.longToInstant(EncoderUtil.byteArrayToLong(input.readByteArray())))
    }
  }
}
