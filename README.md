# LJSP
A minimal lisp interpreter (+ compiler stub) that runs on the JVM

This is a very messy old project (almost 7 years old at the time of writing).
I will introduce it somewhat: To begin with: the main files to look at is
ljsp.java. The main file for bootstrapping the lisp environment is
stuff.ljsp. If you want to have a look at how to instantiate and
interact with java objects from within LJSP see: java.ljsp.

There are a lot of .j (jasmin) files that are committed. In fact most
of these are actually compilation output (see: compile.ljsp) and
commiting them to git is a bit ugly, but as some are hand-generated
mock-ups or tests of future plans for the compiler I elected to simply
commit all of them.

Some keywords for lispers: This is a lisp-1 (one namespace) with
dynamic bindings (shallow), so watch out if you ever do any threading
(it is in fact doable, if you're careful that any shared variables are
treated as read-only). One design principle was to have as few
primitive functions and forms as possible while keeping the
interpreter reasonable performant, so e.g. let is simply implemented
using lambda. Macros are first-class objects as far as the interpreter
sees it (the compiler doesn't), they're simply a special sort of
lambda whose return value is evaluated (so not quite fexprs, but could
be used to implement them or vice versa).

(TODO: There are plans to change to use deep bindings and maybe even
lexical scoping on top of that (as we then get passable environments),
perhaps to the detriment of speed however.)

Some notes about the compiler: It is currently not very mature, and
only supports static bindings: that is the variables in a function are
only accesible to the function itself, and not to its callees as is
the case with dynamic bindings. Since let is actually implemented
using lambda, we get such strange results such as variable in the
scope outside the let not being callable from inside it.

(TODO: If I ever get around to implementing lexical scoping in the
compiler and interpreter their semantics could be mostly brought in line with
each other that way.)

# How to fire up a REPL:
Simple compile ljsp.java and then run ljsp.sh.
