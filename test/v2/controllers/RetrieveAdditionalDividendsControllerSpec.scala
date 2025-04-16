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
import play.api.mvc.Result
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.domain.{EmploymentId, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import v2.fixtures.RetrieveAdditionalDividendsFixtures._
import v2.mocks.services.MockRetrieveAdditionalDividendsService
import v2.mocks.validators.MockRetrieveAdditionalDividendsValidatorFactory
import v2.models.request.retrieveAdditionalDividends.RetrieveAdditionalDividendsRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveAdditionalDividendsControllerSpec extends ControllerBaseSpec with ControllerTestRunner {
  private val taxYear: String = "2025-26"
  private val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

  private val requestData: RetrieveAdditionalDividendsRequest = RetrieveAdditionalDividendsRequest(
    nino = parsedNino,
    taxYear = TaxYear.fromMtd(taxYear),
    employmentId = EmploymentId(employmentId)
  )

  "RetrieveAdditionalDividendsController" should {
    "return a successful response with status 200 (OK)" when {
      "given a valid request" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveAdditionalDividendsService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveModel))))

        runOkTest(
          expectedStatus = OK,
          maybeExpectedResponseBody = Some(mtdResponse)
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveAdditionalDividendsService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, EmploymentIdFormatError))))

        runErrorTest(EmploymentIdFormatError)
      }
    }
  }

  trait Test extends ControllerTest with MockRetrieveAdditionalDividendsService with MockRetrieveAdditionalDividendsValidatorFactory {

    val controller = new RetrieveAdditionalDividendsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveAdditionalDividendsValidatorFactory,
      service = mockRetrieveAdditionalDividendsService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    override protected def callController(): Future[Result] = controller.retrieve(validNino, taxYear, employmentId)(fakeGetRequest)

  }


}
