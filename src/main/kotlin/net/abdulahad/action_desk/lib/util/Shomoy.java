package net.abdulahad.action_desk.lib.util;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Shomoy {

	private static SimpleDateFormat formatter;
	private final Calendar cal;

	public Shomoy() {
		cal = Calendar.getInstance();
		cal.clear(Calendar.MILLISECOND);
	}

	public Shomoy(String iso) {
		var formats = new String[]{"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", "HH:mm:ss"};
		LocalDateTime dateTime = null;

		try {
			for (String format : formats) {
				dateTime = LocalDateTime.parse(iso, DateTimeFormatter.ofPattern(format));
				break;
			}
		} catch (Exception ignored) {
		}

		if (dateTime == null) {
			throw new IllegalArgumentException("Invalid ISO format: " + iso);
		}

		cal = Calendar.getInstance();
		cal.clear(Calendar.MILLISECOND);
		
		// noinspection MagicConstant
		cal.set(dateTime.getYear(), dateTime.getMonthValue() - 1, dateTime.getDayOfMonth(),
				dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond());
	}

	private static SimpleDateFormat formater(String pattern) {
		if (formatter == null) {
			formatter = new SimpleDateFormat();
		}

		formatter.applyPattern(pattern);
		return formatter;
	}

	public static Shomoy get() {
		return new Shomoy();
	}

	public static Shomoy get(String iso) {
		return new Shomoy(iso);
	}

	/**
	 * Formats the iso datetime as specified by the format.
	 *
	 * @param format The format literals meaning was adopted from PHP. As
	 *               it is clear to understand and concise. The following symbols
	 *               are allowed:
	 *               <table>
	 *               <tr><td>Symbol</td><td>Meaning</td><td>Symbol</td><td>Meaning</td></tr>
	 *               <tr><td>Y</td><td>Year : 2024</td><td>m</td><td>Month: 02</td></tr>
	 *               <tr><td>d</td><td>Date: 09</td><td>H</td><td>Hour24: 01</td></tr>
	 *               <tr><td>h</td><td>Hour12: 09</td><td>i</td><td>Minute: 05</td></tr>
	 *               <tr><td>s</td><td>Seconds: 13</td><td>D</td><td>Day: Mon</td></tr>
	 *               <tr><td>l</td><td>Day: Monday</td><td>a</td><td>AmPm: am/pm</td></tr>
	 *               <tr><td>A</td><td>AmPm: AM/PM</td><td>F</td><td>Month: January</td></tr>
	 *               <tr><td>M</td><td>Month: Jan</td><td>c</td><td>ISO 8601: 2004-02-12T15:19:21+00:00</td></tr>
	 *               </table>
	 *
	 * @return Formatted date time
	 * */
	public static String format(String iso, String format) {
		return new Shomoy(iso).getInFormat(format);
	}

	public long diff(Shomoy shomoy) {
		return getTimeInMillis() - shomoy.getTimeInMillis();
	}

	public long diff(Shomoy shomoy, boolean inSec) {
		var diff = diff(shomoy);

		if (!inSec) return diff;

		return diff / 1000;
	}

	public boolean past(String period, boolean exact) {
		// Parse the period string to extract time units
		Pattern pattern = Pattern.compile("(\\d+)\\s*(sec|second|min|minute|hour|day|week|month|year)s?");
		Matcher matcher = pattern.matcher(period);

		// Create a Duration and Period to accumulate the time units
		Duration duration = Duration.ZERO;
		Period totalPeriod = Period.ZERO;

		while (matcher.find()) {
			int value = Integer.parseInt(matcher.group(1));
			String unit = matcher.group(2);

			switch (unit) {
				case "sec":
				case "second":
					duration = duration.plusSeconds(value);
					break;
				case "min":
				case "minute":
					duration = duration.plusMinutes(value);
					break;
				case "hour":
					duration = duration.plusHours(value);
					break;
				case "day":
					totalPeriod = totalPeriod.plusDays(value);
					break;
				case "week":
					value *= 7;
					totalPeriod = totalPeriod.plusDays(value);
					break;
				case "month":
					totalPeriod = totalPeriod.plusMonths(value);
					break;
				case "year":
					totalPeriod = totalPeriod.plusYears(value);
					break;
				default:
					throw new IllegalArgumentException("Invalid time unit: " + unit);
			}
		}

		// Add the calculated Duration and Period to the current DateTime
		var timezone = ZoneId.systemDefault();
		var dateTime = LocalDateTime.ofInstant(new Shomoy().toInstant(), timezone).minus(totalPeriod).minus(duration);
		var thisDateTime = LocalDateTime.ofInstant(this.toInstant(), timezone);

		// Compare the modified datetime with the current datetime
		if (exact) {
			return thisDateTime.equals(dateTime);
		} else {
			return thisDateTime.isBefore(dateTime) || thisDateTime.equals(dateTime);
		}
	}

	public boolean pastISO(String iso, boolean exact) {
		var shomoy = new Shomoy(iso);

		int result = this.cal.compareTo(shomoy.cal);

		if (exact) {
			return result == 0;
		}

		return result >= 0;
	}

	public Shomoy add(String period) {
		Pattern pattern = Pattern.compile("(\\d+)\\s*(sec|second|min|minute|hour|day|week|month|year)s?");
		Matcher matcher = pattern.matcher(period);

		while (matcher.find()) {
			int value = Integer.parseInt(matcher.group(1));
			String unit = matcher.group(2);

			switch (unit) {
				case "sec":
				case "second":
					addSec(value);
					break;
				case "min":
				case "minute":
					addMin(value);
					break;
				case "hour":
					addHour(value);
					break;
				case "day":
					addDay(value);
					break;
				case "week":
					value *= 7;
					addDay(value);
					break;
				case "month":
					addMonth(value);
					break;
				case "year":
					addYear(value);
					break;
				default:
					throw new IllegalArgumentException("Invalid time unit: " + unit);
			}
		}

		return this;
	}

	public void addSec(int sec) {
		cal.add(Calendar.SECOND, sec);
	}

	public void addMin(int min) {
		cal.add(Calendar.MINUTE, min);
	}

	public void addHour(int hour) {
		cal.add(Calendar.HOUR_OF_DAY, hour);
	}

	public void addDay(int day) {
		cal.add(Calendar.DAY_OF_MONTH, day);
	}

	public void addMonth(int month) {
		cal.add(Calendar.MONTH, month);
	}

	public void addYear(int year) {
		cal.add(Calendar.YEAR, year);
	}

	public String day(boolean full) {
		var format = full ? "EEEE" : "E";
		return formater(format).format(cal.getTime());
	}

	public String day() {
		return day(false);
	}

	public String monthName(boolean full) {
		var format = full ? "MMMM" : "MMM";
		return formater(format).format(cal.getTime());
	}

	public String monthName() {
		return monthName(false);
	}

	public String sec() {
		return formater("ss").format(cal.getTime());
	}

	public String min() {
		return formater("mm").format(cal.getTime());
	}

	public String hour() {
		return formater("HH").format(cal.getTime());
	}

	public String hour24() {
		return formater("kk").format(cal.getTime());
	}

	public String ampm() {
		return formater("a").format(cal.getTime());
	}

	public String date() {
		return formater("dd").format(cal.getTime());
	}

	public String month() {
		return formater("MM").format(cal.getTime());
	}

	public String year() {
		return formater("yyyy").format(cal.getTime());
	}

	public String iso() {
		return formater("yyyy-MM-dd HH:mm:ss").format(cal.getTime());
	}

	public String isoDate() {
		return formater("yyyy-MM-dd").format(cal.getTime());
	}

	public String isoTime() {
		return formater("HH:mm:ss").format(cal.getTime());
	}

	public static String formatInAgo(int sec, int precision) {
		if (sec == 0) return "< 1 sec";

		String[][] timeFormats = {
				{"86400", "1 day", "days"},
				{"3600", "1 hr", "hrs"},
				{"60", "1 min", "mins"},
				{"1", "1 sec", "secs"}
		};

		List<String> times = new ArrayList<>();

		for (String[] timeFormat : timeFormats) {
			int formatSeconds = Integer.parseInt(timeFormat[0]);
			int unitCount = sec / formatSeconds;

			sec %= formatSeconds;

			if (unitCount == 0) continue;

			if (unitCount == 1) {
				times.add(timeFormat[1]);
			} else {
				times.add(unitCount + " " + timeFormat[2]);
			}

			if (times.size() == precision) break;
		}

		return String.join(", ", times);
	}

	public String formatInAgo(int precision) {
		return formatInAgo(this, precision);
	}

	public String formatInAgo(Shomoy shomoy, int precision) {
		var timeAgo = (int) (Calendar.getInstance().getTimeInMillis() - shomoy.getTimeInMillis());
		return formatInAgo(timeAgo / 1000, precision);
	}

	public static String formatInAgoWithWord(String str) {
		if (str.contains("-")) {
			return "In " + str.replaceAll("-", "");
		} else {
			return str + " ago";
		}
	}

	public Instant toInstant() {
		return cal.getTime().toInstant();
	}

	public Date toDate() {
		return cal.getTime();
	}

	public long getTimeInMillis() {
		return cal.getTimeInMillis();
	}

	/**
	 * Formats the iso datetime as specified by the format.
	 *
	 * @param format The format literals meaning was adopted from PHP. As
	 *               it is clear to understand and concise. The following symbols
	 *               are allowed:
	 *               <table>
	 *               <tr><td>Symbol</td><td>Meaning</td><td>Symbol</td><td>Meaning</td></tr>
	 *               <tr><td>Y</td><td>Year : 2024</td><td>m</td><td>Month: 02</td></tr>
	 *               <tr><td>d</td><td>Date: 09</td><td>H</td><td>Hour24: 01</td></tr>
	 *               <tr><td>h</td><td>Hour12: 09</td><td>i</td><td>Minute: 05</td></tr>
	 *               <tr><td>s</td><td>Seconds: 13</td><td>D</td><td>Day: Mon</td></tr>
	 *               <tr><td>l</td><td>Day: Monday</td><td>a</td><td>AmPm: am/pm</td></tr>
	 *               <tr><td>A</td><td>AmPm: AM/PM</td><td>F</td><td>Month: January</td></tr>
	 *               <tr><td>M</td><td>Month: Jan</td><td>c</td><td>ISO 8601: 2004-02-12T15:19:21+00:00</td></tr>
	 *               </table>
	 *
	 * @return Formatted date time
	 * */
	public String getInFormat(String format) {
		var builder = new StringBuilder();
		var chars = format.toCharArray();

		for (char c : chars) {
			if (c == 'Y')      builder.append(year());
			else if (c == 'm') builder.append(month());
			else if (c == 'd') builder.append(date());
			else if (c == 'H') builder.append(hour24());
			else if (c == 'h') builder.append(hour());
			else if (c == 'i') builder.append(min());
			else if (c == 's') builder.append(sec());
			else if (c == 'D') builder.append(day());
			else if (c == 'l') builder.append(day(true));
			else if (c == 'a') builder.append(ampm());
			else if (c == 'A') builder.append(ampm().toUpperCase());
			else if (c == 'F') builder.append(monthName(true));
			else if (c == 'M') builder.append(monthName());
			else builder.append(c);
		}

		return builder.toString();
	}

	public Calendar getCalendar() {
		return cal;
	}
}
