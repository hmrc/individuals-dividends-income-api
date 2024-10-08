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

package v1.connectors

import api.connectors.DownstreamUri.{DesUri, IfsUri, TaxYearSpecificIfsUri}
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.{AppConfig, FeatureSwitches}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.models.request.retrieveUkDividendsAnnualIncomeSummary.RetrieveUkDividendsAnnualIncomeSummaryRequest
import v1.models.response.retrieveUkDividendsAnnualIncomeSummary.RetrieveUkDividendsAnnualIncomeSummaryResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveUKDividendsIncomeAnnualSummaryConnector @Inject() (val http: HttpClient, val appConfig: AppConfig)(implicit
    featureSwitches: FeatureSwitches)
    extends BaseDownstreamConnector {

  def retrieveUKDividendsIncomeAnnualSummary(request: RetrieveUkDividendsAnnualIncomeSummaryRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveUkDividendsAnnualIncomeSummaryResponse]] = {

    import api.connectors.httpparsers.StandardDownstreamHttpParser._
    import request.nino.nino
    import request.taxYear

    val path = s"income-tax/nino/$nino/income-source/dividends/annual/${taxYear.asDownstream}"

    val downstreamUri =
      if (taxYear.useTaxYearSpecificApi) {
        TaxYearSpecificIfsUri[RetrieveUkDividendsAnnualIncomeSummaryResponse](
          s"income-tax/${taxYear.asTysDownstream}/$nino/income-source/dividends/annual")
      } else if (featureSwitches.isDesIf_MigrationEnabled) {
        IfsUri[RetrieveUkDividendsAnnualIncomeSummaryResponse](path)
      } else {
        DesUri[RetrieveUkDividendsAnnualIncomeSummaryResponse](path)
      }

    get(downstreamUri)

  }

}
