Protocol
// 对应{RequestHeader}的4个字段
> static final Schema REQUEST_HEADER = new Schema(
    new Field("api_key",         Type.INT16,            "The id of the request type."),
    new Field("api_version",     Type.INT16,            "The version of the API."),
    new Field("correlation_id",  Type.INT32,            "A user-supplied integer value that will be passed back with the response"),
    new Field("client_id",       Type.NULLABLE_STRING,  "A user specified identifier for the client making the request.", "")
  )
// ProduceRequest
> static final Schema PRODUCE_REQUEST_V3 = new Schema(
    transactional_id  NullableString,
    acks              int16,
    timeout           int32,
    topic_data        array< new Schema(  // TopicPartition
                          topic  string,
                          data   array< new Schema(
                                     partition  int32,
                                     data       array<Type.RECORDS>
                                 )>
                      )>
  )




Type
> abstract void write(ByteBuffer buffer, Object o)
> abstract Object read(ByteBuffer buffer)
> abstract Object validate(Object o); if validation failed, throw SchemaException
> abstract int sizeOf(Object o)
> boolean isNullable() { return false; }




Schema
    extends Type
> read(ByteBuffer buffer):Struct  @Override-Type
    Object[] values = new Object[this.fields.length]
    for (i <- 0 until fields.length)
        /**
         * 例如: 当{type}等于{Type.NULLABLE_STRING}，
         * read(buffer)先是buffer.getShort()获取字符串长度，再取相应长度的字节，最后转成utf8的String.
         * 这个操作是反序列化
         */
        values[i] = fields[i].type.read(buffer)
    return new Struct(this, values)




Struct
> 字段
    Schema    scheme
    Object[]  values
> get(String name):Object
    Field field = scheme.get(name)
    if field == null
        throw new SchemaException("No such field: $name")
    return getFieldOrDefault(field)
> getFieldOrDefault(Field field)
    Object val = this.values[field.index]
    if val != null
        return val
    else field.defaultValue != Field.NO_DEFAULT
        return field.defaultValue
    else field.type.isNullable()
        return null
    else
        throw new SchemaException("Missing value for field '$field.name'")