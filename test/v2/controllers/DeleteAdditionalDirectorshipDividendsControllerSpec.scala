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

package v2.controllers

import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Result
import shared.config.MockSharedAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.{EmploymentId, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import shared.utils.MockIdGenerator
import v2.mocks.services.MockDeleteAdditionalDirectorshipDividendsService
import v2.mocks.validators.MockDeleteAdditionalDirectorshipDividendsValidatorFactory
import v2.models.request.deleteAdditionalDirectorshipDividends.DeleteAdditionalDirectorshipDividendsRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteAdditionalDirectorshipDividendsControllerSpec
  extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAuditService
    with MockDeleteAdditionalDirectorshipDividendsService
    with MockDeleteAdditionalDirectorshipDividendsValidatorFactory
    with MockIdGenerator
    with MockSharedAppConfig {

  val taxYear: String = "2025-26"
  val employmentId        = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  val parsedEmploymentId: EmploymentId = EmploymentId(employmentId)

  val requestData: DeleteAdditionalDirectorshipDividendsRequest = DeleteAdditionalDirectorshipDividendsRequest(
    nino = parsedNino,
    taxYear = TaxYear.fromMtd(taxYear),
    employmentId = parsedEmploymentId
  )

  "DeleteAdditionalDirectorshipDividendsController" should {
    "return NO_content" when {
      "happy path" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockDeleteAdditionalDirectorshipDividendsService
          .delete(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))



        runOkTestWithAudit(expectedStatus = NO_CONTENT)
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError)
      }

      "service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockDeleteAdditionalDirectorshipDividendsService
          .delete(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller = new DeleteAdditionalDirectorshipDividendsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockDeleteAdditionalDirectorshipDividendsValidatorFactory,
      service = mockDeleteAdditionalDirectorshipDividendsService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.deleteAdditionalDirectorshipDividends(validNino, taxYear, employmentId)(fakeRequest)

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "DeleteAdditionalDirectorshipDividends",
        transactionName = "delete-additional-directorship-dividends",
        detail = GenericAuditDetail(
          userType = "Individual",
          versionNumber = apiVersion.name,
          agentReferenceNumber = None,
          params = Map("nino" -> validNino, "taxYear" -> taxYear, "employmentId" -> employmentId),
          `X-CorrelationId` = correlationId,
          requestBody = None,
          auditResponse = auditResponse
        )
      )

  }

}
