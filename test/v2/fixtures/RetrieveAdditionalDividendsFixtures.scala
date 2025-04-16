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

package v2.fixtures

import play.api.libs.json.{JsValue, Json}
import v2.models.response.retrieveAdditionalDividends.RetrieveAdditionalDividendsResponse

object RetrieveAdditionalDividendsFixtures {

  val retrieveModel: RetrieveAdditionalDividendsResponse = RetrieveAdditionalDividendsResponse(companyDirector = true, Some(true), Some("2024-04-06"), Some("Company One"), Some("36488522"), Some(20.95), Some(1024.99), "2025-08-24T14:15:22Z")

  val mtdResponse: JsValue = Json.parse(
    """
      |{
      |  "companyDirector": true,
      |  "closeCompany": true,
      |  "directorshipCeasedDate": "2024-04-06",
      |  "companyName": "Company One",
      |  "companyNumber": "36488522",
      |  "shareholding": 20.95,
      |  "dividendReceived": 1024.99,
      |  "submittedOn": "2025-08-24T14:15:22Z"
      |}
    """.stripMargin
  )
}
