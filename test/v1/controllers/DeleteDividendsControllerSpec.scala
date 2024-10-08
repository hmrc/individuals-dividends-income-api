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

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.mocks.MockIdGenerator
import api.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import config.MockAppConfig
import play.api.libs.json.JsValue
import play.api.mvc.Result
import play.api.Configuration
import v1.mocks.requestParsers.MockDeleteDividendsRequestParser
import v1.mocks.services.MockDeleteDividendsService
import v1.models.request.deleteDividends.{DeleteDividendsRawData, DeleteDividendsRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteDividendsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockAuditService
    with MockDeleteDividendsService
    with MockDeleteDividendsRequestParser
    with MockIdGenerator
    with MockAppConfig {

  val taxYear: String = "2019-20"

  val rawData: DeleteDividendsRawData = DeleteDividendsRawData(
    nino = nino,
    taxYear = taxYear
  )

  val requestData: DeleteDividendsRequest = DeleteDividendsRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  "DeleteDividendsController" should {
    "return NO_content" when {
      "happy path" in new Test {
        MockDeleteDividendsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeleteDividendsService
          .delete(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTestWithAudit(expectedStatus = NO_CONTENT)
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockDeleteDividendsRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError)))

        runErrorTestWithAudit(NinoFormatError)
      }

      "service returns an error" in new Test {
        MockDeleteDividendsRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeleteDividendsService
          .delete(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))))

        runErrorTestWithAudit(RuleTaxYearNotSupportedError)
      }
    }
  }

  trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller = new DeleteDividendsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockDeleteDividendsRequestParser,
      service = mockDeleteDividendsService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitches.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.deleteDividends(nino, taxYear)(fakeDeleteRequest)

    def event(auditResponse: AuditResponse, requestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "DeleteDividendsIncome",
        transactionName = "delete-dividends-income",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          request = None,
          `X-CorrelationId` = correlationId,
          response = auditResponse
        )
      )

  }

}
