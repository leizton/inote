// 日期的Json序列化
public class DateJsonSerializer extends JsonSerializer<Date> {
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");
	
	@Override
	public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
		jgen.writeString(DATE_FORMATTER.print(value.getTime()));
	}
}

// 注解@JsonSerialize作用在字段或者getter方法上.
public class UserInfo {
	private String username;
	
	@JsonSerialize(using = DateJsonSerializer.class)
	private Date birthday;
	
	/* getter and setter */
}