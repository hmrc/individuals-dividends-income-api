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

import common.errors.RuleOutsideAmendmentWindowError
import shared.controllers.EndpointLogContext
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import v2.mocks.connectors.MockCreateAmendUkDividendsAnnualSummaryConnector
import v2.models.request.createAmendUkDividendsIncomeAnnualSummary.{
  CreateAmendUkDividendsIncomeAnnualSummaryBody,
  CreateAmendUkDividendsIncomeAnnualSummaryRequest
}

import scala.concurrent.Future

class CreateAmendUkDividendsAnnualSummaryServiceSpec extends ServiceSpec {

  private val request = CreateAmendUkDividendsIncomeAnnualSummaryRequest(
    nino = Nino("AA112233A"),
    taxYear = TaxYear.fromMtd("2023-24"),
    body = CreateAmendUkDividendsIncomeAnnualSummaryBody(None, None)
  )

  "CreateAmendAmendUkDividendsAnnualSummaryService" when {
    "the downstream request is successful" must {
      "return a success result" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockCreateAmendUkDividendsAnnualSummaryConnector
          .createOrAmendAnnualSummary(request)
          .returns(Future.successful(outcome))

        await(service.createAmendUkDividends(request)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit = {

          s"downstream returns $downstreamErrorCode" in new Test {
            MockCreateAmendUkDividendsAnnualSummaryConnector
              .createOrAmendAnnualSummary(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            val result: Either[ErrorWrapper, ResponseWrapper[Unit]] = await(service.createAmendUkDividends(request))
            result shouldBe Left(ErrorWrapper(correlationId, error))
          }
        }

        val errors = List(
          ("INVALID_NINO", NinoFormatError),
          ("INVALID_TAXYEAR", TaxYearFormatError), // remove once DES to IFS migration complete
          ("INVALID_TYPE", InternalError),
          ("INVALID_PAYLOAD", InternalError),
          ("NOT_FOUND_INCOME_SOURCE", NotFoundError),
          ("MISSING_CHARITIES_NAME_GIFT_AID", InternalError),
          ("MISSING_GIFT_AID_AMOUNT", InternalError),
          ("MISSING_CHARITIES_NAME_INVESTMENT", InternalError),
          ("MISSING_INVESTMENT_AMOUNT", InternalError),
          ("INVALID_ACCOUNTING_PERIOD", RuleTaxYearNotSupportedError),
          ("GONE", InternalError),
          ("NOT_FOUND", NotFoundError),
          ("SERVICE_UNAVAILABLE", InternalError),
          ("SERVER_ERROR", InternalError)
        )

        val extraTysErrors = List(
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_INCOMESOURCE_TYPE", InternalError),
          ("TAX_YEAR_NOT_SUPPORTED", RuleTaxYearNotSupportedError),
          ("OUTSIDE_AMENDMENT_WINDOW", RuleOutsideAmendmentWindowError),
          ("INCOME_SOURCE_NOT_FOUND", NotFoundError),
          ("INCOMPATIBLE_INCOME_SOURCE", InternalError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }

  trait Test extends MockCreateAmendUkDividendsAnnualSummaryConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: CreateAmendUkDividendsAnnualSummaryService =
      new CreateAmendUkDividendsAnnualSummaryService(mockAmendUkDividendsConnector)

  }

}
