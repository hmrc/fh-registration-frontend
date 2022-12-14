package uk.gov.hmrc.fhregistrationfrontend.utils

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object dateTimeHelper {

  def convertDateToString(date: Instant): String = {
    val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
    dateTimeFormatter.format(date)
  }

  def generateExpiryDate(numberOfDays: Int, date: Long) = {
      Instant.ofEpochMilli(date).plus(numberOfDays, ChronoUnit.DAYS)
  }

}
