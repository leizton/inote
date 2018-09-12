/* lambda表达式 */
lambda表达式是用匿名内部类实现函数式接口的简写
函数式接口(@FunctionalInterface)是只有一个抽象方法的接口

/* 方法引用 */
方法引用，"类名::方法名"，是Lambda的简写
如 Apple::getWeight() 是 (Apple a) -> a.getWeight() 的简写，函数描述符是(Apple)->Integer

/* 常用函数式接口 */
Predicate		(T) -> boolean
Consumer		(T) -> void
Function		(T) -> R
Supplier		()  -> T
UnaryOperator	(T) -> T    // 一元运算符
BinaryOperator	(T,T) -> T  // 二元运算符
BiPredicate		(T,U) -> boolean
BiConsumer		(T,U) -> void
BiFunction		(T,U) -> R

/* 比较器 */
interface Comparator<T> {
	comparing(Function<? extends T, ? extends U> keyExtractor):Comparator<T> static {
		// keyExtractor 是从 T实例 中提取用于比较的 U实例 的lambda
		Object.requireNonNull(keyExtractor)
		return (Comparator<T> & Serializable)
			   (T o1, T o2) -> keyExtractor.apply(o1).compareTo(keyExtractor.apply(o2))
	}

	comparing(Function<? extends T, ? extends U> keyExtractor,
			  Comparator<? extends U> keyComparator):Comparator<T> static;  // 指定key的比较器

	reversed():Comparator<T> default {  // default表示接口提供了默认实现
		Collections.reverseOrder(this)
	}
}

/* 流的常用中间节点和终端节点 */
中间节点:
	filter, map, flatMap,
	limit, skip, sorted, distinct, average(IntStream)

终端节点:
	forEach, count, collect,
	anyMatch(流中任一元素匹配上条件), allMatch, noneMatch,
	findFirst, findAny,
	reduce(归约, 用于累和累积等, 也可以计算最大值最小值), max, min

中间节点是后续的一个新流节点，终端节点让整个流运行计算。


/* 扁平化流，flatMap，把多个流扁平(合并)成一个流，输入是多个同类型流，输出是该类型的一个流 */
lstA = [1, 3, 5];  lstB = [2, 4];
// pairs = [ [1,2], [1,4], [3,2], [3,4], [5,2], [5,4] ]
List<int[]> pairs =
		lstA.stream()
		.flatMap(
			(int a) ->
				// 每次map()返回Stream<int[]>，需要通过flatMap()把多个流扁平成一个流
				lstB.stream().map( (int b) -> new int[] {a, b} )
		)
		.collection(Collections.toList());

/* reduce */
reduce包含2个参数:  初始值  BinaryOperator的lambda
reduce语法格式:
	reduce( 初始值, (状态值, 上一个流节点输出值) -> { return 本流节点输出值; } )
例子:
List<Integer> box = Lists.newArrayList(1, 2, 3, 4, 5, 6);
int sum = box.stream()
	.reduce(0, (Integer sumTemp, Integer v) -> sumTemp + v);

/* java.util.stream.Collectors 提供的常用归约方法 */
toList maxBy summingInt groupingBy reducing(通用)

Stream.collect(Collector<? super T, A, R> collector):R
	T是流的类型Stream<T>，A是归约的中间类型，R是输出类型

Stream.collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner):R

例子:
List<Integer> box = Lists.newArrayList(1, 2, 3, 4, 5, 6);
Comparator<Integer> intComp = Comparator.comparingInt(Integer::intValue);
// Collectors.maxBy()
Optional<Integer> max = box.stream().collect(Collectors.maxBy(intComp));
// Collectors.summingInt()
int sum = box.stream().collect(Collectors.summingInt(Integer::intValue));

Collectors.reducing(U init, BiFunction<T, U> mapper, BinaryOperator<U> accum)
	T是输入类型，U是输出类型，init是初始值，mapper是转换函数，accum是累积函数

// maxBy() 由 reducing() 实现
Collectors.maxBy(Comparator<T> comparator):Collector<T, ?, Optional<T>> {
	BinaryOperator<U> accum = BinaryOperator.maxBy(comparator)
	return reducing(accum)
}


/* 自定义收集器Collector */
按是否是质数对list分组
public class PrimeGroupBy implements Collector<Integer, Map<Boolean, List<Integer>>, Map<Boolean, List<Integer>>) {
	// 收集类型A 和 输出类型R 都是 Map<Boolean, List<Integer>>

	@Override  // 供应源, 提供收集类型A的对象
	public Supplier<Map<Boolean, List<Integer>> supplier() {
		return () -> new HashMap<Boolean, List<Integer>>() {{
			put(true, new ArrayList<Integer>());
			put(true, new ArrayList<Integer>());
		}};
	}

	@Override  // 累加器, BiConsumer的2个入参: 收集类型A 和 输入类型T
	public BiConsumer<Map<Boolean, List<Integer>>, Integer> accumulator() {
		return (Map<Boolean, List<Integer>> state, Integer input) ->
			state.get(isPrime(input)).add(input);  // isPrime()判断是否是质数
	}

	@Override  // 归约器, BinaryOperator: 2个收集类型A的入参, 返回输出类型R
	public BinaryOperator<Map<Boolean, List<Integer>>, Map<Boolean, List<Integer>>> combiner() {
		return (Map<Boolean, List<Integer>> map1, Map<Boolean, List<Integer>> map2) -> {
			map1.get(true).addAll(map2.get(true));
			map1.get(false).addAll(map2.get(false));
			return map1;
		};
	}

	@Override  // 输出变换器(transform), 对输出结果(R的对象)作转换
	public Function<Map<Boolean, List<Integer>>, Map<Boolean, List<Integer>>> finisher() {
		return Function.identity();  // 恒等变换
	}

	@Override
	public Set<Characteristics> characteristics() {
		return Collections.unmodifiableSet(Enum.of(IDENTITY_FINISH));
	}
}

// [1, 100]的质数, boxed()装箱: 把int变成Integer
List<Integer> primers = IntStream.rangeClosed(1, 100).boxed().collect(new PrimeGroupBy());

// 用lambda
List<Integer> primers = IntStream.rangeClosed(1, 100).boxed()
	.collect(
		() -> new HashMap<Boolean, List<Integer>> {{ /* ... */ }},  // supplier
		(state, input) -> state.get(isPrime(input)).add(input),     // accumulator
		(map1, map2) -> { /* ... */ }                               // combiner
	);