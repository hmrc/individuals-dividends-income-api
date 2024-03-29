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

package api.controllers.requestParsers.validators.validations

import api.models.errors.{CountryCodeFormatError, CountryCodeRuleError}
import support.UnitSpec

class CountryCodeValidationSpec extends UnitSpec {

  "CountryCodeValidation" when {
    "validate" must {
      "return an empty list for a valid country code" in {
        CountryCodeValidation.validate("GBR") shouldBe NoValidationErrors
      }

      "return a CountryCodeFormatError for an invalid country code" in {
        CountryCodeValidation.validate("notACountryCode") shouldBe List(CountryCodeFormatError)
      }

      "return a CountryCodeFormatError for an invalid format country code" in {
        CountryCodeValidation.validate("FRANCE") shouldBe List(CountryCodeFormatError)
      }

      "return a CountryCodeFormatError for an invalid rule country code" in {
        CountryCodeValidation.validate("FRE") shouldBe List(CountryCodeRuleError)
      }
    }

    "validateOptional" must {
      "return an empty list for a value of 'None'" in {
        CountryCodeValidation.validateOptional(None) shouldBe NoValidationErrors
      }

      "validate correctly for some valid country code" in {
        CountryCodeValidation.validateOptional(Some("FRA")) shouldBe NoValidationErrors
      }

      "validate correctly for some invalid format country code" in {
        CountryCodeValidation.validateOptional(Some("FRANCE")) shouldBe List(CountryCodeFormatError)
      }

      "validate correctly for some invalid rule country code" in {
        CountryCodeValidation.validateOptional(Some("SBT")) shouldBe List(CountryCodeRuleError)
      }
    }
  }

}

