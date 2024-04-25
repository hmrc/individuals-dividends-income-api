/*
 * Copyright 2023 HM Revenue & Customs
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

package v1.controllers

import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, FlattenedGenericAuditDetail}
import shared.models.auth.UserDetails
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.libs.json.JsValue
import play.api.mvc.Result
import v1.mocks.services.MockDeleteUkDividendsIncomeAnnualSummaryService
import v1.mocks.validators.MockDeleteUkDividendsIncomeAnnualSummaryValidatorFactory
import v1.models.request.deleteUkDividendsIncomeAnnualSummary.DeleteUkDividendsIncomeAnnualSummaryRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteUkDividendsIncomeAnnualSummaryControllerSpec
  extends ControllerBaseSpec
    with ControllerTestRunner
    with MockDeleteUkDividendsIncomeAnnualSummaryService
    with MockDeleteUkDividendsIncomeAnnualSummaryValidatorFactory
    with MockAppConfig{

  private val taxYear = "2017-18"
  private val mtdId   = "test-mtd-id"

  private val requestData: DeleteUkDividendsIncomeAnnualSummaryRequest = DeleteUkDividendsIncomeAnnualSummaryRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  "DeleteDividendsController" should {
    "return a successful response with status 204 (No Content)" when {
      "a valid request is supplied" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockDeleteUkDividendsIncomeAnnualSummaryService
          .delete(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTest(expectedStatus = NO_CONTENT)
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockDeleteUkDividendsIncomeAnnualSummaryService
          .delete(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTest(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[FlattenedGenericAuditDetail] {

    val controller = new DeleteUkDividendsIncomeAnnualSummaryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockDeleteDividendsIncomeAnnualSummaryValidatorFactory,
      service = mockDeleteUkDividendsIncomeAnnualSummaryService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.deleteUkDividends(nino, taxYear)(fakeDeleteRequest)

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[FlattenedGenericAuditDetail] =
      AuditEvent(
        auditType = "DeleteUkDividendsIncome",
        transactionName = "delete-uk-dividends-income",
        detail = FlattenedGenericAuditDetail(
          versionNumber = Some("1.0"),
          userDetails = UserDetails(mtdId, "Individual", None),
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          request = None,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}

