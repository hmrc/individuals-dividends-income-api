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
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import shared.controllers._
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.auth.UserDetails
import shared.models.errors._
import shared.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import shared.utils.IdGenerator
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import v1.controllers.validators.CreateAmendDividendsValidatorFactory
import v1.services.CreateAmendDividendsService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendDividendsController @Inject() (val authService: EnrolmentsAuthService,
                                                val lookupService: MtdIdLookupService,
                                                validatorFactory: CreateAmendDividendsValidatorFactory,
                                                service: CreateAmendDividendsService,
                                                auditService: AuditService,
                                                cc: ControllerComponents,
                                                val idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "CreateAmendDividendsController",
      endpointName = "createAmendDividends"
    )

  def createAmendDividends(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, taxYear, request.body)
      val requestHandler = RequestHandler
        .withValidator(validator)
        .withService(service.createAmendDividends)
        .withAuditing(auditHandler(nino, taxYear, request))
        .withNoContentResult(successStatus = OK)

      requestHandler.handleRequest()
    }

  private def auditHandler(nino: String, taxYear: String, request: UserRequest[JsValue]): AuditHandler = {
    new AuditHandler() {
      override def performAudit(userDetails: UserDetails, httpStatus: Int, response: Either[ErrorWrapper, Option[JsValue]])(implicit
          ctx: RequestContext,
          ec: ExecutionContext): Unit = {

        response match {
          case Left(err: ErrorWrapper) =>
            auditSubmission(
              GenericAuditDetail(
                request.userDetails,
                Map("nino" -> nino, "taxYear" -> taxYear),
                Some(request.body),
                ctx.correlationId,
                AuditResponse(httpStatus = httpStatus, response = Left(err.auditErrors))
              )
            )

          case Right(_: Option[JsValue]) =>
            auditSubmission(
              GenericAuditDetail(
                request.userDetails,
                Map("nino" -> nino, "taxYear" -> taxYear),
                Some(request.body),
                ctx.correlationId,
                AuditResponse(OK, Right(None))
              )
            )
        }
      }
    }
  }

  private def auditSubmission(details: GenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("CreateAmendDividendsIncome", "create-amend-dividends-income", details)
    auditService.auditEvent(event)
  }

}
