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

import cats.data.Validated
import cats.data.Validated.Valid
import cats.implicits.toTraverseOps
import common.controllers.validators.resolvers.ResolveCustomerRef
import shared.controllers.validators.RulesValidator
import shared.controllers.validators.resolvers.{ResolveParsedCountryCode, ResolveParsedNumber}
import shared.models.errors.MtdError
import v1.models.request.createAmendDividends._

import javax.inject.Singleton

@Singleton
object CreateAmendDividendsValidator extends RulesValidator[CreateAmendDividendsRequest] {

  override def validateBusinessRules(parsed: CreateAmendDividendsRequest): Validated[Seq[MtdError], CreateAmendDividendsRequest] = {
    val body = parsed.body

    val resolvedMonetaryValue                                                 = ResolveParsedNumber()
    def resolveOptionalMonetaryValue(value: Option[BigDecimal], path: String) = resolvedMonetaryValue(value, path)
    def resolveMonetaryValue(value: BigDecimal, path: String)                 = resolvedMonetaryValue(value, path)

    def resolveCustomerRef(value: Option[String], path: String) = value match {
      case Some(value) => ResolveCustomerRef(value, path)
      case _           => Valid(None)
    }

    def bodyValueValidator(requestDataBody: CreateAmendDividendsRequestBody): Validated[Seq[MtdError], CreateAmendDividendsRequest] = {

      val foreignDividends: Validated[Seq[MtdError], Unit] =
        requestDataBody.foreignDividend match {
          case Some(dividends) =>
            dividends.zipWithIndex
              .map { case (dividend, index) =>
                List(validateForeignDividend(dividend, index)).sequence
              }
              .sequence
              .andThen(_ => valid)
          case _ => Validated.Valid(None)
        }

      val foreignDividendsWhilstAbroad: Validated[Seq[MtdError], Unit] =
        requestDataBody.dividendIncomeReceivedWhilstAbroad match {
          case Some(items) =>
            items.zipWithIndex
              .map { case (dividend, index) =>
                List(validateDividendIncomeReceivedWhilstAbroad(dividend, index)).sequence
              }
              .sequence
              .andThen(_ => valid)
          case _ => Validated.Valid(None)
        }

      combine(
        foreignDividends,
        foreignDividendsWhilstAbroad,
        validateCommonDividends(requestDataBody.stockDividend, "stockDividend"),
        validateCommonDividends(requestDataBody.redeemableShares, "redeemableShares"),
        validateCommonDividends(requestDataBody.bonusIssuesOfSecurities, "bonusIssuesOfSecurities"),
        validateCommonDividends(requestDataBody.closeCompanyLoansWrittenOff, "closeCompanyLoansWrittenOff")
      ).onSuccess(parsed)

    }

    def validateForeignDividend(foreignDividend: CreateAmendForeignDividendItem, arrayIndex: Int): Validated[Seq[MtdError], Unit] = {
      val countryCode  = ResolveParsedCountryCode(foreignDividend.countryCode, s"/foreignDividend/$arrayIndex/countryCode")
      val amtBeforeTax = resolveOptionalMonetaryValue(foreignDividend.amountBeforeTax, s"/foreignDividend/$arrayIndex/amountBeforeTax")
      val taxTakenOff  = resolveOptionalMonetaryValue(foreignDividend.taxTakenOff, s"/foreignDividend/$arrayIndex/taxTakenOff")
      val specialWithholdingTax =
        resolveOptionalMonetaryValue(foreignDividend.specialWithholdingTax, s"/foreignDividend/$arrayIndex/specialWithholdingTax")
      val taxableAmt = resolveMonetaryValue(foreignDividend.taxableAmount, s"/foreignDividend/$arrayIndex/taxableAmount")
      combine(countryCode, amtBeforeTax, taxTakenOff, specialWithholdingTax, taxableAmt)
    }

    def validateDividendIncomeReceivedWhilstAbroad(dividendIncomeReceivedWhilstAbroad: CreateAmendDividendIncomeReceivedWhilstAbroadItem,
                                                   arrayIndex: Int): Validated[Seq[MtdError], Unit] = {
      combine(
        ResolveParsedCountryCode(dividendIncomeReceivedWhilstAbroad.countryCode, s"/dividendIncomeReceivedWhilstAbroad/$arrayIndex/countryCode"),
        resolveOptionalMonetaryValue(
          dividendIncomeReceivedWhilstAbroad.amountBeforeTax,
          s"/dividendIncomeReceivedWhilstAbroad/$arrayIndex/amountBeforeTax"),
        resolveOptionalMonetaryValue(dividendIncomeReceivedWhilstAbroad.taxTakenOff, s"/dividendIncomeReceivedWhilstAbroad/$arrayIndex/taxTakenOff"),
        resolveOptionalMonetaryValue(
          dividendIncomeReceivedWhilstAbroad.specialWithholdingTax,
          s"/dividendIncomeReceivedWhilstAbroad/$arrayIndex/specialWithholdingTax"),
        resolveMonetaryValue(dividendIncomeReceivedWhilstAbroad.taxableAmount, s"/dividendIncomeReceivedWhilstAbroad/$arrayIndex/taxableAmount")
      )

    }

    def validateCommonDividends(commonDividends: Option[CreateAmendCommonDividends], fieldName: String): Validated[Seq[MtdError], Unit] = {
      commonDividends match {
        case Some(dividends) =>
          combine(
            resolveCustomerRef(dividends.customerReference, s"/$fieldName/customerReference"),
            resolveMonetaryValue(dividends.grossAmount, s"/$fieldName/grossAmount")
          )
        case _ => Validated.Valid(None)
      }

    }

    bodyValueValidator(body).onSuccess(parsed)
  }

}
