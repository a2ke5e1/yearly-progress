package com.a3.yearlyprogess.data.models

data class SunriseSunsetResponse(val results: List<Result>, val status: String) {
  fun getStartAndEndTime(dayLight: Boolean): Pair<Long, Long> {
    var startTime = 0L
    var endTime = 0L

    if (dayLight) {
      startTime = this.results[1].getFirstLight().time
      endTime = this.results[1].getLastLight().time
      return Pair(startTime, endTime)
    }

    if (System.currentTimeMillis() < this.results[1].getSunset().time) {
      startTime = this.results[0].getSunset().time
      endTime = this.results[1].getSunrise().time
      return Pair(startTime, endTime)
    }

    startTime = this.results[1].getSunset().time
    endTime = this.results[2].getSunrise().time

    return Pair(startTime, endTime)
  }
}
