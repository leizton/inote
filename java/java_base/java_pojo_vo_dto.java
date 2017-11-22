POJO  plain ordinary java object    符合JavaBean规范的普通对象

// 持久层
PO    persistant object      持久对象，对应数据库表的一行记录

// 业务层
VO    value object           值对象，业务层内传递的数据，包含PO
BO    business object        业务对象，领域模型中的领域对象

// controller层
DTO   data transfer object   数据传输对象，controller与外部交互的对象，
                             从service层获取的po、vo无需全部返回给调用者，这时可以转成dto