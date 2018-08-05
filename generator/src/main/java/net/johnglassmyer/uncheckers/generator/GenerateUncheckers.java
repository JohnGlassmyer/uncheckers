package net.johnglassmyer.uncheckers.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.google.common.base.Charsets;

public class GenerateUncheckers {
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			throw new RuntimeException("must specify output filename as argument");
		}
		Path outputPath = Paths.get(args[0]);

		Class<? extends Exception> checkedExceptionClass = Exception.class;
		Class<? extends RuntimeException> uncheckedExceptionClass = RuntimeException.class;

		String uncheckersPackageName = "net.johnglassmyer.uncheckers";
		String uncheckersEnclosingClassName = "Uncheckers";
		String checkedInterfaceNamePrefix = "Checked";
		String checkedIntefaceNameSuffix = "";
		String uncheckerMethodNamePrefix = "uncheck";
		String uncheckerMethodNameSuffix = "";

		String generatedSource = Generator.generate(
				SamTypes.STANDARD_SAM_TYPES,
				checkedExceptionClass,
				uncheckedExceptionClass,
				uncheckersPackageName,
				uncheckersEnclosingClassName,
				checkedInterfaceNamePrefix,
				checkedIntefaceNameSuffix,
				uncheckerMethodNamePrefix,
				uncheckerMethodNameSuffix);

		Files.createDirectories(outputPath.getParent());
		Files.write(outputPath, generatedSource.getBytes(Charsets.UTF_8));
	}
}
