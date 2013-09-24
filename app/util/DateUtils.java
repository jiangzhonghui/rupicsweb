package util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

	public final static SimpleDateFormat formatter = new SimpleDateFormat(
			"yyyy-MM-dd");

	public static Date getDateOfNextWeek() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.WEEK_OF_YEAR, 1);
		return cal.getTime();
	}

	public static Date addDateDays(Date date, int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_YEAR, days);
		return cal.getTime();
	}

	public static String formatDate(Date date) {
		DateFormat formatter = SimpleDateFormat.getDateInstance();
		return formatter.format(date);
	}

	public static boolean isBetweenDate(Date objDate, Date startDate,
			Date endDate) {
		long obj = Long.valueOf(formatter.format(objDate));
		long obj1 = Long.valueOf(formatter.format(startDate));
		long obj2 = Long.valueOf(formatter.format(endDate));
		return obj >= obj1 && obj <= obj2;
	}

	public static Date getDateWithoutTime(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
				cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		return cal.getTime();
	}

	public static int getWeekday(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_WEEK);
	}
	
	public static String getWeekofDay(String date) {
		SimpleDateFormat lformatter = new SimpleDateFormat(
				"EE");
		return lformatter.format(defaultParse(date));
	}

	public static String defaultFormat(Date date) {
		return formatter.format(date);
	}

	public static Date defaultParse(String str) {
		try {
			return formatter.parse(str);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static boolean beforeToday(String str) {
		Date date = defaultParse(str);
		Date now = defaultParse(defaultFormat(new Date()));
		return now.after(date);
	}

	public static String convertDate2MMdd(String dateStr, boolean includeMonth) {
		Date date = defaultParse(dateStr);
		date = addDateDays(date, 1);
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		if (includeMonth || cal.get(Calendar.DAY_OF_MONTH) == 1) {
			return dateStr.substring(4, 6) + "-" + dateStr.substring(6);
		} else {
			return dateStr.substring(6);
		}
	}

	public static Calendar getTimeForAlarm(String alarmTime) {

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());

		String[] time = alarmTime.split(":");

		if (time.length == 2) {

			Integer hour = Integer.valueOf(time[0]);
			Integer min = Integer.valueOf(time[1]);

			return getTimeForAlarm(hour, min);
		} else {
			return null;
		}
	}

	public static Calendar getTimeForAlarm(int hour, int min) {

		Calendar cal = Calendar.getInstance();
		if(cal.get(Calendar.HOUR_OF_DAY) > hour || (cal.get(Calendar.HOUR_OF_DAY) == hour && cal.get(Calendar.HOUR_OF_DAY) >= min)){
			return null;
		}
		

		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, min);
		cal.set(Calendar.SECOND, 0);

		return cal;
	}
	
	public static Calendar calculateTimeForAlarm(int hour, int min) {

		Calendar cal = Calendar.getInstance();
//		if(cal.get(Calendar.HOUR_OF_DAY) > hour || (cal.get(Calendar.HOUR_OF_DAY) == hour && cal.get(Calendar.HOUR_OF_DAY) >= min)){
//			cal.setTime(addDateDays(cal.getTime(), 1));
//		}
		

		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, min);
		cal.set(Calendar.SECOND, 0);

		return cal;
	}
	
	public static String formatHourMinute(Date date){
		SimpleDateFormat lformatter = new SimpleDateFormat(
				"HH:mm");
		return lformatter.format(date);
	}
	public static String getMonthDay(String str, boolean linebreak){
		Date date = defaultParse(str);
		String format = "";
		if(linebreak){
			format = "MM\n月\ndd\n日";
		}else{
			format = "MM月dd日";
		}
		SimpleDateFormat lformatter = new SimpleDateFormat(
				format);
		return lformatter.format(date);
	}
}
