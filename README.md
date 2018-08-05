# uncheckers

Generates static helper methods that make methods throwing checked exceptions
easier to use in functional contexts by wrapping and rethrowing checked
exceptions in unchecked exceptions.

### example

`IoUncheckers.uncheckFunctionIo` unchecks a lambda or an instance of a
functional interface declared to throw `IOException`:

    public interface CheckedIoFunction<T, R> {
        public R apply(T t) throws IOException;
    }
    
    public static <T, R> Function<T, R> uncheckFunctionIo(CheckedIoFunction<T, R> checkedIoFunction) {
        return (t) -> {
            try {
                return checkedIoFunction.apply(t);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

This makes it easy to use a lambda declared to throw `IOException` e.g. within
a `Stream` pipeline:

    optionalPath.map(uncheckFunctionIo(path -> Files.readAllBytes(path)))

Such a lambda would otherwise be impossible to use within a `Stream` pipeline
without a nasty `try` block.

### bases covered

Currently the project generates two classes:

* `IoUncheckers`, which unchecks otherwise functional types throwing
`IOException` by catching and rethrowing in `UncheckedIOException`

* `Uncheckers`, which unchecks otherwise functional types throwing `Exception`
by rethrowing in `RuntimeException`

Each class defines an interface/method pair corresponding to each of `Runnable`,
`Comparator`, and the 43 functional interface types of `java.util.function`.

The `Generator` class could be used to generate methods to handle other
exception types and/or other functional types.
