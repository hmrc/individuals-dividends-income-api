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

package api.services

import api.mocks.connectors.MockMtdIdLookupConnector
import api.connectors.MtdIdLookupConnector
import api.models.errors.{ClientOrAgentNotAuthorisedError, InternalError, InvalidBearerTokenError, NinoFormatError}

import scala.concurrent.Future

class MtdIdLookupServiceSpec extends ServiceSpec {

  val nino = "AA123456A"

  trait Test extends MockMtdIdLookupConnector {
    lazy val target = new MtdIdLookupService(mockMtdIdLookupConnector)
  }

  "calling .getMtdId" when {

    "an mtdId is found for the NINO" should {
      "return the mtdId" in new Test {
        val mtdId = "someMtdId"

        MockedMtdIdLookupConnector.lookup(nino) returns Future.successful(Right(mtdId))

        await(target.lookup(nino)) shouldBe Right(mtdId)
      }
    }

    "an invalid NINO is passed in" should {
      "return NinoFormatError" in new Test {
        val invalidNino = "INVALID_NINO"

        await(target.lookup(invalidNino)) shouldBe Left(NinoFormatError)
      }
    }

    "the downstream service returns a 403 status error" should {
      "return ClientOrAgentNotAuthorisedError" in new Test {
        MockedMtdIdLookupConnector.lookup(nino) returns Future.successful(Left(MtdIdLookupConnector.Error(FORBIDDEN)))

        await(target.lookup(nino)) shouldBe Left(ClientOrAgentNotAuthorisedError)
      }
    }

    "the downstream service returns a 401 status error" should {
      "return InvalidBearerTokenError" in new Test {
        MockedMtdIdLookupConnector.lookup(nino) returns Future.successful(Left(MtdIdLookupConnector.Error(UNAUTHORIZED)))

        await(target.lookup(nino)) shouldBe Left(InvalidBearerTokenError)
      }
    }

    "the downstream service returns another status code" should {
      "return InternalError" in new Test {
        MockedMtdIdLookupConnector.lookup(nino) returns Future.successful(Left(MtdIdLookupConnector.Error(IM_A_TEAPOT)))

        await(target.lookup(nino)) shouldBe Left(InternalError)
      }
    }

  }

}
