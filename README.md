# uncheckers

Generates static helper methods that decorate and **uncheck** lambdas and
functional interfaces declared as throwing checked exceptions, wrapping and
rethrowing checked exceptions in unchecked exceptions, in order to make
such lambdas and functional interfaces easier to use in functional pipelines.

Also generates static helper methods that **uncheck and call** such lambdas
and functional interfaces, in order to avoid the boilerplate of handling
checked exceptions even outside of functional pipelines.

### uncheck example

`Uncheckers.uncheckIntFunction` decorates and unchecks an `IntFunction`-like
lambda or functional interface instance declared to throw any `Exception`:

    @FunctionalInterface
    public interface CheckedIntFunction<R> {
        public R apply(int i) throws Exception;
    }

    public static <R> IntFunction<R> uncheckIntFunction(CheckedIntFunction<R> checkedIntFunction) {
        return (i) -> {
            try {
                return checkedIntFunction.apply(i);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

This makes it easy to take advantage of Java's built-in MIDI support without
having to write mountains of boilerplate to assuage the fears of API methods:

    range(0, 16)
            .mapToObj(uncheckIntFunction(c -> Arrays.asList(
                    new ShortMessage(ShortMessage.CONTROL_CHANGE | c, ALL_SOUND_OFF, 0),
                    new ShortMessage(ShortMessage.CONTROL_CHANGE | c, RESET_ALL_CONTROLLERS, 0))))

### callUnchecked example

`IoUncheckers.callUncheckedIoSupplier` unchecks and calls a `Supplier`-like
lambda or functional interface instance declared to throw `IOException`:

    @FunctionalInterface
    public interface CheckedIoSupplier<T> {
        public T get() throws IOException;
    }
    
    public static <T> T callUncheckedIoSupplier(CheckedIoSupplier<T> checkedIoSupplier) {
        try {
            return checkedIoSupplier.get();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

This can make reading or writing files quicker and easier:

    GffFile rgnGff = GffFile.create(callUncheckedIoSupplier(
            () -> Files.readAllBytes(options.rgnGff.get())));

Especially in non-critical and/or experimental projects, this not only
saves time and focus otherwise spent maintaining boilerplate but also
avoids the risk of inadvertently catching and ignoring important errors.

### bases covered

Currently the project generates two classes:

* `IoUncheckers`, which unchecks otherwise functional interfaces throwing
`IOException` by catching and rethrowing in `UncheckedIOException`

* `Uncheckers`, which unchecks otherwise functional interfaces throwing
`Exception` by rethrowing in `RuntimeException`

Each class defines an interface, an `uncheck` static method, and a
`callUnchecked` static method corresponding to each of `Runnable`,
`Comparator`, and the 43 functional interface types of `java.util.function`.

The `Generator` class could also be used to generate interfaces and methods
to handle other exception types and/or other functional interface types.

### Maven artifact

Contains the generated sources. Available from a repository I'm hosting through my Github:

    <repositories>
        <repository>
            <id>johnglassmyer-github-releases</id>
            <url>https://raw.github.com/JohnGlassmyer/maven-repository/master/releases</url>
        </repository>
    </repositories>
    
    <dependencies>
        <dependency>
            <groupId>net.johnglassmyer.uncheckers</groupId>
            <artifactId>uncheckers</artifactId>
            <version>20180805d</version>
        </dependency>
    </dependencies>
