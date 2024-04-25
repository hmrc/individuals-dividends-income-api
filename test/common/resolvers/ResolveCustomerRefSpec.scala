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

package common.resolvers

import cats.data.Validated.{Invalid, Valid}
import common.controllers.validators.resolvers.ResolveCustomerRef
import common.models.domain.CustomerRef
import shared.UnitSpec
import shared.models.errors.CustomerRefFormatError

class ResolveCustomerRefSpec extends UnitSpec {

  private val path: String = "some/path"

  "ResolveCustomerRefSpec" should {
    "return no errors" when {
      "given a valid customerRef" in {
        val validCustomerRef = "validRef"
        val result           = ResolveCustomerRef(validCustomerRef, path)
        result shouldBe Valid(CustomerRef(validCustomerRef))
      }
    }

    "return an error" when {
      "given an empty string for customerRef" in {
        val customerRef = ""
        val result      = ResolveCustomerRef(customerRef)
        result shouldBe Invalid(List(CustomerRefFormatError))
      }
      "given an 92 character string for customerRef" in {
        val customerRef = "1234567890qwertyuiop[asdfghjklzxcvbnm.,q21wer1234567890qwertyuiop[asdfghjklzxcvbnm.,q21werx"
        val result      = ResolveCustomerRef(customerRef, path)
        result shouldBe Invalid(List(CustomerRefFormatError.withPath(path)))
      }
    }
  }

}
