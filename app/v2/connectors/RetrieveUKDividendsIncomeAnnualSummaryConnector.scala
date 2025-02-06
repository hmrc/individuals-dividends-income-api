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

package v2.connectors

import config.DividendsIncomeFeatureSwitches
import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.{DesUri, IfsUri, TaxYearSpecificIfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser._
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v2.models.request.retrieveUkDividendsAnnualIncomeSummary.RetrieveUkDividendsIncomeAnnualSummaryRequest
import v2.models.response.retrieveUkDividendsAnnualIncomeSummary.RetrieveUkDividendsAnnualIncomeSummaryResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveUKDividendsIncomeAnnualSummaryConnector @Inject() (val http: HttpClient, val appConfig: SharedAppConfig)
    extends BaseDownstreamConnector {

  def retrieveUKDividendsIncomeAnnualSummary(request: RetrieveUkDividendsIncomeAnnualSummaryRequest)(implicit
                                                                                                     hc: HeaderCarrier,
                                                                                                     ec: ExecutionContext,
                                                                                                     correlationId: String): Future[DownstreamOutcome[RetrieveUkDividendsAnnualIncomeSummaryResponse]] = {

    import request._

    val path = s"income-tax/nino/$nino/income-source/dividends/annual/${taxYear.asDownstream}"

    val downstreamUri =
      if (taxYear.useTaxYearSpecificApi) {
        TaxYearSpecificIfsUri[RetrieveUkDividendsAnnualIncomeSummaryResponse](
          s"income-tax/${taxYear.asTysDownstream}/$nino/income-source/dividends/annual")
      } else if (DividendsIncomeFeatureSwitches().isDesIfMigrationEnabled) {
        IfsUri[RetrieveUkDividendsAnnualIncomeSummaryResponse](path)
      } else {
        DesUri[RetrieveUkDividendsAnnualIncomeSummaryResponse](path)
      }

    get(downstreamUri)

  }

}
