class Foo {
	private static final Logger LOGGER = LoggerFactory.getLogger(Foo.class);
	static {
		LOGGER.info("static_code_block: {}", "Foo.class");  // {}是占位符
		LOGGER.error("", "{}, {}", "1", 2);
		LOGGER.error(e.getMessage(), e);
	}
}