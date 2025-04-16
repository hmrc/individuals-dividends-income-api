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

package v2.services

import shared.models.domain._
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import v2.fixtures.RetrieveAdditionalDividendsFixtures._
import v2.mocks.connectors.MockRetrieveAdditionalDividendsConnector
import v2.models.request.retrieveAdditionalDividends.RetrieveAdditionalDividendsRequest

import scala.concurrent.Future

class RetrieveAdditionalDividendsServiceSpec extends ServiceSpec {

  "RetrieveAdditionalDividendsService" should {
    "return the expected response for a request" when {
      "a valid request is made" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, retrieveModel))

        MockRetrieveAdditionalDividendsConnector
          .retrieve(request)
          .returns(Future.successful(outcome))

        await(service.retrieve(request)) shouldBe outcome
      }

      "an error is mapped according to the spec" when {
        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockRetrieveAdditionalDividendsConnector
              .retrieve(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.retrieve(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = List(
          ("1215", NinoFormatError),
          ("1117", TaxYearFormatError),
          ("1217", EmploymentIdFormatError),
          ("1216", InternalError),
          ("5010", NotFoundError)
        )

        errors.foreach(args => (serviceError _).tupled(args))
      }
    }

  }

  trait Test extends MockRetrieveAdditionalDividendsConnector {

    private val nino    = "AA112233A"
    private val taxYear = "2025-26"
    private val employmentId = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    val request: RetrieveAdditionalDividendsRequest = RetrieveAdditionalDividendsRequest(
      nino = Nino(nino),
      taxYear = TaxYear.fromMtd(taxYear),
      employmentId = EmploymentId(employmentId)
    )

    val service: RetrieveAdditionalDividendsService = new RetrieveAdditionalDividendsService(
      connector = mockRetrieveAdditionalDividendsConnector
    )

  }
}
