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
import shared.models.domain.{EmploymentId, Nino, TaxYear}
import shared.models.errors.{DownstreamErrorCode, DownstreamErrors}
import shared.models.outcomes.ResponseWrapper
import v2.models.request.retrieveAdditionalDividends.RetrieveAdditionalDividendsRequest
import v2.models.response.retrieveAdditionalDividends.RetrieveAdditionalDividendsResponse
import v2.fixtures.RetrieveAdditionalDividendsFixtures._

import scala.concurrent.Future

class RetrieveAdditionalDividendsConnectorSpec extends ConnectorSpec {

  private val nino         = Nino("AA123456A")
  private val employmentId = EmploymentId("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")
  private val taxYear      = TaxYear.fromMtd("2025-26")

  "RetrieveAdditionalDividendsConnector" should {
    "return a valid response" when {
      "a valid request is made" in new HipTest with Test {

        willGet(url = s"$baseUrl/itsd/income-sources/$nino/directorships/$employmentId?taxYear=${taxYear.asTysDownstream}")
          .returns(Future.successful(outcome))

        await(connector.retrieve(request)) shouldBe outcome
      }
    }

    "return an error" when {
      "downstream returns an error" in new HipTest with Test {
        val downstreamErrorResponse: DownstreamErrors =
          DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))
        val errorOutcome = Left(ResponseWrapper(correlationId, downstreamErrorResponse))

        willGet(
          url = s"$baseUrl/itsd/income-sources/$nino/directorships/$employmentId?taxYear=${taxYear.asTysDownstream}"
        )
          .returns(Future.successful(errorOutcome))

        val result: DownstreamOutcome[RetrieveAdditionalDividendsResponse] =
          await(connector.retrieve(request))
        result shouldBe errorOutcome

      }
    }
  }

  trait Test { _: ConnectorTest =>

    val connector: RetrieveAdditionalDividendsConnector =
      new RetrieveAdditionalDividendsConnector(http = mockHttpClient, appConfig = mockSharedAppConfig)

    lazy val request: RetrieveAdditionalDividendsRequest = RetrieveAdditionalDividendsRequest(nino, taxYear, employmentId)
    val outcome: Right[Nothing, ResponseWrapper[RetrieveAdditionalDividendsResponse]] = Right(ResponseWrapper(correlationId, retrieveModel))
  }

}
