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

import config.AppConfig
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import shared.UnitSpec
import shared.controllers.validators.ValueFormatErrorMessages
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import v1.controllers.validators.CreateAmendUkDividendsIncomeAnnualSummaryValidatorFactory
import v1.models.request.createAmendUkDividendsIncomeAnnualSummary.{CreateAmendUkDividendsIncomeAnnualSummaryBody, CreateAmendUkDividendsIncomeAnnualSummaryRequest}

class CreateAmendUkDividendsIncomeAnnualSummaryValidatorSpec extends UnitSpec with MockAppConfig with ValueFormatErrorMessages {

  implicit val correlationId: String = "1234"

  object Data {
    val validNino             = "AA123456A"
    val validTaxYear          = "2019-20"
    val validUkDividends      = 55844806400.99
    val validOtherUkDividends = 60267421355.99

    val validRequestBodyJson: JsValue = Json.parse(s"""
                                                              |{
                                                              | "ukDividends": $validUkDividends,
                                                              | "otherUkDividends": $validOtherUkDividends
                                                              |}
                                                              |""".stripMargin)

    val emptyRequestBodyJson: JsValue = Json.parse("""{}""")

    val nonsenseRequestBodyJson: JsValue = Json.parse("""{"field": "value"}""")

    val nonValidRequestBodyJson: JsValue = Json.parse(
      """
        |{
        |  "ukDividends": true
        |}
    """.stripMargin
    )

    val invalidUkDividendsJson: JsValue = Json.parse(s"""
                                                                |{
                                                                |  "ukDividends": -1,
                                                                |  "otherUkDividends": $validOtherUkDividends
                                                                |}
                                                                |""".stripMargin)

    val invalidOtherUkDividendsJson: JsValue = Json.parse(s"""
                                                                     |{
                                                                     |  "ukDividends": $validUkDividends,
                                                                     |  "otherUkDividends": -1
                                                                     |}
                                                                     |""".stripMargin)

    val createAmendUkDividendsIncomeAnnualSummaryBody: CreateAmendUkDividendsIncomeAnnualSummaryBody = CreateAmendUkDividendsIncomeAnnualSummaryBody(
      ukDividends = Some(validUkDividends),
      otherUkDividends = Some(validOtherUkDividends)
    )

  }

  import Data._

  class Test extends MockAppConfig {
    private val MINIMUM_YEAR          = 2018
    implicit val appConfig: AppConfig = mockAppConfig

    MockedAppConfig.ukDividendsMinimumTaxYear
      .returns(MINIMUM_YEAR)
      .anyNumberOfTimes()

    val validatorFactory                                        = new CreateAmendUkDividendsIncomeAnnualSummaryValidatorFactory
    def validator(nino: String, taxYear: String, body: JsValue) = validatorFactory.validator(nino, taxYear, body)

  }

  "running validation" should {
    "return no errors" when {
      "passed a valid raw request model" in new Test {
        val result = validator(validNino, validTaxYear, validRequestBodyJson).validateAndWrapResult()
        result shouldBe Right(
          CreateAmendUkDividendsIncomeAnnualSummaryRequest(
            Nino(validNino),
            TaxYear.fromMtd(validTaxYear),
            createAmendUkDividendsIncomeAnnualSummaryBody))
      }
    }

    "return NinoFormatError error" when {
      "passed an invalid nino" in new Test {
        val result = validator("A12344A", validTaxYear, validRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }

    "return TaxYearFormatError error" when {
      "passed an invalid taxYear" in new Test {
        val result = validator(validNino, "201495", validRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "an invalid tax year is supplied" in new Test {
        val result = validator(validNino, "2016-17", validRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }
    }

    "return RuleTaxYearRangeInvalidError error" when {
      "an invalid tax year is supplied" in new Test {
        val result = validator(validNino, "2019-23", validRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }
    }

    "return RuleIncorrectOrEmptyBodyError error" when {
      "an empty JSON body is submitted" in new Test {
        val result = validator(validNino, validTaxYear, emptyRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "a non-empty JSON body is submitted without any expected fields" in new Test {
        val result = validator(validNino, validTaxYear, nonsenseRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "the submitted request body is not in the correct format" in new Test {
        val result = validator(validNino, validTaxYear, nonValidRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleIncorrectOrEmptyBodyError.copy(
              paths = Some(
                Seq(
                  "/ukDividends"
                ))
            )))
      }
    }

    "return ukDividendsFormatError" when {
      "passed invalid ukDividends" in new Test {
        val result = validator(validNino, validTaxYear, invalidUkDividendsJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(
                Seq(
                  "/ukDividends"
                )))))
      }
    }
    "return otherUkDividendsFormatError" when {
      "passed invalid otherUkDividends" in new Test {
        val result = validator(validNino, validTaxYear, invalidOtherUkDividendsJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(
                Seq(
                  "/otherUkDividends"
                )))))
      }
    }

  }

}
