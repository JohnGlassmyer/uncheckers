package net.johnglassmyer.uncheckers.generator;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.reflect.TypeToken;

class Generator {
	private static final String UNCHECKERS_SITE_URL = "http://github.com/JohnGlassmyer/uncheckers";

	public static String generate(
			List<Class<?>> samTypes,
			Class<? extends Exception> checkedExceptionClass,
			Class<? extends Throwable> uncheckedExceptionClass,
			String uncheckersPackageName,
			String uncheckersEnclosingClassName,
			Function<String, String> samTypeNameToCheckedInterfaceName,
			Function<String, String> samTypeNameToUncheckMethodName,
			Function<String, String> samTypeNameToCallUncheckedMethodName) {
		if (isCheckedException(uncheckedExceptionClass)) {
			throw new IllegalArgumentException(String.format(
					"%s is a checked exception type", uncheckedExceptionClass.getName()));
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream printStream = new PrintStream(baos);

		printStream.println(generateHeader(
				uncheckersPackageName,
				checkedExceptionClass,
				uncheckedExceptionClass,
				samTypes));

		printStream.println();

		String enclosingClassJavadoc = generateEnclosingClassJavadoc(
				checkedExceptionClass, uncheckedExceptionClass);
		printStream.println(enclosingClassJavadoc);
		printStream.format("public class %s {\n", uncheckersEnclosingClassName);

		Set<Class<?>> processedSamTypes = new HashSet<>();
		Set<String> processedSamTypeSimpleNames = new HashSet<>();
		for (Class<?> samType : samTypes) {
			if (!processedSamTypes.isEmpty()) {
				printStream.println();
			}

			String samTypeSimpleName = samType.getSimpleName();

			if (processedSamTypeSimpleNames.contains(samTypeSimpleName)) {
				throw new IllegalArgumentException(String.format(
						"the list of SAM types includes more than one with simpleName %s",
						samTypeSimpleName));
			}

			processedSamTypeSimpleNames.add(samTypeSimpleName);

			if (processedSamTypes.contains(samType)) {
				throw new IllegalArgumentException(String.format(
						"the list of SAM types includes %s more than once",
						samType.getName()));
			}

			processedSamTypes.add(samType);

			String samTypeTypeParams =
					samType.getTypeParameters().length > 0
					? "<" + Joiner.on(", ").join(samType.getTypeParameters()) + ">"
					: "";

			TypeToken<?> typeToken = TypeToken.of(samType);

			Method method = extractSingleAbstractMethod(samType);

			findThrownCheckedException(typeToken, method).ifPresent(exceptionClass -> {
					throw new IllegalArgumentException(String.format(
							samType.getName(), method.getName(), exceptionClass.getName()));
			});

			Type methodReturnType = typeToken.resolveType(method.getGenericReturnType()).getType();

			Map<String, String> paramsAndArgNames = toStringParamsAndArgNames(
					Arrays.stream(method.getGenericParameterTypes())
							.map(typeToken::resolveType)
							.collect(Collectors.toList()));

			String joinedMethodParams = paramsAndArgNames.keySet().stream()
					.collect(Collectors.joining(", "));

			String joinedMethodArgs = paramsAndArgNames.values().stream()
					.collect(Collectors.joining(", "));

			String checkedInterfaceName =
					samTypeNameToCheckedInterfaceName.apply(samTypeSimpleName);
			String checkedInterfaceNameWithTypeParams = checkedInterfaceName + samTypeTypeParams;
			String checkedInterfaceInstanceName =
					Character.toLowerCase(checkedInterfaceName.charAt(0))
					+ checkedInterfaceName.substring(1);

			printStream.println(generateCheckedInterface(
					checkedExceptionClass,
					samTypeSimpleName,
					method,
					methodReturnType,
					joinedMethodParams,
					checkedInterfaceNameWithTypeParams));

			printStream.println();

			printStream.println(generateUncheckerMethod(
					checkedExceptionClass,
					uncheckedExceptionClass,
					samTypeNameToUncheckMethodName,
					samType,
					samTypeSimpleName,
					samTypeTypeParams,
					method,
					methodReturnType,
					joinedMethodArgs,
					checkedInterfaceNameWithTypeParams,
					checkedInterfaceInstanceName));

			printStream.println();

			printStream.println(generateCallUncheckedMethod(
					checkedExceptionClass,
					uncheckedExceptionClass,
					samTypeNameToCallUncheckedMethodName,
					samType,
					samTypeSimpleName,
					samTypeTypeParams,
					method,
					methodReturnType,
					joinedMethodParams,
					joinedMethodArgs,
					checkedInterfaceNameWithTypeParams,
					checkedInterfaceInstanceName));
		}

		printStream.println("}");

		try {
			return baos.toString(Charsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			// you got me this time..
			throw new RuntimeException(e);
		}
	}

	private static String generateHeader(
			String uncheckersPackageName,
			Class<? extends Exception> checkedExceptionClass,
			Class<? extends Throwable> uncheckedExceptionClass,
			List<Class<?>> samTypes) {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format(
				"// generated by %s"
				+ "\npackage %s;"
				+ "\n"
				+ "\n// checked exception type"
				+ "\nimport %s;"
				+ "\n"
				+ "\n// unchecked exception type"
				+ "\nimport %s;",
				UNCHECKERS_SITE_URL,
				uncheckersPackageName,
				checkedExceptionClass.getName(),
				uncheckedExceptionClass.getName()));

		builder.append("\n");

		builder.append("\n// SAM types");
		for (Class<?> clazz : samTypes) {
			builder.append(String.format("\nimport %s;", clazz.getName()));
		}

		return builder.toString();
	}

	private static String generateEnclosingClassJavadoc(
			Class<? extends Exception> checkedExceptionClass,
			Class<? extends Throwable> uncheckedExceptionClass) {
		return String.format(
				"/**"
				+ "\n * Static helper methods which wrap and re-throw"
				+ "\n * {@link %s %s}"
				+ "\n * in {@link %s %s}"
				+ "\n * so that methods known to throw {@code %s}"
				+ "\n * can be more easily called in functional contexts,"
				+ "\n * for example with {@link java.util.stream.Stream Streams}."
				+ "\n *"
				+ "\n * @see <a href=\"%s\""
				+ "\n * >%s</a>"
				+ "\n */",
				checkedExceptionClass.getName(),
				checkedExceptionClass.getSimpleName(),
				uncheckedExceptionClass.getName(),
				uncheckedExceptionClass.getSimpleName(),
				checkedExceptionClass.getSimpleName(),
				UNCHECKERS_SITE_URL,
				UNCHECKERS_SITE_URL);
	}

	private static Method extractSingleAbstractMethod(Class<?> samType) {
		List<Method> methods = Arrays.asList(samType.getMethods()).stream()
				.filter(m -> Modifier.isAbstract(m.getModifiers()))
				.filter(m -> !isMethodOfObject(m))
				.collect(Collectors.toList());

		if (methods.size() != 1) {
			throw new RuntimeException(String.format(
					"%s has %d abstract methods: %s",
					samType.getName(),
					methods.size(),
					methods.stream().map(m -> m.getName()).collect(Collectors.toList())));
		}

		return methods.get(0);
	}

	private static boolean isMethodOfObject(Method m) {
		for (Method objectMethod : Object.class.getDeclaredMethods()) {
			if (m.getName().equals(objectMethod.getName())
					&& Arrays.equals(m.getParameterTypes(), objectMethod.getParameterTypes())) {
				return true;
			}
		}

		return false;
	}

	private static Optional<? extends Class<?>> findThrownCheckedException(
			TypeToken<?> typeToken, Method method) {
		return Arrays.stream(method.getGenericExceptionTypes())
				.map(typeToken::resolveType)
				.map(TypeToken::getRawType)
				.filter(Generator::isCheckedException)
				.findFirst();
	}

	private static Map<String, String> toStringParamsAndArgNames(
			List<TypeToken<?>> methodParameterTypes) {
		Map<String, String> paramsAndArgNames = new LinkedHashMap<>();

		Map<TypeToken<?>, Long> methodParamTypeCounts = methodParameterTypes.stream()
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

		Map<String, Integer> countByName = new HashMap<>();
		methodParameterTypes.stream()
				.forEachOrdered(t -> {
					String typeName = t.getType().getTypeName();
					String paramName = paramNameForTypeName(typeName);
					String dedupedParamName;
					if (methodParamTypeCounts.get(t) == 1) {
						dedupedParamName = paramName;
					} else {
						int count = countByName.compute(
								paramName, (n, c) -> (c == null) ? 0 : (c + 1));
						dedupedParamName = paramName + count;
					}

					paramsAndArgNames.put(typeName + " " + dedupedParamName, dedupedParamName);
				});

		return paramsAndArgNames;
	}

	private static String generateCheckedInterface(
			Class<? extends Exception> checkedExceptionClass,
			String samTypeSimpleName,
			Method method,
			Type methodReturnType,
			String joinedMethodParams,
			String checkedInterfaceNameWithTypeParams) {
		String checkedInterfaceJavadoc = String.format(
				"\t/**"
				+ "\n\t * A lambda or functional interface"
				+ "\n\t * known to throw {@code %s}"
				+ "\n\t * but otherwise convertible to {@code %s}."
				+ "\n\t */",
				checkedExceptionClass.getSimpleName(),
				samTypeSimpleName);

		return String.format(
				"%s"
						+ "\n\t@FunctionalInterface"
						+ "\n\tpublic interface %s {"
						+ "\n\t\tpublic %s %s(%s) throws %s;"
						+ "\n\t}",
				checkedInterfaceJavadoc,
				checkedInterfaceNameWithTypeParams,
				methodReturnType,
				method.getName(),
				joinedMethodParams,
				checkedExceptionClass.getSimpleName());
	}

	private static String generateUncheckerMethod(
			Class<? extends Exception> checkedExceptionClass,
			Class<? extends Throwable> uncheckedExceptionClass,
			Function<String, String> samTypeNameToUncheckMethodName,
			Class<?> samType,
			String samTypeSimpleName,
			String samTypeTypeParams,
			Method method,
			Type methodReturnType,
			String joinedMethodArgs,
			String checkedInterfaceNameWithTypeParams,
			String checkedInterfaceInstanceName) {
		String uncheckMethodName =
				samTypeNameToUncheckMethodName.apply(samTypeSimpleName);

		String uncheckMethodJavadoc = String.format(
				"\t/**"
				+ "\n\t * Decorates the given {@link %s %s}-like"
				+ "\n\t * lambda or functional interface instance"
				+ "\n\t * with a {@code %s} that wraps and re-throws"
				+ "\n\t * any thrown {@code %s}"
				+ "\n\t * in a new {@code %s}."
				+ "\n\t */",
				samType.getName(),
				samTypeSimpleName,
				samTypeSimpleName,
				checkedExceptionClass.getSimpleName(),
				uncheckedExceptionClass.getSimpleName());

		return String.format(
				"%s"
						+ "\n\tpublic static %s%s %s(%s %s) {"
						+ "\n\t\treturn (%s) -> {"
						+ "\n\t\t\ttry {"
						+ "\n\t\t\t\t%s%s.%s(%s);"
						+ "\n\t\t\t} catch (%s e) {"
						+ "\n\t\t\t\tthrow new %s(e);"
						+ "\n\t\t\t}"
						+ "\n\t\t};"
						+ "\n\t}",
				uncheckMethodJavadoc,
				samTypeTypeParams + (samTypeTypeParams.isEmpty() ? "" : " "),
				samTypeSimpleName + samTypeTypeParams,
				uncheckMethodName,
				checkedInterfaceNameWithTypeParams,
				checkedInterfaceInstanceName,
				joinedMethodArgs,
				methodReturnType.equals(Void.TYPE) ? "" : "return ",
				checkedInterfaceInstanceName,
				method.getName(),
				joinedMethodArgs,
				checkedExceptionClass.getSimpleName(),
				uncheckedExceptionClass.getSimpleName());
	}

	private static String generateCallUncheckedMethod(
			Class<? extends Exception> checkedExceptionClass,
			Class<? extends Throwable> uncheckedExceptionClass,
			Function<String, String> samTypeNameToCallUncheckedMethodName,
			Class<?> samType,
			String samTypeSimpleName,
			String samTypeTypeParams,
			Method method,
			Type methodReturnType,
			String joinedMethodParams,
			String joinedMethodArgs,
			String checkedInterfaceNameWithTypeParams,
			String checkedInterfaceInstanceName) {
		String callUncheckedMethodName =
				samTypeNameToCallUncheckedMethodName.apply(samTypeSimpleName);

		String callUncheckedMethodJavadoc = String.format(
				"\t/**"
				+ "\n\t * Calls the given {@link %s %s}-like"
				+ "\n\t * lambda or functional interface instance,"
				+ "\n\t * wrapping and re-throwing any thrown {@code %s}"
				+ "\n\t * in a new {@code %s}."
				+ "\n\t */",
				samType.getName(),
				samTypeSimpleName,
				checkedExceptionClass.getSimpleName(),
				uncheckedExceptionClass.getSimpleName());

		return String.format(
				"%s"
						+ "\n\tpublic static %s%s %s(%s %s%s) {"
						+ "\n\t\ttry {"
						+ "\n\t\t\t%s%s.%s(%s);"
						+ "\n\t\t} catch (%s e) {"
						+ "\n\t\t\tthrow new %s(e);"
						+ "\n\t\t}"
						+ "\n\t}",
				callUncheckedMethodJavadoc,
				samTypeTypeParams + (samTypeTypeParams.isEmpty() ? "" : " "),
				methodReturnType,
				callUncheckedMethodName,
				checkedInterfaceNameWithTypeParams,
				checkedInterfaceInstanceName,
				(joinedMethodParams.isEmpty() ? "" : ", ") + joinedMethodParams,
				methodReturnType.equals(Void.TYPE) ? "" : "return ",
				checkedInterfaceInstanceName,
				method.getName(),
				joinedMethodArgs,
				checkedExceptionClass.getSimpleName(),
				uncheckedExceptionClass.getSimpleName());
	}

	private static boolean isCheckedException(Class<?> clazz) {
		return Exception.class.isAssignableFrom(clazz)
				&& !RuntimeException.class.isAssignableFrom(clazz);
	}

	private static String paramNameForTypeName(String typeName) {
		return typeName.equals(typeName.toUpperCase())
				? typeName.toLowerCase()
				: typeName.substring(0, 1);
	}
}
