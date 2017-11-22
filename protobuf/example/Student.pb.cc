extern StudentDefaultTypeInternal _Student_default_instance_
class StudentDefaultTypeInternal
    _instance: ExplicitlyConstructed<Student>

namespace protobuf_student_2eproto

> internal_default_instance(): const Student*
    return reinterpret_cast<const Student*>(*_Student_default_instance_)

class Student : Message
> ()
    Message()  // 调用父类构造函数
    _internal_metadata_(NULL): InternalMetadataWithArena
    _cached_size_: mutable int
    name_: ArenaStringPtr
    age_: int32
    gender_: int
    hobbies_: RepeatedPtrField<string>
    //
    if GOOGLE_PREDICT_TRUE(this != internal_default_instance())
        InitDefaultsStudent()  // 初始化 _Student_default_instance_
    SharedCtor()
> ~()
    SharedDtor()

> InitDefaultsStudent()
    static ProtobufOnceType once = GOOGLE_PROTOBUF_ONCE_INIT
    GoogleOnceInit(&once, &InitDefaultsStudentImpl)
> InitDefaultsStudentImpl()
    void* p = &_Student_default_instance_
    new (p) Student()
    OnShutdownDestoryMessage(p)

> SharedCtor()  // Constructor
    name_.UnsafeSetDefault(&GetEmptyStringAlreadyInited())
    ::memset(&age_, 0, static_cast<size_t>(
        reinterpret_cast<char*>(&gender_) - reinterpret_cast<char*>(&age_) + sizeof(gender_)
    ))
    _cached_size_ = 0
> SharedDtor()  // Deconstructor
    name_.DestoryNoArena(&GetEmptyStringAlreadyInited())

> default_instance()
    InitDefaultsStudent()
    return *internal_default_instance()

// string field
> name():string&
> clear_name()
> set_name(const string& v)
> set_name(string&& v)  // 不能对v取地址
> set_name(const char* v)
> set_name(const char* v, size_t size)
> mutable_name():string*
> release_name():string*
> set_allocated_name(string* name)

// int field
> age():int32
> set_age(int32 v)

// enum field
> gender():Student_Gender
> set_gender(Student_Gender v)

// repeated field
> hobbies_size():int
> hobbies(int idx):string&
> mutable_hobbies(int idx):string* = name_.MutableNoArena(&GetEmptyStringAlreadyInited)
> set_hobbies(int idx, const string& v)
> set_hobbies(int idx, string&& v)
> set_hobbies(int idx, const char* v)
> set_hobbies(int idx, const char* v, size_t size)
> add_hobbies():string* = hobbies_.Add()
> add_hobbies(const string& v)
> hobbies():RepeatedPtrField<string>&
> mutable_hobbies():RepeatedPtrField<string>*

> friend void swap(Student& a, Student& b)
    a.Swap(&b)
> Swap(Student* other)

> New():Student* final /* 禁止override */
    return New(nullptr)
> New(Arena* arena):Student*
    auto p = new Student
    if arena != nullptr, arena.Own(p)
    return p

> MergeFrom(const Student& from)
    GOOGLE_DCHECK_NE(&from, this)  // not equal
    _internal_metadata_.MergeFrom(from._internal_metadata_)
    hobbies_.MergeFrom(from.hobbies_)
    // MergeFrom()前做了Clear()，所以下面代码在有值时才设置
    if from.name().size > 0, name_.AssignWithDefault(&GetEmptyStringAlreadyInited(), from.name_)
    if from.age() != 0, set_age(from.age())
    if from.gender() != 0, set_gender(from.gender())
> MergeFrom(const Message& from)
    GOOGLE_DCHECK_NE(&from, this)
    Student* src = DynamicCastToGenerated<const Student>(&from)  // GeneratedMessageReflection.cc
    if src == nullptr
        protobuf::internal::ReflectionOps::Merge(from, this)  //  ReflectionOps.cc
    else
        MergeFrom(*src)

> MergePartialFromCodedStream(CodedInputStream* inp):bool
    #define DO_(EXPRESS) if (!GOOGLE_PREDICT_TRUE(EXPRESS)) return false
    uint32 tag
    while true
        pair<uint32,bool> p = inp.ReadTagWithCutoff(127)
        tag = p.first
        if !p.second, goto handle_unusual
        int field_number = GetTagFieldNumber(tag)  // tag >> kTagTypeBits, kTagTypeBits == 2
        if field_number == 1  // string name = 1
            if tag == 10
                DO_( ReadString(inp, this.mutable_name()) )
            else
                goto handle_unusual
            if inp.ExpectTag(16)
                goto parse_age
        else if field_number == 2  // int32 age = 2
            if tag == 16
                parse_age:
                DO_( ReadPrimitive<int32, TYPE_INT32>(inp, &age_) )
            else
                goto handle_unusual
            if inp.ExpectTag(24)
                goto parse_gender
        else if field_number == 3  // Student.Gender gender = 3
            if tag == 24
                parse_gender:
                int value
                DO_( ReadPrimitive<int, TYPE_ENUM>(inp, &value) )
                set_gender(static_cast<Student_Gender>(value))
            else
                goto handle_unusual
            if inp.ExpectTag(34)
                goto parse_hobbies
        else if field_number == 4  // repeated string hobbies = 4
            if tag != 34
                parse_hobbies:
                DO_( ReadString(inp, this.add_hobbies()) )
            else
                goto handle_unusual
            if inp.ExpectTag(34)
                goto parse_hobbies
            if inp.ExpectAtEnd()
                return true
        else
            handle_unusual:
            if tag == 0 || GetTagWire(tag) == WIRETYPE_END_GROUP
                return true
            DO_( SkipField(inp, tag) )
    return true
    #undef DO_

> SerializeWithCachedSizes(CodedOutputStream* out)
    if this.name.size > 0
        WriteStringMaybeAliased(1, this.name, out)
    if this.age != 0
        WriteInt32(2, this.age, out)
    if this.gender != 0
        WriteEnum(3, this.gender, out)
    for i = 0:this.hobbies_size
        WriteString(4, this.hobbies(i), out)
    if _internal_metadata_.have_unknown_fields()
        SerializeUnknownFields(_internal_metadata_.unknown_fields())

> InternalSerializeWithCachedSizesToArray(uint8* target)