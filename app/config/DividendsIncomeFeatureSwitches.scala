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

package config

import play.api.Configuration
import shared.config.{FeatureSwitches, SharedAppConfig}

case class DividendsIncomeFeatureSwitches private(protected val featureSwitchConfig: Configuration) extends FeatureSwitches {

  val isDesIfMigrationEnabled: Boolean = isEnabled("desIf_Migration")
  val isPassDeleteIntentEnabled: Boolean = isEnabled("passDeleteIntentHeader")
}

object DividendsIncomeFeatureSwitches {
  def apply()(implicit appConfig: SharedAppConfig): DividendsIncomeFeatureSwitches = DividendsIncomeFeatureSwitches(appConfig.featureSwitchConfig)
}
