/**
 * Provides classes supporting the use of functional programming idioms, building on
 * {@link com.google.common.base.Functions}
 *
 * <h1>Monads</h1>
 *
 * Most importantly, we're borrowing the idea of "Monads" from the Functional Programming world to deal cleanly with
 * asynchronous operations in the browser. Without some sort of functional approach, asynchronous
 * operations, whether those are AJAX requests or indexedb queries, quickly devolve into callback
 * pyramids of doom, and lead to terrible, awful, unmaintainable code that no one wants to touch.</p>
 *
 * <p>{@code Promise}s, which we're stealing from the JavaScript world, are the most important Monads for
 * ActivityInfo. Promises wrap a given type {@code T} along with the {@code Promise.State}
 * of the underlying value: it can be {@code PENDING}, meaning that we're waiting for the server or the database
 * thread to respond, or it could be {@code REJECTED}, meaning that some error has occurred and we won't get the value,
 * or {@code SETTLED}, meaning the value is available.
 * </p>
 *
 * <p>The {@code Optional} class from the Guava library is another example of a Monad: it wraps an underlying
 * type {@code T} along with, essentially, a flag indicating whether the value is present or absent.  </p>
 *
 * <p>The challenge of dealing with Monads in general, and Promises specifically, is to separate core
 * program logic from the bookkeeping needed for dealing the details related to the Monadic Class. For
 * the {@code Optional} monad, the goal is to separate whatever interesting stuff you're doing without having
 * to check for {@code null} on every line.</p>
 *
 * <p>For the case of our Promises, we want to get about our business without having to constantly worry about
 * whether the request succeeded or failed. Or rather, we want to group together a series of operations, perhaps
 * involving many many asynchronous requests, and only deal once with the question of whether or not the operation
 * succeeded as a whole.</p>
 *
 * <p>We can achieve this separation of concerns with the what Haskell people would call the
 * call the <strong>bind</strong>, <strong>join</strong>, and <strong>fmap</strong> functions.</p>
 *
 * <h2>Bind, or "then"</h2>
 *
 * <p>We've modeled the {@code bind} function as a method on the {@code Promise} called
 * {@link org.activityinfo.promise.Promise#then(com.google.common.base.Function)} to
 * match the JavaScript Promise specification.
 * </p>
 *
 * <p>In essence the {@code bind} function unwraps or "pierces" the Promise Monad, preforms an operation on the
 * unwrapped value, and then wraps the result back up in a new Promise Monad</p>
 *
 * <h2>Join</h2>
 *
 * <p>Conceptually the {@code join} operator flattens two Monads into a single result.
 * See {@link org.activityinfo.promise.Promise#join(com.google.common.base.Function)}</p>
 *
 *
 */
package org.activityinfo.promise;