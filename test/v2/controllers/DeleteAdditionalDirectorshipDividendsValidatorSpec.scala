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

package v2.controllers

import shared.models.domain.{EmploymentId, Nino, TaxYear}
import shared.models.errors._
import shared.utils.UnitSpec
import v2.models.request.deleteAdditionalDirectorshipDividends.DeleteAdditionalDirectorshipDividendsRequest

class DeleteAdditionalDirectorshipDividendsValidatorSpec extends UnitSpec {
  private implicit val correlationId: String = "1234"

  private val validNino    = "AA123456A"
  private val validTaxYear = "2025-26"

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private val employmentId        = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"
  private val parsedEmploymentId    = EmploymentId(employmentId)

  private def validator(nino: String, taxYear: String, employmentId: String): DeleteAdditionalDirectorshipDividendsValidator = new DeleteAdditionalDirectorshipDividendsValidator(nino, taxYear, employmentId)

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        val result: Either[ErrorWrapper, DeleteAdditionalDirectorshipDividendsRequest] = validator(validNino, validTaxYear, employmentId).validateAndWrapResult()

        result shouldBe Right(DeleteAdditionalDirectorshipDividendsRequest(parsedNino, parsedTaxYear, parsedEmploymentId))
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in {
        val result: Either[ErrorWrapper, DeleteAdditionalDirectorshipDividendsRequest] = validator("A12344A", validTaxYear, employmentId).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }

    "return EmploymentIdFormatError error" when {
      "an invalid employment id is supplied" in {
        val result: Either[ErrorWrapper, DeleteAdditionalDirectorshipDividendsRequest] = validator(validNino, validTaxYear, "4557ecb5-fd32-48cc-81f5").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, EmploymentIdFormatError))
      }
    }


    "return TaxYearFormatError error" when {
      "an invalid tax year is supplied" in {
        val result: Either[ErrorWrapper, DeleteAdditionalDirectorshipDividendsRequest] = validator(validNino, "20178", employmentId).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }
    }

    "return RuleTaxYearRangeInvalidError error" when {
      "an invalid tax year range is supplied" in {
        val result: Either[ErrorWrapper, DeleteAdditionalDirectorshipDividendsRequest] = validator(validNino, "2019-21", employmentId).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "an invalid tax year is supplied" in {
        val result: Either[ErrorWrapper, DeleteAdditionalDirectorshipDividendsRequest] = validator(validNino, "2018-19", employmentId).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in {
        val result: Either[ErrorWrapper, DeleteAdditionalDirectorshipDividendsRequest] = validator("A12344A", "20178", employmentId).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}
