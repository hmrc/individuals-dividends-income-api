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

package v1.models.response.retrieveDividends

import play.api.libs.json.{JsError, JsObject, JsValue, Json}
import shared.utils.UnitSpec
import v1.fixtures.RetrieveDividendsFixtures._

class DividendIncomeReceivedWhilstAbroadSpec extends UnitSpec {

  val mandatoryFieldsOnlyModel: DividendIncomeReceivedWhilstAbroadItem = DividendIncomeReceivedWhilstAbroadItem(
    countryCode = "DEU",
    amountBeforeTax = None,
    taxTakenOff = None,
    specialWithholdingTax = None,
    foreignTaxCreditRelief = None,
    taxableAmount = 4000.99
  )

  val mandatoryFieldsOnlyJson: JsValue = Json.parse(
    s"""
       |{
       |  "countryCode": "DEU",
       |  "taxableAmount": 4000.99
       |}
       |""".stripMargin
  )

  "DividendIncomeReceivedWhilstAbroadItem" when {
    "read from valid JSON" should {
      "produce the expected DividendIncomeReceivedWhilstAbroadItem object" in {
        dividendIncomeReceivedWhilstAbroadJson.as[DividendIncomeReceivedWhilstAbroadItem] shouldBe dividendIncomeReceivedWhilstAbroadModel
      }
    }

    "read from valid JSON with mandatory fields only" should {
      "produce the expected ForeignDividendItem object" in {
        mandatoryFieldsOnlyJson.as[DividendIncomeReceivedWhilstAbroadItem] shouldBe mandatoryFieldsOnlyModel
      }
    }

    "read from empty JSON" should {
      "produce a JsError" in {
        val invalidJson = JsObject.empty

        invalidJson.validate[DividendIncomeReceivedWhilstAbroadItem] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JsObject" in {
        Json.toJson(dividendIncomeReceivedWhilstAbroadModel) shouldBe dividendIncomeReceivedWhilstAbroadJson
      }
    }

    "written to JSON with mandatory fields only" should {
      "produce the expected JsObject" in {
        Json.toJson(mandatoryFieldsOnlyModel) shouldBe mandatoryFieldsOnlyJson
      }
    }
  }

}
