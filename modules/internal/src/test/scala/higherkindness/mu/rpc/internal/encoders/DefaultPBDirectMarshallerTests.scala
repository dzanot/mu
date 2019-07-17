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

import java.io.{ByteArrayInputStream, InputStream}

import cats.implicits._
import cats.Monoid
import higherkindness.mu.rpc.internal.encoders.pbd._
import higherkindness.mu.rpc.protocol.ProtoDefault
import io.grpc.MethodDescriptor.Marshaller
import org.scalatest._

class DefaultPBDirectMarshallerTests extends WordSpec with Matchers {

  case class MyTestDefault(s: String)

  case class NestedThing(lt: ListThing, i: Int)
  case class ListThing(l: List[Int], o: Option[String])


  private val expectedString = "12345"

  private val emptyInputStream: InputStream = new ByteArrayInputStream(Array())

  private def testDefault[A](expectedDefault: A)(implicit M: Marshaller[A]) =
    M.parse(emptyInputStream) shouldBe expectedDefault

  "Default pbd marshaller" should {

    "handle empty streams by filling in the default value" in {
      import cats.derived.auto.monoid._

      case class FloatMessage(v: Float)
      testDefault[FloatMessage](FloatMessage(0.0f))

      case class DoubleMessage(v: Double)
      testDefault[DoubleMessage](DoubleMessage(0.0))

//      implicit val monoidBoolean: Monoid[Boolean] = new Monoid[Boolean] {
//        override def empty: Boolean = false
//
//        override def combine(x: Boolean, y: Boolean): Boolean = ???
//      }
//
//      case class BooleanMessage(v: Boolean)
//      testDefault[BooleanMessage](BooleanMessage(false))

      case class IntMessage(v: Int)
      testDefault[IntMessage](IntMessage(0))

      case class LongMessage(v: Long)
      testDefault[LongMessage](LongMessage(0))

      case class StringMessage(v: String)
      testDefault[StringMessage](StringMessage(""))
    }

    "handle empty streams by filling in the default value when a ProtoDefault typeclass exists" in {
      implicit val protoDefault: ProtoDefault[MyTestDefault] = new ProtoDefault[MyTestDefault] {
        override def default: MyTestDefault = MyTestDefault(expectedString)
      }

      testDefault[MyTestDefault](MyTestDefault(expectedString))
    }

    "handle empty streams by filling in the default value when a Monoid typeclass exists" in {
      implicit val monoid: Monoid[MyTestDefault] = new Monoid[MyTestDefault] {
        override def empty: MyTestDefault = MyTestDefault(expectedString)

        override def combine(x: MyTestDefault, y: MyTestDefault): MyTestDefault =
          ??? // explicit as it should never be called
      }

      testDefault[MyTestDefault](MyTestDefault(expectedString))
    }

    "handle empty streams by filling in the default when an automatically derived Monoid typeclass exists" in {
      import cats.derived.auto.monoid._

      testDefault[MyTestDefault](MyTestDefault(""))
    }

    "handle defaults for more complex data with Monoid Instance" in {
      implicit val monoid: Monoid[NestedThing] = new Monoid[NestedThing] {
        override def empty: NestedThing = NestedThing(ListThing(List(), None), 0)

        override def combine(x: NestedThing, y: NestedThing): NestedThing =
          ??? // explicit as it should never be called
      }

      testDefault[NestedThing](NestedThing(ListThing(List(), None), 0))
    }

    "handle automatically derived typeclasses for more complex data" in {
      import cats.implicits._ //this is already imported above, but it wont compile without this here.....
      import cats.derived.auto.monoid._

      testDefault[NestedThing](NestedThing(ListThing(List(), None), 0))
    }
  }
}
