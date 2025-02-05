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

import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import shared.config.SharedAppConfig
import shared.controllers._
import shared.routing.Version
import shared.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import shared.utils.IdGenerator
import v2.services.CreateAmendUkDividendsAnnualSummaryService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CreateAmendUkDividendsAnnualSummaryController @Inject() (val authService: EnrolmentsAuthService,
                                                               val lookupService: MtdIdLookupService,
                                                               validatorFactory: CreateAmendUkDividendsIncomeAnnualSummaryValidatorFactory,
                                                               service: CreateAmendUkDividendsAnnualSummaryService,
                                                               auditService: AuditService,
                                                               cc: ControllerComponents,
                                                               val idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: SharedAppConfig)
    extends AuthorisedController(cc) {

  val endpointName: String = "create-amend-uk-dividends-annual-summary"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "CreateAmendUkDividendsAnnualSummaryController",
      endpointName = "createAmendUkDividendsAnnualSummary"
    )

  def createAmendUkDividendsAnnualSummary(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, taxYear, request.body)

      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.createAmendUkDividends)
        .withAuditing(
          AuditHandler(
            auditService = auditService,
            auditType = "CreateAndAmendUkDividendsIncome",
            transactionName = "create-amend-uk-dividends-income",
            apiVersion = Version(request),
            params = Map("nino" -> nino, "taxYear" -> taxYear),
            requestBody = Some(request.body),
            includeResponse = true
          )
        )
        .withNoContentResult(successStatus = OK)

      requestHandler.handleRequest()
    }

}
