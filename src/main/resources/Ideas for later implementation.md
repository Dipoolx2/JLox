# Ideas for further implementation
Here, ideas for further implementation of the Lox language are documented.

## Idea 1: Better string support
### 1.1 Escape sequence support for strings
- The idea is to support escape sequences such as \n and \t in strings.
- What would be necessary for this is to unescape these and pass the unescaped version as a literal when scanning.\
- See section 4.6.1 in the book.
### 1.2 Backtick strings (`)
- The idea is to introduce "backtick strings", with ` marks surrounding the string.
- A string with the backticks will allow newlines.
- A string with double quote marks won't allow newlines.
- This is to have better newline control.
- 
## Idea 2: Further number notation
### 2.1: Leading points
- Allow for leading points (e.g .234 as another way of writing 0.234).
### 2.2: Scientific notation
- Allow for scientific notation to indicate a number (e.g 1.22e-2).

## Idea 3: Better error printing
### 3.1: Better errors A
- For each incorrect character in the syntax, a separate message is given.
- It would be nice if all these incorrect characters can be put into a singular message.
- See note at section 4.5.1 in the book.
### 3.2: Better errors B
- The idea is that the new version would print the appropriate place where the syntax error occurs to the user.
- What would be needed for this, is the column of the line where the token is in the sentence
- You would also need the offset of this particular column until where the syntax error extends.
- This should only be calculated at the moment that the specific place is printed. Because otherwise it is very slow.
- See section 4.2.3 in the book.
