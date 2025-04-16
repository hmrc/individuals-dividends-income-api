package v2.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers._
import shared.models.errors._
import shared.services._
import shared.support.IntegrationBaseSpec

import v2.fixtures.RetrieveAdditionalDividendsFixtures._

class RetrieveAdditionalDividendsControllerISpec extends IntegrationBaseSpec {

  "Calling the 'Retrieve additional dividend and directorship data' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUri, Map("taxYear" -> downstreamTaxYear), OK, downstreamResponse)
        }

        val response: WSResponse = await(request.get())
        response.status shouldBe OK
        response.json shouldBe mtdResponse
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String,
                                requestTaxYear: String,
                                requestEmploymentId: String,
                                expectedStatus: Int,
                                expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String         = requestNino
            override val taxYear: String      = requestTaxYear
            override val employmentId: String = requestEmploymentId

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = Seq(
          ("AA1123A", "2025-26", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "20177", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2025-26", "incorrect-id", BAD_REQUEST, EmploymentIdFormatError),
          ("AA123456A", "2015-17", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", BAD_REQUEST, RuleTaxYearRangeInvalidError),
          ("AA123456A", "2015-16", "4557ecb5-fd32-48cc-81f5-e6acd1099f3c", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )

        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "downstream service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns an $downstreamCode error and status $downstreamStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.GET, downstreamUri, Map("taxYear" -> downstreamTaxYear), downstreamStatus, errorBody(downstreamCode))
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        def errorBody(code: String): String =
          s"""
             |[
             |    {
             |        "errorCode": "$code",
             |        "errorDescription": "error description"
             |    }
             |]
          """.stripMargin

        val errors = List(
          (BAD_REQUEST, "1215", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "1117", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "1217", BAD_REQUEST, EmploymentIdFormatError),
          (NOT_FOUND, "5010", NOT_FOUND, NotFoundError),
          (BAD_REQUEST, "1216", INTERNAL_SERVER_ERROR, InternalError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "UNMATCHED_STUB_ERROR", BAD_REQUEST, RuleIncorrectGovTestScenarioError)
        )

        errors.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

  private trait Test {

    val nino: String         = "AA123456A"
    val employmentId: String = "4557ecb5-fd32-48cc-81f5-e6acd1099f3c"

    def taxYear: String           = "2025-26"
    def downstreamTaxYear: String = "25-26"

    def downstreamUri: String = s"/itsd/income-sources/$nino/directorships/$employmentId"

    val downstreamResponse: JsValue = Json.parse(
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
      """.stripMargin)

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(s"/directorship/$nino/$taxYear/$employmentId")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.2.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

  }

}
