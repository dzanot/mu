/*
 * Copyright 2017-2020 47 Degrees <http://47deg.com>
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

package higherkindness.mu.rpc.internal.client

import io.grpc._
import io.grpc.ClientCall.Listener
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall

class HeaderAttachingClientCall[Req, Res](
    call: ClientCall[Req, Res],
    extraHeaders: Metadata
) extends SimpleForwardingClientCall[Req, Res](call) {

  override def start(responseListener: Listener[Res], headers: Metadata): Unit = {
    headers.merge(extraHeaders)
    super.start(responseListener, headers)
  }

}