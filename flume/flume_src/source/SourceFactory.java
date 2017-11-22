SourceFactory interface
> create(String sourceName, String type):Source
> getClass(String type):Class<? extends Source>


DefaultSourceFactory
	implements SourceFactory
> create(String sourceName, String type):Source
	Class<? extends Source> sourceClass = this.getClass(type)
	Source source = sourceClass.newInstance()
	source.setName(sourceName)
	return source
> getClass(String type):Class
	// 枚举SourceType, 如SEQ("org.apache.flume.source.SequenceGeneratorSource")
	// SourceType的sourceClassName字段是类名
	SourceType srcType = SourceType.valueOf( type.toUpperCase() )
	return Class.forName( srcType.getSourceClassName() )