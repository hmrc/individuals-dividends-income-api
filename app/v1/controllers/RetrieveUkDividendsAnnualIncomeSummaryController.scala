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

import config.AppConfig
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import shared.controllers.{AuthorisedController, EndpointLogContext, RequestContext, RequestHandler}
import shared.services.{EnrolmentsAuthService, MtdIdLookupService}
import shared.utils.IdGenerator
import v1.controllers.validators.RetrieveUkDividendsIncomeAnnualSummaryValidatorFactory
import v1.services.RetrieveUkDividendsIncomeAnnualSummaryService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RetrieveUkDividendsAnnualIncomeSummaryController @Inject() (val authService: EnrolmentsAuthService,
                                                                  val lookupService: MtdIdLookupService,
                                                                  validatorFactory: RetrieveUkDividendsIncomeAnnualSummaryValidatorFactory,
                                                                  service: RetrieveUkDividendsIncomeAnnualSummaryService,
                                                                  cc: ControllerComponents,
                                                                  val idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "RetrieveUkDividendsAnnualIncomeSummaryController",
      endpointName = "retrieveUkDividendsAnnualIncomeSummary"
    )

  def retrieveUkDividends(nino: String, taxYear: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, taxYear)
      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.retrieveUKDividendsIncomeAnnualSummary)
        .withPlainJsonResult()

      requestHandler.handleRequest()
    }

}
