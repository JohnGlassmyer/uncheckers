package net.johnglassmyer.uncheckers.generator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import com.google.common.base.Charsets;

public class GenerateIoUncheckers {
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			throw new RuntimeException("must specify output filename as argument");
		}
		Path outputPath = Paths.get(args[0]);

		Class<? extends Exception> checkedExceptionClass = IOException.class;
		Class<? extends RuntimeException> uncheckedExceptionClass = UncheckedIOException.class;

		String uncheckersPackageName = "net.johnglassmyer.uncheckers";
		String uncheckersEnclosingClassName = "IoUncheckers";

		Function<String, String> samTypeNameToCheckedInterfaceName =
				name -> String.format("CheckedIo%s", name);

		Function<String, String> samTypeNameToUncheckMethodName =
				name -> String.format("uncheckIo%s", name);

		Function<String, String> samTypeNameToCallUncheckedMethodName =
				name -> String.format("callUncheckedIo%s", name);

		String generatedSource = Generator.generate(
				SamTypes.STANDARD_SAM_TYPES,
				checkedExceptionClass,
				uncheckedExceptionClass,
				uncheckersPackageName,
				uncheckersEnclosingClassName,
				samTypeNameToCheckedInterfaceName,
				samTypeNameToUncheckMethodName,
				samTypeNameToCallUncheckedMethodName);

		Files.createDirectories(outputPath.getParent());
		Files.write(outputPath, generatedSource.getBytes(Charsets.UTF_8));
	}
}
