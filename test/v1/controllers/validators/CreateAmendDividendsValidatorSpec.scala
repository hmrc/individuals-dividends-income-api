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
import v1.controllers.validators.CreateAmendDividendsValidatorFactory
import v1.models.request.createAmendDividends.{CreateAmendDividendsRequest, CreateAmendDividendsRequestBodyFixture}

class CreateAmendDividendsValidatorSpec
    extends UnitSpec
    with MockAppConfig
    with ValueFormatErrorMessages
    with CreateAmendDividendsRequestBodyFixture {

  implicit val correlationId: String = "1234"
  private val validNino              = "AA123456A"
  private val validTaxYear           = "2020-21"

  private val validRequestBodyJson: JsValue = json

  private val emptyRequestBodyJson: JsValue = Json.parse("""{}""")

  private val nonsenseRequestBodyJson: JsValue = Json.parse("""{"field": "value"}""")

  private val nonValidRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "stockDividend": {
      |      "customerReference": "my divs",
      |      "grossAmount": "no"
      |   }
      |}
    """.stripMargin
  )

  private val missingMandatoryFieldJson: JsValue = Json.parse(
    """
      |{
      |   "foreignDividend": [
      |      {
      |        "amountBeforeTax": 1232.22,
      |        "taxTakenOff": 22.22,
      |        "specialWithholdingTax": 27.35
      |      },
      |      {
      |        "amountBeforeTax": 1350.55,
      |        "taxTakenOff": 25.27,
      |        "specialWithholdingTax": 30.59
      |      }
      |   ],
      |   "dividendIncomeReceivedWhilstAbroad": [
      |      {
      |        "amountBeforeTax": 1232.22,
      |        "taxTakenOff": 22.22,
      |        "specialWithholdingTax": 27.35
      |      },
      |      {
      |        "amountBeforeTax": 1350.55,
      |        "taxTakenOff": 25.27,
      |        "specialWithholdingTax": 30.59
      |       }
      |   ],
      |   "stockDividend": {
      |      "customerReference": "my divs"
      |   },
      |   "redeemableShares": {
      |      "customerReference": "my shares"
      |   },
      |   "bonusIssuesOfSecurities": {
      |      "customerReference": "my secs"
      |   },
      |   "closeCompanyLoansWrittenOff": {
      |      "customerReference": "write off"
      |   }
      |}
    """.stripMargin
  )

  private val invalidCountryCodeRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignDividend": [
      |      {
      |        "countryCode": "GERMANY",
      |        "amountBeforeTax": 1232.22,
      |        "taxTakenOff": 22.22,
      |        "specialWithholdingTax": 27.35,
      |        "foreignTaxCreditRelief": true,
      |        "taxableAmount": 2321.22
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidCountryCodeRuleRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "dividendIncomeReceivedWhilstAbroad": [
      |      {
      |        "countryCode": "SBT",
      |        "amountBeforeTax": 1232.22,
      |        "taxTakenOff": 22.22,
      |        "specialWithholdingTax": 27.35,
      |        "foreignTaxCreditRelief": true,
      |        "taxableAmount": 2321.22
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidCustomerRefRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "redeemableShares": {
      |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
      |      "grossAmount": 12345.75
      |   }
      |}
    """.stripMargin
  )

  private val invalidForeignDividendRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignDividend": [
      |      {
      |        "countryCode": "DEU",
      |        "amountBeforeTax": 1232.223,
      |        "taxTakenOff": 22.22,
      |        "specialWithholdingTax": 27.35,
      |        "foreignTaxCreditRelief": true,
      |        "taxableAmount": 2321.22
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidDividendIncomeReceivedWhilstAbroadRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "dividendIncomeReceivedWhilstAbroad": [
      |      {
      |        "countryCode": "DEU",
      |        "amountBeforeTax": 1232.22,
      |        "taxTakenOff": -22.22,
      |        "specialWithholdingTax": 27.35,
      |        "foreignTaxCreditRelief": true,
      |        "taxableAmount": 2321.22
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private val invalidStockDividendRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "stockDividend": {
      |      "customerReference": "my divs",
      |      "grossAmount": 12321.224
      |   }
      |}
    """.stripMargin
  )

  private val invalidRedeemableSharesRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "redeemableShares": {
      |      "customerReference": "my shares",
      |      "grossAmount": -12345.75
      |   }
      |}
    """.stripMargin
  )

  private val invalidBonusIssuesOfSecuritiesRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "bonusIssuesOfSecurities": {
      |      "customerReference": "my secs",
      |      "grossAmount": 12500.899
      |   }
      |}
    """.stripMargin
  )

  private val invalidCloseCompanyLoansWrittenOffRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "closeCompanyLoansWrittenOff": {
      |      "customerReference": "write off",
      |      "grossAmount": -13700.55
      |   }
      |}
    """.stripMargin
  )

  private val allInvalidValueRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "foreignDividend": [
      |      {
      |        "countryCode": "GERMANY",
      |        "amountBeforeTax": -1232.22,
      |        "taxTakenOff": 22.223,
      |        "specialWithholdingTax": 27.354,
      |        "foreignTaxCreditRelief": true,
      |        "taxableAmount": -2321.22
      |      },
      |      {
      |        "countryCode": "PUR",
      |        "amountBeforeTax": 1350.559,
      |        "taxTakenOff": 25.278,
      |        "specialWithholdingTax": -30.59,
      |        "foreignTaxCreditRelief": false,
      |        "taxableAmount": -2500.99
      |      }
      |   ],
      |   "dividendIncomeReceivedWhilstAbroad": [
      |      {
      |        "countryCode": "FRANCE",
      |        "amountBeforeTax": 1232.227,
      |        "taxTakenOff": 22.224,
      |        "specialWithholdingTax": 27.358,
      |        "foreignTaxCreditRelief": true,
      |        "taxableAmount": 2321.229
      |      },
      |      {
      |        "countryCode": "SBT",
      |        "amountBeforeTax": -1350.55,
      |        "taxTakenOff": -25.27,
      |        "specialWithholdingTax": -30.59,
      |        "foreignTaxCreditRelief": false,
      |        "taxableAmount": -2500.99
      |       }
      |   ],
      |   "stockDividend": {
      |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
      |      "grossAmount": -12321.22
      |   },
      |   "redeemableShares": {
      |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
      |      "grossAmount": 12345.758
      |   },
      |   "bonusIssuesOfSecurities": {
      |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
      |      "grossAmount": -12500.89
      |   },
      |   "closeCompanyLoansWrittenOff": {
      |      "customerReference": "This customer ref string is 91 characters long ------------------------------------------91",
      |      "grossAmount": 13700.557
      |   }
      |}
    """.stripMargin
  )

  class Test extends MockAppConfig {
    implicit val appConfig: AppConfig = mockAppConfig

    MockedAppConfig.minimumPermittedTaxYear
      .returns(2021)
      .anyNumberOfTimes()

    val validatorFactory                                        = new CreateAmendDividendsValidatorFactory
    def validator(nino: String, taxYear: String, body: JsValue) = validatorFactory.validator(nino, taxYear, body)

  }

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        val result = validator(validNino, validTaxYear, validRequestBodyJson).validateAndWrapResult()
        result shouldBe Right(CreateAmendDividendsRequest(Nino(validNino), TaxYear.fromMtd(validTaxYear), requestBodyModel))
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in new Test {
        val result = validator("A12344A", validTaxYear, validRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }

    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in new Test {
        val result = validator(validNino, "20178", validRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
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
        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/stockDividend/grossAmount")))))
      }

      "return RuleTaxYearRangeInvalidError error" when {
        "an invalid tax year range is supplied" in new Test {
          val result = validator(validNino, "2019-21", validRequestBodyJson).validateAndWrapResult()
          result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
        }
      }

      "return RuleTaxYearNotSupportedError error" when {
        "an invalid tax year is supplied" in new Test {
          val result = validator(validNino, "2018-19", validRequestBodyJson).validateAndWrapResult()
          result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
        }
      }

      "mandatory fields are not provided" in new Test {
        val paths: Seq[String] = Seq(
          "/bonusIssuesOfSecurities/grossAmount",
          "/closeCompanyLoansWrittenOff/grossAmount",
          "/dividendIncomeReceivedWhilstAbroad/0/countryCode",
          "/dividendIncomeReceivedWhilstAbroad/0/taxableAmount",
          "/dividendIncomeReceivedWhilstAbroad/1/countryCode",
          "/dividendIncomeReceivedWhilstAbroad/1/taxableAmount",
          "/foreignDividend/0/countryCode",
          "/foreignDividend/0/taxableAmount",
          "/foreignDividend/1/countryCode",
          "/foreignDividend/1/taxableAmount",
          "/redeemableShares/grossAmount",
          "/stockDividend/grossAmount"
        )

        val result = validator(validNino, validTaxYear, missingMandatoryFieldJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.copy(paths = Some(paths))))
      }
    }

    "return CountryCodeFormatError error" when {
      "an incorrectly formatted country code is submitted" in new Test {
        val result = validator(validNino, validTaxYear, invalidCountryCodeRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, CountryCodeFormatError.copy(paths = Some(List("/foreignDividend/0/countryCode")))))
      }
    }

    "return CountryCodeRuleError error" when {
      "an invalid country code is submitted" in new Test {
        val result = validator(validNino, validTaxYear, invalidCountryCodeRuleRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, RuleCountryCodeError.copy(paths = Some(List("/dividendIncomeReceivedWhilstAbroad/0/countryCode")))))
      }
    }

    "return CustomerRefFormatError error" when {
      "an incorrectly formatted customer reference is submitted" in new Test {
        val result = validator(validNino, validTaxYear, invalidCustomerRefRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, CustomerRefFormatError.copy(paths = Some(List("/redeemableShares/customerReference")))))
      }
    }

    "return ValueFormatError error (single failure)" when {
      "one field fails value validation (foreign dividend)" in new Test {
        val result = validator(validNino, validTaxYear, invalidForeignDividendRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(Seq("/foreignDividend/0/amountBeforeTax"))
            )))
      }

      "one field fails value validation (dividend income received whilst abroad)" in new Test {
        val result = validator(validNino, validTaxYear, invalidDividendIncomeReceivedWhilstAbroadRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(Seq("/dividendIncomeReceivedWhilstAbroad/0/taxTakenOff"))
            )))
      }

      "one field fails value validation (stock dividend)" in new Test {
        val result = validator(validNino, validTaxYear, invalidStockDividendRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(Seq("/stockDividend/grossAmount"))
            )))
      }

      "one field fails value validation (redeemable shares)" in new Test {
        val result = validator(validNino, validTaxYear, invalidRedeemableSharesRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(Seq("/redeemableShares/grossAmount"))
            )))
      }

      "one field fails value validation (bonus issues of securities)" in new Test {
        val result = validator(validNino, validTaxYear, invalidBonusIssuesOfSecuritiesRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(Seq("/bonusIssuesOfSecurities/grossAmount"))
            )))
      }

      "one field fails value validation (close company loans written off)" in new Test {
        val result = validator(validNino, validTaxYear, invalidCloseCompanyLoansWrittenOffRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.copy(
              message = ZERO_MINIMUM_INCLUSIVE,
              paths = Some(Seq("/closeCompanyLoansWrittenOff/grossAmount"))
            )))
      }
    }

    "return ValueFormatError error (multiple failures)" when {
      "multiple fields fail value validation" in new Test {
        val result = validator(validNino, validTaxYear, allInvalidValueRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(
              CountryCodeFormatError.copy(
                paths = Some(List(
                  "/foreignDividend/0/countryCode",
                  "/dividendIncomeReceivedWhilstAbroad/0/countryCode"
                ))
              ),
              CustomerRefFormatError.copy(
                paths = Some(List(
                  "/stockDividend/customerReference",
                  "/redeemableShares/customerReference",
                  "/bonusIssuesOfSecurities/customerReference",
                  "/closeCompanyLoansWrittenOff/customerReference"
                ))
              ),
              ValueFormatError.copy(
                message = ZERO_MINIMUM_INCLUSIVE,
                paths = Some(List(
                  "/foreignDividend/0/amountBeforeTax",
                  "/foreignDividend/0/taxTakenOff",
                  "/foreignDividend/0/specialWithholdingTax",
                  "/foreignDividend/0/taxableAmount",
                  "/foreignDividend/1/amountBeforeTax",
                  "/foreignDividend/1/taxTakenOff",
                  "/foreignDividend/1/specialWithholdingTax",
                  "/foreignDividend/1/taxableAmount",
                  "/dividendIncomeReceivedWhilstAbroad/0/amountBeforeTax",
                  "/dividendIncomeReceivedWhilstAbroad/0/taxTakenOff",
                  "/dividendIncomeReceivedWhilstAbroad/0/specialWithholdingTax",
                  "/dividendIncomeReceivedWhilstAbroad/0/taxableAmount",
                  "/dividendIncomeReceivedWhilstAbroad/1/amountBeforeTax",
                  "/dividendIncomeReceivedWhilstAbroad/1/taxTakenOff",
                  "/dividendIncomeReceivedWhilstAbroad/1/specialWithholdingTax",
                  "/dividendIncomeReceivedWhilstAbroad/1/taxableAmount",
                  "/stockDividend/grossAmount",
                  "/redeemableShares/grossAmount",
                  "/bonusIssuesOfSecurities/grossAmount",
                  "/closeCompanyLoansWrittenOff/grossAmount"
                ))
              ),
              RuleCountryCodeError.copy(
                paths = Some(List(
                  "/foreignDividend/1/countryCode",
                  "/dividendIncomeReceivedWhilstAbroad/1/countryCode"
                ))
              )
            ))
          ))
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors (path parameters)" in new Test {
        val result = validator("A12344A", "20178", emptyRequestBodyJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, TaxYearFormatError, RuleIncorrectOrEmptyBodyError))))
      }
    }
  }

}
