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

package v1.controllers.validators

import cats.data.Validated
import cats.implicits.catsSyntaxTuple3Semigroupal
import shared.controllers.validators.resolvers.ResolveTaxYearMinimum
import config.AppConfig
import play.api.libs.json.JsValue
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveNino, ResolveNonEmptyJsonObject}
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import v1.controllers.validators.CreateAmendUkDividendsIncomeAnnualSummaryValidator.validateBusinessRules
import v1.models.request.createAmendUkDividendsIncomeAnnualSummary.{
  CreateAmendUkDividendsIncomeAnnualSummaryBody,
  CreateAmendUkDividendsIncomeAnnualSummaryRequest
}

import javax.inject.{Inject, Singleton}

@Singleton
class CreateAmendUkDividendsIncomeAnnualSummaryValidatorFactory @Inject() (implicit appConfig: AppConfig) {
  private lazy val minTaxYear: TaxYear = TaxYear.fromDownstreamInt(appConfig.ukDividendsMinimumTaxYear)
  private lazy val resolveTaxYear      = ResolveTaxYearMinimum(minTaxYear)

  private val resolveNonEmptyJsonObject = new ResolveNonEmptyJsonObject[CreateAmendUkDividendsIncomeAnnualSummaryBody]()

  def validator(nino: String, taxYear: String, body: JsValue): Validator[CreateAmendUkDividendsIncomeAnnualSummaryRequest] =
    new Validator[CreateAmendUkDividendsIncomeAnnualSummaryRequest] {

      override def validate: Validated[Seq[MtdError], CreateAmendUkDividendsIncomeAnnualSummaryRequest] =
        (
          ResolveNino(nino),
          resolveTaxYear(taxYear),
          resolveNonEmptyJsonObject(body)
        ).mapN(CreateAmendUkDividendsIncomeAnnualSummaryRequest) andThen validateBusinessRules

    }

}
