jdk的不可变集合
    Collections.unmodifialbeCollection(Collections<? extends T> c):Collection<T>
    Collections.unmodifialbeList(List<? extends T> c):List<T>
    unmodifialbeMap/Set.
jdk的UnmodifiableXXX只是包装原集合的get/set/add等方法, 在set/add方法中抛异常, 并没有复制原集合的元素.
这样做的问题是对原集合修改后会使不可变集合也改变, 如下:
    List<String> srcList = Arrays.asList("a", "b");
    List<String> unmodList = Collections.unmodifialbeList(srcList);
    LOGGER.info("{}", unmodList);  // [a, b]
    srcList.add("c");
    LOGGER.info("{}", unmodList);  // [a, b, c]


Guava的ImmutableList接口
  // copyOf()和of()都不允许有null元素, 否则抛出NullPointerException
  public static <E> ImmutableList<E> copyOf(Collection<? extends E> elements) {
    if (elements instanceof ImmutableCollection) {
      @SuppressWarnings("unchecked") // all supported methods are covariant
      ImmutableList<E> list = ((ImmutableCollection<E>) elements).asList();
      return list.isPartialView() ? ImmutableList.<E>asImmutableList(list.toArray()) : list;
    }
    return construct(elements.toArray());  // elements = arraysCopyOf(elements, length);
  }
  static <T> T[] arraysCopyOf(T[] original, int newLength) {
    T[] copy = newArray(original, newLength);
    System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));  // 引用copy
    return copy;
  }
  // 3.0开始的of()使用了可变参数
  public static <E> ImmutableList<E> of(
      E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9, E e10, E e11, E e12, E... others)