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

import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.HipUri
import shared.connectors.httpparsers.StandardDownstreamHttpParser._
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v2.models.request.createAmendAdditionalDirectorshipDividend.CreateAmendAdditionalDirectorshipDividendRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateAmendAdditionalDirectorshipDividendConnector @Inject()(val http: HttpClientV2, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def createAmend(request: CreateAmendAdditionalDirectorshipDividendRequest)(implicit
                                                                             hc: HeaderCarrier,
                                                                             ec: ExecutionContext,
                                                                             correlationId: String): Future[DownstreamOutcome[Unit]] = {
    import request._

    val downstreamUri: DownstreamUri[Unit] =
      HipUri[Unit](s"itsd/income-sources/$nino/directorships/$employmentId?taxYear=${taxYear.asTysDownstream}")

    put(body = request.body, uri = downstreamUri)
  }
}
