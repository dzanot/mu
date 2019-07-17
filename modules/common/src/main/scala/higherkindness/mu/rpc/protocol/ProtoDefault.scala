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

package higherkindness.mu.rpc.protocol

import cats.Monoid
import alleycats.{Empty => CEmpty}

trait ProtoDefault[A] {
  def default: A
}

object ProtoDefault {
  def apply[A](implicit P: ProtoDefault[A]): ProtoDefault[A] = P

  implicit def protoDefaultFromMonoid[A](implicit M: Monoid[A]): ProtoDefault[A] =
    new ProtoDefault[A] { def default: A = M.empty }

  implicit def protoDefaultFromEmpty[A](implicit E: CEmpty[A]): ProtoDefault[A] =
    new ProtoDefault[A] { def default: A = E.empty }

  implicit val booleanEmpty: CEmpty[Boolean] =
    new CEmpty[Boolean] { override def empty: Boolean = false }

  implicit val protoDefaultBoolean: ProtoDefault[Boolean] = new ProtoDefault[Boolean] {
    def default: Boolean = false
  }

  implicit val protoDefaultBigDecimal: ProtoDefault[BigDecimal] = new ProtoDefault[BigDecimal] {
    def default: BigDecimal = BigDecimal(0)
  }

  implicit val protoDefaultMuEmpty: ProtoDefault[Empty.type] = new ProtoDefault[Empty.type] {
    override def default: Empty.type = Empty
  }
}
