package com.jmorla.tradingboot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;


class TradingbootApplicationTests {

	@Test
	void contextLoads() {
		System.out.println(getInitialDelay(18));
	}

	private long getInitialDelay(int startHour) {
		ZoneId zoneId = ZoneId.of("America/New_York");
		ZonedDateTime current = ZonedDateTime.now().withZoneSameInstant(zoneId);
		ZonedDateTime startTime = ZonedDateTime
				.now()
				.withHour(startHour)
				.withMinute(0).withZoneSameInstant(zoneId);

		long timeLeft = current.until(startTime, ChronoUnit.MINUTES);
		return timeLeft > 0 ? timeLeft : 1;
	}

}
