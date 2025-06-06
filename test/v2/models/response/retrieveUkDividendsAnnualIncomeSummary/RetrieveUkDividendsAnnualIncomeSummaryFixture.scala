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

package v2.models.response.retrieveUkDividendsAnnualIncomeSummary

import play.api.libs.json.{JsValue, Json}

trait RetrieveUkDividendsAnnualIncomeSummaryFixture {

  protected val desResponseJson: JsValue = Json.parse("""
                                                         |{
                                                         |  "ukDividends": 10.12,
                                                         |  "otherUkDividends": 11.12
                                                         |}
                                                         |""".stripMargin)

  protected val ifsResponseJson: JsValue = Json.parse("""
                                                        |{
                                                        |  "ukDividendsAnnual" : {
                                                        |    "ukDividends": 10.12,
                                                        |    "otherUkDividends": 11.12
                                                        |  }
                                                        |}
                                                        |""".stripMargin)

  protected val mtdResponseJson: JsValue = Json.parse("""
                                                        |{
                                                        |  "ukDividends": 10.12,
                                                        |  "otherUkDividends": 11.12
                                                        |}
                                                        |""".stripMargin)

  protected val responseModel: RetrieveUkDividendsAnnualIncomeSummaryResponse =
    RetrieveUkDividendsAnnualIncomeSummaryResponse(Some(10.12), Some(11.12))

}
