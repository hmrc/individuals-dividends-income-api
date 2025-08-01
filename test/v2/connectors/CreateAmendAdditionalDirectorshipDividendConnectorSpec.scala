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
import v2.fixtures.CreateAmendAdditionalDirectorshipDividendFixtures.minimumRequestBodyModel
import v2.models.request.createAmendAdditionalDirectorshipDividend._
import uk.gov.hmrc.http.StringContextOps

import scala.concurrent.Future

class CreateAmendAdditionalDirectorshipDividendConnectorSpec extends ConnectorSpec {

  private val nino: Nino                 = Nino("AA123456A")
  private val taxYear: TaxYear           = TaxYear.fromMtd("2025-26")
  private val employmentId: EmploymentId = EmploymentId("4557ecb5-fd32-48cc-81f5-e6acd1099f3c")

  "CreateAmendAdditionalDirectorshipDividendConnector" should {
    "return a 204 (NO_CONTENT) status for a success scenario" when {
      "the downstream call is successful" in new HipTest with Test {
        val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

        willPut(
          url = url"$baseUrl/itsd/income-sources/$nino/directorships/$employmentId?taxYear=${taxYear.asTysDownstream}",
          body = minimumRequestBodyModel
        ).returns(Future.successful(outcome))

        val result: DownstreamOutcome[Unit] = await(connector.createAmend(request))

        result shouldBe outcome
      }
    }

    "return an error" when {
      "downstream returns an error" in new HipTest with Test {
        val downstreamErrorResponse: DownstreamErrors = DownstreamErrors.single(DownstreamErrorCode("SOME_ERROR"))
        val errorOutcome: Left[ResponseWrapper[DownstreamErrors], Nothing] =
          Left(ResponseWrapper(correlationId, downstreamErrorResponse))

        willPut(
          url = url"$baseUrl/itsd/income-sources/$nino/directorships/$employmentId?taxYear=${taxYear.asTysDownstream}",
          body = minimumRequestBodyModel
        ).returns(Future.successful(errorOutcome))

        val result: DownstreamOutcome[Unit] = await(connector.createAmend(request))

        result shouldBe errorOutcome
      }
    }
  }

  private trait Test { _: ConnectorTest =>

    protected val connector: CreateAmendAdditionalDirectorshipDividendConnector =
      new CreateAmendAdditionalDirectorshipDividendConnector(http = mockHttpClient, appConfig = mockSharedAppConfig)

    protected val request: CreateAmendAdditionalDirectorshipDividendRequest = CreateAmendAdditionalDirectorshipDividendRequest(
      nino = nino,
      taxYear = taxYear,
      employmentId = employmentId,
      body = minimumRequestBodyModel
    )
  }

}
