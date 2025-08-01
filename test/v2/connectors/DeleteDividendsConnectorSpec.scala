/*
 * Copyright 2025 HM Revenue & Customs
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

package v2.connectors

import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import v2.models.request.deleteDividends.DeleteDividendsRequest
import uk.gov.hmrc.http.StringContextOps

import scala.concurrent.Future

class DeleteDividendsConnectorSpec extends ConnectorSpec {

  private val nino: String = "AA123456A"

  "DeleteDividendsConnector" should {
    "return a 200 result on delete for a non-TYS request" when {
      "the downstream call is successful and not tax year specific" in new IfsTest with Test {
        def taxYear: TaxYear                               = TaxYear.fromMtd("2021-22")
        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        willDelete(url"$baseUrl/income-tax/income/dividends/$nino/${taxYear.asMtd}") returns Future.successful(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.delete(request))
        result shouldBe outcome
      }
    }

    "return a 200 result on delete for a TYS request" when {
      "the downstream call is successful and tax year specific" in new IfsTest with Test {
        def taxYear: TaxYear                               = TaxYear.fromMtd("2023-24")
        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        willDelete(url"$baseUrl/income-tax/income/dividends/23-24/$nino") returns Future.successful(outcome)

        val result: DownstreamOutcome[Unit] = await(connector.delete(request))
        result shouldBe outcome
      }
    }
  }

  trait Test {
    _: ConnectorTest =>

    def taxYear: TaxYear

    protected val connector: DeleteDividendsConnector =
      new DeleteDividendsConnector(
        http = mockHttpClient,
        appConfig = mockSharedAppConfig
      )

    protected val request: DeleteDividendsRequest =
      DeleteDividendsRequest(
        nino = Nino(nino),
        taxYear = taxYear
      )

  }

}
