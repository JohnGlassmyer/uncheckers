package net.johnglassmyer.uncheckers.generator;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleToIntFunction;
import java.util.function.DoubleToLongFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;
import java.util.function.LongSupplier;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.ObjLongConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongBiFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;
import java.util.function.LongPredicate;

import com.google.common.collect.ImmutableList;

class SamTypes {
	static final List<Class<?>> STANDARD_SAM_TYPES = ImmutableList.<Class<?>>builder()
			.add(Runnable.class)
			.add(Comparator.class)
			.add(
					BiConsumer.class,
					BiFunction.class,
					BinaryOperator.class,
					BiPredicate.class,
					BooleanSupplier.class,
					Consumer.class,
					DoubleBinaryOperator.class,
					DoubleConsumer.class,
					DoubleFunction.class,
					DoublePredicate.class,
					DoubleSupplier.class,
					DoubleToIntFunction.class,
					DoubleToLongFunction.class,
					DoubleUnaryOperator.class,
					Function.class,
					IntBinaryOperator.class,
					IntConsumer.class,
					IntFunction.class,
					IntPredicate.class,
					IntSupplier.class,
					IntToDoubleFunction.class,
					IntToLongFunction.class,
					IntUnaryOperator.class,
					LongBinaryOperator.class,
					LongConsumer.class,
					LongFunction.class,
					LongPredicate.class,
					LongSupplier.class,
					LongToDoubleFunction.class,
					LongToIntFunction.class,
					LongUnaryOperator.class,
					ObjDoubleConsumer.class,
					ObjIntConsumer.class,
					ObjLongConsumer.class,
					Predicate.class,
					Supplier.class,
					ToDoubleBiFunction.class,
					ToDoubleFunction.class,
					ToIntBiFunction.class,
					ToIntFunction.class,
					ToLongBiFunction.class,
					ToLongFunction.class,
					UnaryOperator.class)
			.build();
}
