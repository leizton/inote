> Merge(Message& from, Message* to)
    Reflection* from_refl = from.GetReflection
    Reflection* to_refl = to.GetReflection
    //
    vector<FieldDescriptor*> fields
    from_refl.ListFields(from, &fields)
    for auto field : fields
        if field.is_repeated
            for j = 0 : from_refl.FieldSize(from, field)
                switch field.cpp_type
                    case FieldDescriptor::CPPTYPE_INT32:
                        to_refl.AddInt32(to, field, from_refl.GetRepeatedInt32(from, field, j))
                        break
                    case INT64, UINT32, UINT64, FLOAT, DOUBLE, BOOL, STRING, ENUM
                    case FieldDescriptor::CPPTYPE_MESSAGE:
                        to_refl.AddMessage(to, field).MergeFrom(from_refl.GetRepeatedMessage(from, field, j))
        else
            switch field.cpp_type
                case FieldDescriptor::CPPTYPE_INT32:
                    to_refl.SetInt32(to, field, from_refl.GetInt32(from, field))
                    break
                case INT64, UINT32, UINT64, FLOAT, DOUBLE, BOOL, STRING, ENUM
                case FieldDescriptor::CPPTYPE_MESSAGE
    // end for
    to_refl.MutableUnknownFields(to).MergeFrom(from_refl.MutableUnknownFields(to))