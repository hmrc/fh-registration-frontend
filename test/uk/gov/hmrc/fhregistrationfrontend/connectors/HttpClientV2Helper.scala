/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.connectors

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

trait HttpClientV2Helper extends AsyncWordSpec with Matchers with MockitoSugar with ScalaFutures {

  val mockHttp: HttpClientV2 = mock[HttpClientV2]
  val requestBuilder: RequestBuilder = mock[RequestBuilder]

  val jsonCaptor: ArgumentCaptor[JsValue] = ArgumentCaptor.forClass(classOf[JsValue])

  when(mockHttp.get(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)
  when(mockHttp.post(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)
  when(mockHttp.delete(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)
  when(mockHttp.put(any[URL])(any[HeaderCarrier])).thenReturn(requestBuilder)
  when(requestBuilder.transform(any())).thenReturn(requestBuilder)
  when(requestBuilder.setHeader(any())).thenReturn(requestBuilder)
  when(requestBuilder.withBody(any[JsValue])(any(), any(), any())).thenReturn(requestBuilder)
  when(requestBuilder.withBody(jsonCaptor.capture())(any(), any(), any())).thenReturn(requestBuilder)

  def requestBuilderExecute[A](result: Future[A]): Unit =
    when(requestBuilder.execute[A](any[HttpReads[A]], any[ExecutionContext]))
      .thenReturn(result)
}
