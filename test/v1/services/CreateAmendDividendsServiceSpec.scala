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

package v1.services

import shared.controllers.EndpointLogContext
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import v1.mocks.connectors.MockCreateAmendDividendsConnector
import v1.models.request.createAmendDividends.{CreateAmendDividendsRequest, CreateAmendDividendsRequestBody}

import scala.concurrent.Future

class CreateAmendDividendsServiceSpec extends ServiceSpec {

  private val nino    = "AA112233A"
  private val taxYear = TaxYear.fromMtd("2019-20")

  val createAmendDividendsRequest: CreateAmendDividendsRequest = CreateAmendDividendsRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    body = CreateAmendDividendsRequestBody(None, None, None, None, None, None)
  )

  trait Test extends MockCreateAmendDividendsConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: CreateAmendDividendsService = new CreateAmendDividendsService(
      connector = mockCreateAmendDividendsConnector
    )

  }

  "CreateAmendDividendsService" when {
    "CreateAmendDividends" must {
      "return correct result for a success" in new Test {
        private val outcome = Right(ResponseWrapper(correlationId, ()))

        MockCreateAmendDividendsConnector
          .createAmendDividends(createAmendDividendsRequest)
          .returns(Future.successful(outcome))

        await(service.createAmendDividends(createAmendDividendsRequest)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(errorCode: String, error: MtdError): Unit =
          s"a $errorCode error is returned from the service" in new Test {

            MockCreateAmendDividendsConnector
              .createAmendDividends(createAmendDividendsRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(errorCode))))))

            await(service.createAmendDividends(createAmendDividendsRequest)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors = List(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_CORRELATIONID", InternalError),
          ("INVALID_PAYLOAD", InternalError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )

        val extraTysErrors = List(
          ("INVALID_CORRELATION_ID", InternalError),
          ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
