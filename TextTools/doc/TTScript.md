TT Script File Format
=====================

## Hello World

		start: "Hello World!";
	
Script outputs always "Hello World!".

## Format

### Models

Syntax

		name ":" sequence ((","|"|") sequence)* ";"

Sequence is a list of expressions. Expression can be either string or model-reference.

"start" is a special model that is executed first.

Example

		abc: "a", "b", "c";
		start: abc;

Script outputs either "a", "b" or "c".

### Expressions

Syntax

		(
		value				# value
		| "(" expression ")"		# expression
		| "{" expression "}"		# quiet expression
		| variable "=" value		# variable assign
		| field "=" value		# field assign
		) (
		"*"				# repeat 0-9 times
		| "?"				# repeat 0-1 times
		| "{" a=int ("," b=int)? "}"	# repeat 1-a times or a-b times
		)
		
Quiet expression does not return a value. It is useful in variable assign,
because

		$a="text"

returns "text".

Expressions can be repeated. Repeat times are always random.

### Values

Syntax

		model-name				# calculates model and returns its value
		| "[" model-name ("::" field-name)? "]"	# returns the field of a random precalculated model
		| variable				# returns the name of the variable
		| string
		
TT interpreter remembers every field of every model calculated. Default field is "name". If no models are calculated,
interpreter throws ImpossibleException.

Example:

		start: cat "\n" mouse "\n" eat;
		cat: "There is a cat called " name=random-name ".";
		mouse: "There is a mouse called " name=random-name ".";
		eat:
			[cat] eats [mouse] "."
		;
		
### Variables

Syntax

		"$" name

Assigning value to a variable

		variable "=" value
		
		Example:
		
		$var="car"

Variables are always global.
