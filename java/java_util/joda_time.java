import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

// currentTimestamp是1970-01-01 00:00:00到现在的毫秒数
LOGGER.info("当前时间: {}", DATE_FORMATTER.print(currentTimestamp));

DateTime now = DateTime.now(); // 当前时间

// 跳到下一个时间点，时间点是每个月的某天某时某分
// 因为不知道当前年份和月份，所以有以下计算
int day = 20, hour = 3, minute = 0;
DateTime nextDay = DateTime.now(); // 下一个时间点所在的天
if (nextDay.getDayOfMonth() < day) {
	// 下一个时间点在本月中, 直接加上天数
	nextDay = nextDay.plusDays(day - nextDay.getDayOfMonth());
} else {
	// 下一个时间点在下一个月, 先减掉天数, 再加1个月. 防止从01-30变到02-20出错.
	nextDay = nextDay.minusDays(nextDay.getDayOfMonth() - day).plusMonths(1);
}
nextTime = new DateTime(nextDay.getYear(), nextDay.getMonthOfYear(), nextDay.getDayOfMonth(), hour, minute);