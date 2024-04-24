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
import shared.controllers.validators.RulesValidator
import shared.controllers.validators.resolvers.ResolveParsedNumber
import shared.models.errors.MtdError
import v1.models.request.createAmendUkDividendsIncomeAnnualSummary._

object CreateAmendUkDividendsIncomeAnnualSummaryValidator extends RulesValidator[CreateAmendUkDividendsIncomeAnnualSummaryRequest] {

  private val minValue: BigDecimal = 0
  val maxValue: BigDecimal         = 99999999999.99

  override def validateBusinessRules(
      parsed: CreateAmendUkDividendsIncomeAnnualSummaryRequest): Validated[Seq[MtdError], CreateAmendUkDividendsIncomeAnnualSummaryRequest] = {

    val body = parsed.body
    def bodyValueValidator: Validated[Seq[MtdError], CreateAmendUkDividendsIncomeAnnualSummaryRequest] = {
      val resolvedDividend                                         = ResolveParsedNumber(min = minValue, max = maxValue)
      def resolveDividend(value: Option[BigDecimal], path: String) = resolvedDividend(value, path)

      combine(
        resolveDividend(body.ukDividends, "/ukDividends"),
        resolveDividend(body.otherUkDividends, "/otherUkDividends")
      ).onSuccess(parsed)

    }
    bodyValueValidator
  }

}
