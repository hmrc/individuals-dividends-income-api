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

package v2.mocks.validators

import org.scalamock.handlers.CallHandler
import shared.controllers.validators.{MockValidatorFactory, Validator}
import v2.controllers.DeleteUkDividendsIncomeAnnualSummaryValidatorFactory
import v2.models.request.deleteUkDividendsIncomeAnnualSummary.DeleteUkDividendsIncomeAnnualSummaryRequest

trait MockDeleteUkDividendsIncomeAnnualSummaryValidatorFactory extends MockValidatorFactory[DeleteUkDividendsIncomeAnnualSummaryRequest] {

    val mockDeleteUkDividendsIncomeAnnualSummaryValidatorFactory: DeleteUkDividendsIncomeAnnualSummaryValidatorFactory = mock[DeleteUkDividendsIncomeAnnualSummaryValidatorFactory]

    def validator(): CallHandler[Validator[DeleteUkDividendsIncomeAnnualSummaryRequest]] =
      (mockDeleteUkDividendsIncomeAnnualSummaryValidatorFactory.validator(_: String, _: String)).expects(*, *)

  }
