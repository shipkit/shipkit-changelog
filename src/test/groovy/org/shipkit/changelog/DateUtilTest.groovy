package org.shipkit.changelog

import spock.lang.Specification

import static org.shipkit.changelog.DateUtil.*

class DateUtilTest extends Specification {

    def "parses UTC date"() {
        //Using any date to assert the logic:
        def date = new Date(1593930840832)

        expect:
        formatDate(date) == "2020-07-05"
        formatDateToLocalTime(date, TimeZone.getTimeZone("GMT")) == "2020-07-05 06:34 AM GMT"
        parseDateInEpochSeconds("1593930840") == new Date(1593930840000)
    }
}
