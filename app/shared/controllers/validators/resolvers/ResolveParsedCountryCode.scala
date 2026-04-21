package shared.controllers.validators.resolvers

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import shared.models.errors.{CountryCodeFormatError, MtdError, RuleCountryCodeError}

case class ResolveParsedCountryCode(path: String) {

  def apply(value: String): Validated[List[MtdError], String] = {
    if (value.length != 3) {
      Invalid(List(CountryCodeFormatError.withPath(path)))
    } else if (ResolveParsedCountryCode.permittedCodes.contains(value)) {
      Valid(value)
    } else {
      Invalid(List(RuleCountryCodeError.withPath(path)))
    }
  }

  def apply(maybeValue: Option[String]): Validated[List[MtdError], Option[String]] = {
    maybeValue match {
      case Some(value) => apply(value).map(Some(_))
      case None        => Valid(None)
    }
  }
}

object ResolveParsedCountryCode {

  val permittedCodes: Set[String] = Set(
    "AFG","ALB","DZA","ASM","AND","AGO","AIA","ATG","ARG","ARM","ABW","AUS","AUT","AZE",
    "BHS","BHR","BGD","BRB","BLR","BEL","BLZ","BEN","BMU","BTN","BOL","BES","BIH","BWA",
    "BRA","VGB","BRN","BGR","BFA","MMR","BDI","KHM","CMR","CAN","CPV","CYM","CAF","TCD",
    "CHL","CHN","CXR","CCK","COL","COM","COG","COK","CRI","CIV","HRV","CUB","CUW","CYP",
    "CZE","COD","DNK","DJI","DMA","DOM","ECU","EGY","SLV","GNQ","ERI","EST","ETH","FLK",
    "FRO","FJI","FIN","FRA","GUF","PYF","GAB","GMB","GEO","DEU","GHA","GIB","GRC","GRL",
    "GRD","GLP","GUM","GTM","GGY","GIN","GNB","GUY","HTI","HND","HKG","HUN","ISL","IND",
    "IDN","IRN","IRQ","IRL","IMN","ISR","ITA","JAM","JPN","JEY","JOR","KAZ","KEN","KIR",
    "XKX","KWT","KGZ","LAO","LVA","LBN","LSO","LBR","LBY","LIE","LTU","LUX","MAC","MKD",
    "MDG","MWI","MYS","MDV","MLI","MLT","MHL","MTQ","MRT","MUS","MYT","MEX","FSM","MDA",
    "MCO","MNG","MNE","MSR","MAR","MOZ","NAM","NRU","NPL","NLD","NCL","NZL","NIC","NER",
    "NGA","NIU","NFK","PRK","MNP","NOR","OMN","PAK","PLW","PAN","PNG","PRY","PER","PHL",
    "PCN","POL","PRT","PRI","QAT","REU","ROU","RUS","RWA","SHN","KNA","LCA","SPM","VCT",
    "WSM","SMR","STP","SAU","SEN","SRB","SYC","SLE","SGP","SXM","SVK","SVN","SLB","SOM",
    "ZAF","KOR","SSD","ESP","LKA","SDN","SUR","SJM","SWZ","SWE","CHE","SYR","TWN","TJK",
    "TZA","THA","TLS","TGO","TKL","TON","TTO","TUN","TUR","TKM","TCA","TUV","UGA","UKR",
    "ARE","USA","VIR","URY","UZB","VUT","VAT","VEN","VNM","WLF","YEM","ZMB","ZWE","ZZZ"
  )

  def apply(value: String, path: String): Validated[Seq[MtdError], String] =
    ResolveParsedCountryCode(path)(value)

  def apply(maybeValue: Option[String], path: String): Validated[Seq[MtdError], Option[String]] =
    ResolveParsedCountryCode(path)(maybeValue)
}
